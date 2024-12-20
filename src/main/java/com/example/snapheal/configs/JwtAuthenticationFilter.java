package com.example.snapheal.configs;

import com.example.snapheal.entities.RefreshToken;
import com.example.snapheal.exceptions.TokenInvalidException;
import com.example.snapheal.repository.RefreshTokenRepository;
import com.example.snapheal.responses.ResponseObject;
import com.example.snapheal.service.JwtService;
import com.example.snapheal.service.RateLimiterService;
import com.example.snapheal.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIP = request.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(clientIP);

        if (!bucket.tryConsume(1)) {
            // Trả về mã lỗi 429 nếu quá giới hạn tần suất
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(new ObjectMapper().writeValueAsString(
                    ResponseObject.builder()
                            .status(HttpStatus.TOO_MANY_REQUESTS)
                            .code(HttpStatus.TOO_MANY_REQUESTS.value())
                            .message("Too many requests")
                            .data(null)
                            .build()
            ));
            return;
        }

        var authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (username != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                RefreshToken refreshToken = refreshTokenRepository.findByToken(jwt).orElseThrow(
                        () -> new TokenInvalidException("Token not found!")
                );

                if (refreshToken.isRevoked()) {
                    throw new TokenInvalidException("Access token is invalid because its refresh token has been revoked!");
                }
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            // Send response with error message directly if the token is expired
            handleException(response, "Token has expired");
        } catch (JwtException e) {
            // Send response with error message directly for other JWT issues
            handleException(response, "Invalid token");
        } catch (Exception e) {
            // General exception handling
            handleException(response, e.getMessage());
        }
    }

    private void handleException(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(new ObjectMapper().writeValueAsString(
                ResponseObject.builder()
                        .status(HttpStatus.UNAUTHORIZED)
                        .code(HttpStatus.UNAUTHORIZED.value())
                        .message(message)
                        .data(null)
                        .build()
        ));
    }
}
