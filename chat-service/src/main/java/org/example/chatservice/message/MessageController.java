package org.example.chatservice.message;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.chatservice.config.SecurityUtils;
import org.example.chatservice.conversation.Conversation;
import org.example.chatservice.conversation.ConversationRepository;
import org.example.chatservice.conversation.ConversationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final LlmClient llmClient;
    private final ConversationRepository conversationRepository;

    @PostMapping(value = "/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessage(@PathVariable UUID id, @RequestBody MessageRequest request){
            SseEmitter emitter = new SseEmitter(180_000L);
            messageService.saveUserMessage(request,id);
            List<Map<String,String>> history = messageService.getHistory(id);
            llmClient.streamFromLlm(history, emitter, id);
            return emitter;
    }


    @PostMapping(value = "/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendFirstMessage(@RequestBody MessageRequest request, HttpServletResponse response){
           SseEmitter emitter = new SseEmitter(180_000L);

           Conversation savedConvo = messageService.createFirstConversation();
           UUID conversationId = savedConvo.getId();

           response.setHeader("X-Conversation-Id", conversationId.toString());

           messageService.saveUserMessage(request,conversationId);
           llmClient.generateTitle(request,emitter,conversationId, savedConvo.getUserId());
            List<Map<String, String>> history = List.of(Map.of(
                    "role", "user",
                    "content", request.content()
            ));
           llmClient.streamFromLlm(history,emitter,conversationId);
           return emitter;
    }

}
