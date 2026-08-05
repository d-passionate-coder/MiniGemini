package org.example.authservice.user;

import jakarta.persistence.*;
import lombok.*;
import org.example.authservice.signup.SignupRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
      @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      private UUID id;

      @Column(nullable = false)
      private String firstName;

      @Column
      private String lastName;

      @Column(nullable = false, unique = true)
      private String email;

      @Column(nullable = false)
      private String passwordHash;

      @Column(nullable = false)
      private LocalDateTime createdAt;

      @PrePersist
      protected void onCreate() {
            createdAt = LocalDateTime.now();
      }

      public static User from(SignupRequest request, String passwordHash) {
            return User.builder()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .passwordHash(passwordHash)
                    .build();
      }

}

