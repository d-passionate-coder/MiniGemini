package org.example.chatservice.conversation;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
     Optional<Conversation> findByTitle(String title);
     List<Conversation> findByUserId(UUID userId);
     List<Conversation> findByUserIdOrderByCreatedAtDesc(UUID userId);
     Optional<Conversation> findByIdAndUserId(UUID id, UUID userId);
     void deleteByIdAndUserId(UUID id, UUID userId);
     @Transactional
     @Modifying
     @Query("UPDATE Conversation c SET title = :title WHERE c.id = :id AND c.userId = :userId")
     void updateTitleByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId, @Param("title") String title);

}
