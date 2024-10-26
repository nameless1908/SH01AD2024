package com.example.snapheal.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration  // Thêm @Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtTokenFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${api.prefix}")
    private String apiPrefix;
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF because you're probably using JWTs and not sessions
                .csrf(csrf -> csrf.disable())

                // Stateless session management for JWT-based authentication
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Set the authentication provider
                .authenticationProvider(authenticationProvider)

                // Add the JWT filter before the UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)

                // Configure authorization rules
                .authorizeHttpRequests(requests -> requests
                        // Allow all requests to /auth/signup and /auth/login without authentication
                        .requestMatchers(
                                String.format("%s/auth/register", apiPrefix),
                                String.format("%s/auth/login", apiPrefix),
                                String.format("%s/auth/refresh-token", apiPrefix)
                        ).permitAll()

                        // Require authentication for any other request
                        .anyRequest().authenticated()
                );

        // Return the SecurityFilterChain
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:8005"));
        configuration.setAllowedMethods(List.of("GET","POST"));
        configuration.setAllowedHeaders(List.of("Authorization","Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**",configuration);

        return source;
    }
}