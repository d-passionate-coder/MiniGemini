package org.example.chatservice.conversation;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chatservice.config.SecurityUtils;
import org.example.chatservice.exception.AppException;
import org.example.chatservice.message.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {
      private final ConversationRepository conversationRepository;

      public ConversationResponse getConversationById(UUID id){
          Conversation conversation = conversationRepository.findById(id)
                          .orElseThrow(() -> new AppException("Conversation not found", HttpStatus.NOT_FOUND));

          List<MessageResponse> messageResponse = conversation.getMessages()
                  .stream()
                  .map((msg) -> new MessageResponse(
                          msg.getId(),
                          msg.getRole(),
                          msg.getContent(),
                          msg.getCreatedAt()
                  ))
                  .collect(Collectors.toList());

          return new ConversationResponse(
                  conversation.getId(),
                  conversation.getTitle(),
                  messageResponse,
                  conversation.getCreatedAt()
          );
      }

    public Conversation getConversationEntityById(UUID id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new AppException("Conversation not found", HttpStatus.NOT_FOUND));
    }


      public List<ConversationResponse> getConversations(){
           UUID userId = SecurityUtils.getCurrentUserId();
            return conversationRepository.findByUserId(userId)
                  .stream()
                    .map(c -> new ConversationResponse(c.getId(),
                            c.getTitle(),
                            List.of(),
                            c.getCreatedAt()))
                    .collect(Collectors.toList());
      }


      public ConversationResponse createConversation(ConversationRequest request){
           UUID userId = SecurityUtils.getCurrentUserId();
           Conversation newConversation = Conversation.builder().title(request.getTitle()).userId(userId).build();
           Conversation saved = conversationRepository.save(newConversation);
           return new ConversationResponse(saved.getId(),saved.getTitle(),List.of(),saved.getCreatedAt());
      }

      @Transactional
      public void deleteConversation(UUID id){
          UUID userId = SecurityUtils.getCurrentUserId();
          conversationRepository.deleteByIdAndUserId(id, userId);
      }

      public Conversation saveConversation(Conversation conversation){
          return conversationRepository.save(conversation);
      }

    public void updateConversationTitle(UUID conversationId, String title, UUID userId){
        conversationRepository.updateTitleByIdAndUserId(conversationId,userId,title);
    }


}
