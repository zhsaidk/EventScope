package com.zhsaidk.config;

import com.zhsaidk.database.entity.ApiKey;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.ApiKeyRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {
    private static final String API_KEY_HEADER = "X-API-KEY";
    private final ApiKeyRepository apiKeyRepository;
    private static final String[] EXCLUDE_PATHS = {
            "/user", "/login", "/rest", "/project"
    };

    private boolean isExcluded(String path) {
        for (String exclude : EXCLUDE_PATHS) {
            if (path.startsWith(exclude)) {
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

        String role = user.getRole().name();
        var authority = new SimpleGrantedAuthority(role);
        var authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                Collections.singletonList(authority)
        );
        authentication.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("Authenticated " + user.getUsername() + " as " + role);

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("text/plain");
        response.getWriter().write(message);
    }
}
