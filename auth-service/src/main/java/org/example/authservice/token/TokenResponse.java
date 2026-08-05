package org.example.authservice.token;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String firstName;
    private String tokenType = "Bearer";

    public TokenResponse(String accessToken, String refreshToken, String firstName){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.firstName = firstName;
    }
}
