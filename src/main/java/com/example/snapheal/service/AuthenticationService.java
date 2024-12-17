package com.example.snapheal.service;

import com.example.snapheal.dtos.LoginUserDto;
import com.example.snapheal.dtos.RegisterUserDto;
import com.example.snapheal.entities.User;
import com.example.snapheal.exceptions.CustomErrorException;
import com.example.snapheal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public User signup(RegisterUserDto input) throws Exception {
        Optional<User> emailExist = userRepository.findByEmail(input.getEmail());
        if (emailExist.isPresent()) {
            throw new CustomErrorException("Email exist!");
        }

        Optional<User> usernameExist = userRepository.findByUsername(input.getUsername());
        if (usernameExist.isPresent()) {
            throw new CustomErrorException("Username exist!");
        }

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
                        () -> new CustomErrorException("User not found for email: " + input.getEmail())
                );
        if (!passwordEncoder.matches(input.getPassword(), user.getPassword())) {
            throw new CustomErrorException("Password not correct!");
        }
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        input.getPassword()
                )
        );

        return (User) auth.getPrincipal();
    }
}