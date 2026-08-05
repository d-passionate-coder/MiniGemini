package org.example.chatservice.conversation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
      private final ConversationService conversationService;

      @GetMapping
      public ResponseEntity<List<ConversationResponse>> getConversations(){
          return ResponseEntity.ok(conversationService.getConversations());
      }

      @GetMapping("/{id}")
      public ResponseEntity<ConversationResponse> getConversationById(@PathVariable UUID id){
          return ResponseEntity.ok(conversationService.getConversationById(id));
      }

      @PostMapping
      public ResponseEntity<ConversationResponse> createConversation(@RequestBody @Valid ConversationRequest request){
          return ResponseEntity.status(HttpStatus.CREATED).body(conversationService.createConversation(request));
      }

      @DeleteMapping("/{id}")
      public ResponseEntity<Void> deleteConversation(@PathVariable UUID id){
          conversationService.deleteConversation(id);
          return ResponseEntity.noContent().build(); //204
      }
    
}
