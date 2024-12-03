package com.github.pmvieira93.gateway.common.utils;


import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.pmvieira93.gateway.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenUtilsTest {

    @Mock
    DecodedJWT decodedJWT;

    @Test
    void givenValidKeyAndNullJwt_whenSearch_shouldReturnNull() {
        // Given
        final DecodedJWT jwt = null;

        // When
        final String result = JwtTokenUtils.search(JwtTokenProvider.SIGN, jwt);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void givenValidKeyAndDecodeJwtWithClaimsNull_whenSearch_shouldReturnNull() {
        // Given
        when(decodedJWT.getClaims()).thenReturn(null);

        // When
        final String result = JwtTokenUtils.search(JwtTokenProvider.SIGN, decodedJWT);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void givenValidKeyAndDecodeJwtNoClaims_whenSearch_shouldReturnNull() {
        // Given
        final Map<String, Claim> claims = Map.of();
        when(decodedJWT.getClaims()).thenReturn(claims);

        // When
        final String result = JwtTokenUtils.search(JwtTokenProvider.SIGN, decodedJWT);

        // Then
        assertThat(result).isNull();
    }

}