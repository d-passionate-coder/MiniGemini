package org.example.chatservice.message;


import lombok.RequiredArgsConstructor;
import org.example.chatservice.conversation.ConversationService;
import org.example.chatservice.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LlmClient {

    @Value("${gemini.api.version}")
    private String apiVersion;

    @Value("${gemini.api.model}")
    private String modelName;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final MessageService messageService;
    private final ConversationService conversationService;


    public void streamFromLlm(List<Map<String,String>> history, SseEmitter emitter, UUID conversationId){
        StringBuffer fullResponseBuilder = new StringBuffer();
        LlmRequest requestBody = LlmRequest.fromHistory(history);
        requestBody.contents().forEach(content -> {
            System.out.println("Role: " + content.role() + " | Part text: " + content.parts().get(0).text());
        });

        String uri = String.format("/%s/models/%s:streamGenerateContent?alt=sse&key=%s",apiVersion,modelName,apiKey);

        try {
            webClient.post()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(LlmResponse.class)
                    .doOnNext(response -> {
                               try {
                                   if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
                                       var candidate = response.candidates().get(0);
                                       if (candidate.content() != null && candidate.content().parts() != null && !candidate.content().parts().isEmpty()) {
                                           String token = candidate.content().parts().get(0).text();
                                           if (token != null) {
                                               fullResponseBuilder.append(token);

                                               // Thread-safe guard
                                               synchronized (emitter) {
                                                   emitter.send(SseEmitter.event().name("token").data(token));
                                               }
                                           }
                                       }
                                   }
                               } catch (Exception ex) {
                                   ex.printStackTrace();
                                   throw new AppException("Error pushing token to SSE emitter", HttpStatus.INTERNAL_SERVER_ERROR);
                               }
                    }
                    )
                    .doOnComplete(() -> {
                        try {
                            String finalResponse = fullResponseBuilder.toString();

                            // 1. Save to Database FIRST
                            if (finalResponse != null && !finalResponse.isBlank()) {
                                System.out.println("[DEBUG] Persisting LLM response for conversation: " + conversationId);
                                messageService.saveLlmResponse(finalResponse, conversationId);
                                System.out.println("[DEBUG] Message saved successfully.");
                            }

                            // 2. Notify Client & Complete SSE Stream SECOND
                            synchronized (emitter) {
                                emitter.send(SseEmitter.event().name("complete").data("[DONE]"));
                                emitter.complete();
                            }
                        }
                        catch (Exception e) {
                            System.err.println("[ERROR] Failed during SSE completion/DB save:");
                            e.printStackTrace();
                            synchronized (emitter) {
                                try {
                                    emitter.completeWithError(e);
                                } catch (Exception ignored) {}
                            }
                        }
                    })
                    .doOnError(org.springframework.web.reactive.function.client.WebClientResponseException.class, e -> {
                        System.err.println("❌ Gemini Stream Response API Rejected Request!");
                        System.err.println("Status Code: " + e.getStatusCode());
                        System.err.println("Error Body: " + e.getResponseBodyAsString()); //
                    })
                    .subscribe();
        }
        catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            System.out.println("Gemini API Rejected the Request!");
            System.out.println("HTTP Status Code: " + e.getStatusCode());
            System.out.println("Error Body From Gemini: " + e.getResponseBodyAsString()); // 🚀 This line is the golden ticket
            emitter.completeWithError(e);
        }
        catch (Exception e){
             emitter.completeWithError(e);
             e.printStackTrace();
             throw new AppException("Fatal exception inside async streaming engine invocation",HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    public void generateTitle(MessageRequest request, SseEmitter emitter, UUID conversationId, UUID userId){
        String userPrompt = request.content();
        String titlePrompt = "Generate a concise 3-5 word chat title based on this message. " +
                "Do not include quotes or punctuation. Message: " + userPrompt;

        LlmRequest titleRequest = LlmRequest.fromSinglePrompt(titlePrompt);
        String uri = String.format("/%s/models/%s:generateContent?key=%s", apiVersion, modelName, apiKey);

        try {
            webClient.post()
                    .uri(uri)
                    .bodyValue(titleRequest)
                    .retrieve()
                    .bodyToMono(LlmResponse.class)
                    .map(response -> {
                        // Safe parsing logic matching your structural tree
                        if (response != null && response.candidates() != null && !response.candidates().isEmpty()) {
                            var parts = response.candidates().get(0).content().parts();
                            if (parts != null && !parts.isEmpty()) {
                                return parts.get(0).text().trim();
                            }
                        }
                        return "New Chat"; // Fallback name
                    })
                    .doOnNext(title -> {
                        try {
                            // Synchronize on the emitter to guarantee safety against concurrent write interleaving
                            conversationService.updateConversationTitle(conversationId,title,userId);
                            synchronized (emitter) {
                                emitter.send(SseEmitter.event().name("title").data(title));

                            }
                        } catch (Exception ex) {
                            System.err.println("Failed to push title event to emitter: " + ex.getMessage());
                        }
                    })
                    .doOnError(org.springframework.web.reactive.function.client.WebClientResponseException.class, e -> {
                        System.err.println("⚠️ Title generation rate-limited. Setting fallback title.");
                        // Update DB with fallback title so app doesn't crash
                        conversationService.updateConversationTitle(conversationId, "New Conversation", userId);
                        synchronized (emitter) {
                            try {
                                emitter.send(SseEmitter.event().name("title").data("New Conversation"));
                            } catch (Exception ignored) {}
                        }
                        System.err.println("❌ Gemini Title API Rejected Request!");
                        System.err.println("Status Code: " + e.getStatusCode());
                        System.err.println("Error Body: " + e.getResponseBodyAsString());
                    })
                    .subscribe();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
