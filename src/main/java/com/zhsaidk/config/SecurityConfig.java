package com.zhsaidk.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistrationBean(ApiKeyFilter apiKeyFilter) {
        System.out.println("Registering ApiKeyFilter with pattern /*");
        FilterRegistrationBean<ApiKeyFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(apiKeyFilter);
        registrationBean.addUrlPatterns("/*"); // Перехватываем все запросы
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/user/**", "/rest/**", "/projects/**").hasAnyAuthority("ADMIN", "USER")
                        .anyRequest().permitAll())
                .formLogin(login->login
                        .loginPage("/login")
                        .defaultSuccessUrl("/projects"))
                .build();
    }
}