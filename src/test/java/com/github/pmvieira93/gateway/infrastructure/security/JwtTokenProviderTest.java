package com.github.pmvieira93.gateway.infrastructure.security;


import com.auth0.jwt.interfaces.Claim;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider("gateway-spring-verify");

    static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJqd3Qtd2ViIiwic3ViIjoiMTIzNDU2Nzg5MCIsImF1ZCI6ImFwaS1nYXRld2F5IiwibmFtZSI6ImZvbyIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNzYzNzE1ODkxLCJqdGkiOiI5NmM3ZmMyNi1kNjQwLTRmZGYtODlmNi1hYjRkYmJiNmNlYTQifQ.trSAHOMPsvCCpc6AL5GSB7o7I2NLzVd1yEOnnpI0tAM";

    @Test
    void givenValidToken_whenCheckIsTokenValid_shouldReturnTrue() {
        // Given
        final boolean expected = true;

        // When
        boolean result = jwtTokenProvider.isTokenValid(TOKEN);

        // Then
        assertThat(result).isEqualTo(expected);

    }

    @Test
    void givenInvalidToken_whenCheckIsTokenValid_shouldReturnFalse() {
        // Given
        final boolean expected = false;

        // When
        boolean result = jwtTokenProvider.isTokenValid(TOKEN+"invalid");

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void givenExpiredToken_whenCheckIsTokenValid_shouldReturnFalse() {
        // Given
        final boolean expected = false;
        final String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJqd3Qtd2ViIiwic3ViIjoiMTIzNDU2Nzg5MCIsImF1ZCI6ImFwaS1nYXRld2F5IiwibmFtZSI6ImZvbyIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyLCJqdGkiOiI5NmM3ZmMyNi1kNjQwLTRmZGYtODlmNi1hYjRkYmJiNmNlYTQifQ.Vj62zVg-8z_ynbC-Evf8wxbelEGFs5i5KAKFpkn92FA";

        // When
        boolean result = jwtTokenProvider.isTokenValid(expiredToken);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void givenInvalidSecretKey_whenCheckIsTokenValid_shouldReturnFalse() {
        // Given
        final boolean expected = false;
        JwtTokenProvider jwtTokenProviderProxy = new JwtTokenProvider("gateway-spring");

        // When
        boolean result = jwtTokenProviderProxy.isTokenValid(TOKEN);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void givenValidToken_whenGetUser_shouldReturnUserId() {
        // Given
        final String expected = "1234567890";

        // When
        String result = jwtTokenProvider.getUser(TOKEN);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void givenNullSecretKey_whenGetUser_shouldReturnNull() {
        // Given
        JwtTokenProvider jwtTokenProviderProxy = new JwtTokenProvider(null);

        // When
        String result = jwtTokenProviderProxy.getUser(TOKEN);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void givenValidToken_whenParseToken_shouldReturnPayloadAsMap() {
        // Given & When

        Map<String, Claim> result = jwtTokenProvider.parseToken(TOKEN);

        // Then
        assertThat(result)
                .isNotNull()
                .hasSizeGreaterThan(0);
    }

    @Test
    void givenInvalidToken_whenParseToken_shouldReturnEmptyMap() {
        // Given
        JwtTokenProvider jwtTokenProviderProxy = new JwtTokenProvider("gateway-spring");

        // When
        Map<String, Claim> result = jwtTokenProviderProxy.parseToken(TOKEN);

        // Then
        assertThat(result)
                .isNull();
    }

}