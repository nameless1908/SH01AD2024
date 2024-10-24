package com.example.snapheal.service;

import com.example.snapheal.dtos.LoginUserDto;
import com.example.snapheal.dtos.RegisterUserDto;
import com.example.snapheal.entities.User;
import com.example.snapheal.exceptions.DataNotFoundException;
import com.example.snapheal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public User signup(RegisterUserDto input) {
        User user = User.builder()
                .username(input.getUsername())
                .fullName(input.getFullName())
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .build();

        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) throws Exception {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(
                     () ->  new DataNotFoundException("User not found for email: " + input.getEmail())
                );
        if (!passwordEncoder.matches(input.getPassword(), user.getPassword())) {
            throw new DataNotFoundException("Password not correct!");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        input.getPassword()
                )
        );

        return user;
    }
}