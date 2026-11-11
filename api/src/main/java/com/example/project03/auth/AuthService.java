package com.example.project03.auth;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final GithubClient github;
    private final UserRepository users;
    private final JwtEncoder encoder;
    private final String redirectUri;

    public AuthService(GithubClient github, UserRepository users, JwtEncoder encoder,
                       @Value("${auth.github.redirect-uri}") String redirectUri) {
        this.github = github;
        this.users = users;
        this.encoder = encoder;
        this.redirectUri = redirectUri;
    }

    public SignInResponse signIn(String code, String verifier, String requestedRedirectUri) {
        if (code == null || code.isBlank() || code.length() > 1024
                || verifier == null || !verifier.matches("[A-Za-z0-9._~-]{43,128}")
                || !redirectUri.equals(requestedRedirectUri)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A code, PKCE verifier, and configured redirect_uri are required.");
        }

        var user = users.signIn(github.authenticate(code, verifier, redirectUri));
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(AuthConfig.ISSUER).subject(user.id())
                .issuedAt(now).expiresAt(now.plusSeconds(3600)).build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new SignInResponse(token, "Bearer", user);
    }

    public record SignInResponse(String access_token, String token_type, UserRepository.User user) {}
}
