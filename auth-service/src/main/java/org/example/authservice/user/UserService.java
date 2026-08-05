package org.example.authservice.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.authservice.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByEmail(String email){
        System.out.println("here");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public boolean existsByEmail(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public User save(User user) {
        User savedUser = userRepository.save(user);
        return savedUser;
    }

}
