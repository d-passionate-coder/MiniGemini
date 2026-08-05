package org.example.chatservice.message;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chatservice.config.SecurityUtils;
import org.example.chatservice.conversation.Conversation;
import org.example.chatservice.conversation.ConversationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageService {
    private final ConversationService conversationService;
    private final MessageRepository messageRepository;

    @Transactional
    public void saveLlmResponse(String content, UUID conversationId){
        Conversation conversation = conversationService.getConversationEntityById(conversationId);
        Message newMessage = Message.builder()
                            .content(content)
                            .role("model")
                            .conversation(conversation)
                            .build();
        messageRepository.save(newMessage);
    }

    @Transactional
    public void saveUserMessage(MessageRequest request, UUID conversationId){
        Conversation conversation = conversationService.getConversationEntityById(conversationId);
        Message newMessage = Message.builder()
                            .conversation(conversation)
                            .content(request.content())
                            .role("user")
                            .build();

        messageRepository.save(newMessage);

    }

    @Transactional
    public List<Map<String,String>> getHistory(UUID conversationId){
          List<Message> messages = messageRepository.findTop10ByConversationIdOrderByCreatedAtDesc(conversationId);
          java.util.Collections.reverse(messages);
          return messages.stream()
                  .map(msg ->
                      Map.of("role",msg.getRole(),"content",msg.getContent())
                  )
                  .collect(Collectors.toList());
    }


    public Conversation createFirstConversation(){
        UUID userId = SecurityUtils.getCurrentUserId();
        Conversation newConvo = new Conversation();
        newConvo.setUserId(userId);
        newConvo.setTitle("New Convo");
        return conversationService.saveConversation(newConvo);
    }



}
