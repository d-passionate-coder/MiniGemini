package org.example.authservice.token;


import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
