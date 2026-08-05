package org.example.authservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
     private int status;
     private String message;
     private LocalDateTime timestamp;

     public static ErrorResponse of(HttpStatus status, String message){
         return new ErrorResponse(status.value(),message,LocalDateTime.now());
     }
}
