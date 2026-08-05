package org.example.authservice.login;

import lombok.RequiredArgsConstructor;
import org.example.authservice.token.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;



    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }

}
