package org.example.authservice.token;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.authservice.exception.AppException;
import org.example.authservice.user.User;
import org.example.authservice.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    private final int REFRESH_TOKEN_EXPIRY_DAYS = 7;

    @Transactional
    public RefreshToken createRefreshToken(User user){
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())   // random UUID as token
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }


    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (refreshToken.isRevoked()) {
            throw new AppException("Refresh token has been revoked", HttpStatus.UNAUTHORIZED);
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);   // cleanup expired token
            throw new AppException("Refresh token has expired, please login again", HttpStatus.UNAUTHORIZED);
        }

        return refreshToken;
    }

    // revoke token on logout
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }


}
