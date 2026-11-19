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
    private final GoogleClient google;
    private final UserRepository users;
    private final JwtEncoder encoder;
    private final String githubRedirectUri;
    private final String googleRedirectUris;
    private final String googleNativeRedirectUri;

    public AuthService(GithubClient github, GoogleClient google, UserRepository users, JwtEncoder encoder,
                       @Value("${auth.github.redirect-uri}") String githubRedirectUri,
                       @Value("${auth.google.redirect-uri}") String googleRedirectUris,
                       @Value("${auth.google.native-redirect-uri}") String googleNativeRedirectUri) {
        this.github = github;
        this.google = google;
        this.users = users;
        this.encoder = encoder;
        this.githubRedirectUri = githubRedirectUri;
        this.googleRedirectUris = googleRedirectUris;
        this.googleNativeRedirectUri = googleNativeRedirectUri;
    }

    public SignInResponse signIn(String code, String verifier, String requestedRedirectUri) {
        validateRequest(code, verifier, requestedRedirectUri, githubRedirectUri);
        var user = users.signIn(github.authenticate(code, verifier, githubRedirectUri));
        return issueToken(user);
    }

    public SignInResponse signInGoogle(String code, String verifier, String requestedRedirectUri) {
        validateGoogleRequest(code, verifier, requestedRedirectUri);
        var identity = google.authenticate(code, verifier, requestedRedirectUri);
        var user = users.signIn("google", identity.id(), identity.email(), identity.displayName());
        return issueToken(user);
    }

    private void validateGoogleRequest(String code, String verifier, String requestedRedirectUri) {
        boolean allowedRedirect = googleNativeRedirectUri.equals(requestedRedirectUri)
                || googleRedirectUris.lines()
                .flatMap(value -> java.util.Arrays.stream(value.split(",")))
                .map(String::trim).anyMatch(value -> value.equals(requestedRedirectUri));
        validateRequest(code, verifier, requestedRedirectUri, allowedRedirect);
    }

    private static void validateRequest(String code, String verifier, String requestedRedirectUri,
                                        String configuredRedirectUri) {
        validateRequest(code, verifier, requestedRedirectUri,
                configuredRedirectUri != null && configuredRedirectUri.equals(requestedRedirectUri));
    }

    private static void validateRequest(String code, String verifier, String requestedRedirectUri,
                                        boolean allowedRedirect) {
        if (code == null || code.isBlank() || code.length() > 1024
                || verifier == null || !verifier.matches("[A-Za-z0-9._~-]{43,128}")
                || !allowedRedirect) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A code, PKCE verifier, and configured redirect_uri are required.");
        }
    }

    private SignInResponse issueToken(UserRepository.User user) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(AuthConfig.ISSUER).subject(user.id())
                .issuedAt(now).expiresAt(now.plusSeconds(3600)).build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new SignInResponse(token, "Bearer", user);
    }

    public record SignInResponse(String access_token, String token_type, UserRepository.User user) {}
}
