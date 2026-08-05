package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AppException {
    public EmailAlreadyExistsException(String email){
        super("Email already Registered: "+email, HttpStatus.CONFLICT);
    }
}
