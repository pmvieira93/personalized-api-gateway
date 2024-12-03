package com.github.pmvieira93.gateway.infrastructure.config;

import com.github.pmvieira93.gateway.infrastructure.security.JwtTokenProvider;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class JwtTokenConfig {

    @Value("${jwt.security.secret}")
    private String secret;

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(secret);
    }
}
