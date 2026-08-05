package org.example.authservice.token;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request){
         RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());

         String newAccessToken = jwtService.generateToken(refreshToken.getUser());
         RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(refreshToken.getUser());
         String firstName = refreshToken.getUser().getFirstName();

         return ResponseEntity.ok(new TokenResponse(newAccessToken,newRefreshToken.getToken(), firstName));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest request){
        refreshTokenService.revokeToken(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully!");
    }
}
