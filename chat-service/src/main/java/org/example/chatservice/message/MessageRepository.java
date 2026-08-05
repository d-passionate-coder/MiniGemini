package org.example.chatservice.message;


import jakarta.transaction.Transactional;
import org.example.chatservice.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findTop10ByConversationIdOrderByCreatedAtDesc(UUID id);
}
