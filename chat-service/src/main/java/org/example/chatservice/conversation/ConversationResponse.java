package org.example.chatservice.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.chatservice.message.MessageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ConversationResponse {
     private UUID id;
     private String title;
     private List<MessageResponse> messages;
     private LocalDateTime localDateTime;
}
