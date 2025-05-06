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

@Component
@RequiredArgsConstructor
public class ApiKeyFilter implements Filter {
    private static final String API_KEY_HEADER = "X-API-KEY";
    private final AuthenticationManager authenticationManager;
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
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI();

        // Пропускаем исключённые пути
        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Получаем API-ключ из заголовка
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null) {
            sendError(response, "Missing API Key");
            return;
        }

        // Ищем API-ключ в базе
        Optional<ApiKey> keyOptional = apiKeyRepository.findByKeyHashAndIsActiveTrue(apiKey);
        if (!keyOptional.isPresent()) {
            sendError(response, "Invalid API Key");
            return;
        }

        ApiKey key = keyOptional.get();

        // Проверяем срок действия ключа
        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
            key.set_active(false);
            apiKeyRepository.save(key);
            sendError(response, "API Key has expired");
            return;
        }

        // Получаем пользователя и его роль
        User user = key.getUser();
        if (user == null) {
            sendError(response, "No user associated with API Key");
            return;
        }

        String role = user.getRole().name();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                Collections.singletonList(authority)
        );
        authentication.setDetails(user);

        // Устанавливаем контекст безопасности
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("Setting authentication for user: " + user.getUsername() + " with role: " + role);
        System.out.println(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("text/plain");
        response.getWriter().write(message);
    }
}