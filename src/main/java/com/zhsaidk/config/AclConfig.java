package com.zhsaidk.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableMethodSecurity
public class AclConfig {

    @Bean
    public MutableAclService aclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache, ConversionService conversionService) {
        CustomJdbcMutableAclService aclService = new CustomJdbcMutableAclService(dataSource, lookupStrategy, aclCache);
        aclService.setClassIdentityQuery("SELECT currval(pg_get_serial_sequence('acl_class', 'id'))");
        aclService.setSidIdentityQuery("SELECT currval(pg_get_serial_sequence('acl_sid', 'id'))");
        aclService.setConversionService(conversionService);
        return aclService;
    }

    @Bean
    public LookupStrategy lookupStrategy(DataSource dataSource, AclCache aclCache, AclAuthorizationStrategy aclAuthorizationStrategy, ConversionService conversionService) {
        LookupStrategy lookupStrategy = new CustomLookupStrategy(
                dataSource,
                aclCache,
                aclAuthorizationStrategy,
                new ConsoleAuditLogger(),
                conversionService
        );
        System.out.println("ConversionService set: " + conversionService);
        return lookupStrategy;
    }

    @Bean
    public ConversionService conversionService() {
        return CustomAclConversionService.createConversionService();
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ADMIN"));
    }

    @Bean
    public AclCache aclCache(CacheManager cacheManager, AclAuthorizationStrategy aclAuthorizationStrategy) {
        return new SpringCacheBasedAclCache(
                cacheManager.getCache("aclCache"),
                new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger()),
                aclAuthorizationStrategy
        );
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("aclCache");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000));
        return cacheManager;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(MutableAclService aclService) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new AclPermissionEvaluator(aclService));
        return expressionHandler;
    }
}