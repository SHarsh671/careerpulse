package com.portfolio.jobapp.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {
    private static final String SECRET = "ASecretKeyThatIsAtLeastThirtyTwoBytesLongForHmacSha256";

    @Test
    void generatesAndParsesTokenForSubject() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);
        String token = provider.generateToken("user@example.com");

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getEmailFromToken(token)).isEqualTo("user@example.com");
    }

    @Test
    void rejectsTokenSignedByDifferentSecret() {
        JwtTokenProvider issuer = new JwtTokenProvider(SECRET, 60_000);
        JwtTokenProvider verifier = new JwtTokenProvider(SECRET + "different", 60_000);

        assertThat(verifier.validateToken(issuer.generateToken("user@example.com"))).isFalse();
    }

    @Test
    void rejectsMalformedToken() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);

        assertThat(provider.validateToken("not-a-jwt")).isFalse();
    }
}
