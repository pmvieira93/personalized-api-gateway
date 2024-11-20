package com.github.pmvieira93.gateway.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;


import java.util.Map;
import java.util.Objects;

/**
 * <a href="https://medium.berkayozcan.com/using-json-web-tokens-jwt-with-spring-boot-for-authentication-and-authorization-7d6f62ab5ecc">...</a>
 * <a href="https://github.com/jwtk/jjwt?tab=readme-ov-file#reading-a-jws">...</a>
 */
@Slf4j
public class JwtTokenProvider {


    private final String secretKey;

    public JwtTokenProvider(final String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isTokenValid(String token) {
        return Objects.nonNull(extractToken(token));
    }

    public Map<String, Claim> parseToken(String token) {
        Map<String, Claim>  claims = null;
        DecodedJWT tokenData = extractToken(token);
        if(Objects.nonNull(tokenData)) {
            claims = tokenData.getClaims();
        }
        return claims;
    }

    public String getUser(final String token) {
        DecodedJWT jwt = extractToken(token);
        return jwt != null ? jwt.getSubject() : null;
    }

    private DecodedJWT extractToken(String token) {
        DecodedJWT tokenData = null;
        if(Objects.nonNull(token)) {
            try {
                tokenData = JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token);
            } catch (JWTVerificationException ex) {
                // Invalid signature/claims
                log.error(ex.getMessage());
            } catch (IllegalArgumentException ex) {
                // JWT token is empty
                log.error(ex.getMessage());
            }
        }
        return tokenData;
    }

}
