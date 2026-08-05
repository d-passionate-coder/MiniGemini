package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AppException{
     public UserNotFoundException(String email){
         super("No account found with this email: "+email, HttpStatus.NOT_FOUND);
     }
}
