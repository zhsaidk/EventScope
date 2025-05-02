package com.zhsaidk.config;

import com.zhsaidk.database.entity.ApiKey;
import com.zhsaidk.database.repo.ApiKeyRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter implements Filter {
    private final static String API_KEY_HEADER = "X-API-KEY";
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

        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        boolean flag = false;

        if (apiKey != null) {
            ApiKey key = apiKeyRepository.findByKeyHashAndIsActiveTrue(apiKey).orElse(null);
            if (key != null) {
                if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
                    key.set_active(false);
                    apiKeyRepository.save(key);
                } else if (Objects.equals(apiKey, key.getKey_hash())) {
                    flag = true;
                }
            }
        }


        if (flag) {
            System.out.println("API Key valid, proceeding");
            filterChain.doFilter(request, response);
        } else {
            System.out.println("API Key invalid or missing, rejecting");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("text/plain");
            response.getWriter().write("Invalid or missing API Key");
            return;
        }
    }
}