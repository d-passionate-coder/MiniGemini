package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException{
    public InvalidCredentialsException(){
        super("Incorrect Password", HttpStatus.UNAUTHORIZED);
    }
}
