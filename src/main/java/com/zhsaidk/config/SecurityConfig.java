package com.zhsaidk.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final ApiKeyFilter apiKeyFilter;

    @Bean
    @Order(value = 0)
    public SecurityFilterChain securityFilterChainFirst(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth->auth
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // 401 для неаутентифицированных
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("text/plain");
                            response.getWriter().write("Access Denied");
                        }))
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(value = 1)
    public SecurityFilterChain securityFilterChainSecond(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth->auth
                        .requestMatchers(HttpMethod.GET, "/projects/**", "/user/**").hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/projects/**", "/user/**").hasAuthority("ADMIN")

                        .anyRequest().permitAll())
                .formLogin(login->login
                        .loginPage("/login")
                        .defaultSuccessUrl("/projects"))
                .build();
    }
}