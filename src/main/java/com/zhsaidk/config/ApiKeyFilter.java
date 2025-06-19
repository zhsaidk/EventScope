package com.zhsaidk.config;

import com.zhsaidk.database.entity.ApiKey;
import com.zhsaidk.database.entity.Role;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.ApiKeyRepository;
import com.zhsaidk.service.UserDetailsImpl;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {
    private static final String API_KEY_HEADER = "X-API-KEY";
    private final ApiKeyRepository apiKeyRepository;
    private static final String[] EXCLUDE_PATHS = {
            "/user", "/login", "/rest", "/project",
            "/css/**", "/js/**", "/images/**", "/error"
    };

    private boolean isExcluded(String path) {
        for (String exclude : EXCLUDE_PATHS) {
            if (path.startsWith(exclude.replace("/**", ""))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null) {
            sendError(response, "Missing API Key");
            return;
        }

        Optional<ApiKey> keyOptional = apiKeyRepository.findByKeyHashAndIsActiveTrue(apiKey);
        if (!keyOptional.isPresent()) {
            sendError(response, "Invalid API Key");
            return;
        }

        ApiKey key = keyOptional.get();

        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
            key.set_active(false);
            apiKeyRepository.save(key);
            sendError(response, "API Key has expired");
            return;
        }

        User user = key.getUser();
        if (user == null) {
            sendError(response, "No user associated with API Key");
            return;
        }
        Role role = user.getRole();
        UserDetailsImpl userDetails = new UserDetailsImpl(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getPassword(),
                Set.of(user.getRole()));
        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                Collections.singletonList(role)
        );
        authentication.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("text/plain");
        response.getWriter().write(message);
    }
}