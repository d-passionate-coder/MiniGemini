package org.example.authservice.login;

import lombok.RequiredArgsConstructor;
import org.example.authservice.exception.InvalidCredentialsException;
import org.example.authservice.token.JwtService;
import org.example.authservice.token.RefreshToken;
import org.example.authservice.token.RefreshTokenService;
import org.example.authservice.token.TokenResponse;
import org.example.authservice.user.User;
import org.example.authservice.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
      private final UserService userService;
      private final JwtService jwtService;
      private final PasswordEncoder passwordEncoder;
      private final RefreshTokenService refreshTokenService;

          public TokenResponse login(LoginRequest request){
                  User user = userService.findByEmail(request.getEmail());

                  if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                      throw new InvalidCredentialsException();
                  }
                  String accessToken = jwtService.generateToken(user);

                  RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
                  System.out.println(accessToken + ' ' + refreshToken);

                  return new TokenResponse(accessToken,refreshToken.getToken(), user.getFirstName());
      }
}

