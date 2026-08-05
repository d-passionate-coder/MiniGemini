package org.example.authservice.signup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class SignupResponse {
    private String accessToken;
    private String refreshToken;
    private String firstName;
}
