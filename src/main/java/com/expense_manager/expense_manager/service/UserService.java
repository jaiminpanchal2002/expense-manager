package com.expense_manager.expense_manager.service;

import com.expense_manager.expense_manager.dto.RegisterRequest;
import com.expense_manager.expense_manager.entity.User;
import com.expense_manager.expense_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User register(RegisterRequest req) {
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword())) // hash it!
                .build();
        return userRepository.save(user);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}