package com.zhsaidk.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

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




}