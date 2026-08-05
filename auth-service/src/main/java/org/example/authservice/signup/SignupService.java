package org.example.authservice.signup;


import lombok.RequiredArgsConstructor;
import org.example.authservice.exception.EmailAlreadyExistsException;
import org.example.authservice.token.JwtService;
import org.example.authservice.token.RefreshToken;
import org.example.authservice.token.RefreshTokenService;
import org.example.authservice.user.User;
import org.example.authservice.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public SignupResponse signup(SignupRequest request){
        if (userService.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        String passwordHash = passwordEncoder.encode(request.getPassword());
        User newUser = User.from(request,passwordHash);
        User createdUser = userService.save(newUser);

        String accessToken = jwtService.generateToken(createdUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(createdUser);
        return new SignupResponse(accessToken,refreshToken.getToken(),createdUser.getFirstName());
    }
}
