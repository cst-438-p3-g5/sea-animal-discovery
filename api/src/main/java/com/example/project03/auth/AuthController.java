package com.example.project03.auth;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {
    private final GithubClient github;
    private final UserRepository users;
    private final JwtEncoder encoder;
    private final String redirectUri;

    public AuthController(GithubClient github, UserRepository users, JwtEncoder encoder,
                          @Value("${auth.github.redirect-uri}") String redirectUri) {
        this.github = github; this.users = users; this.encoder = encoder; this.redirectUri = redirectUri;
    }

    @PostMapping("/auth/github")
    public ResponseEntity<AuthResponse> signIn(@RequestBody CodeRequest request) {
        if (request.code() == null || request.code().isBlank() || request.code().length() > 1024
                || request.code_verifier() == null || !request.code_verifier().matches("[A-Za-z0-9._~-]{43,128}")
                || !redirectUri.equals(request.redirect_uri()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A code, PKCE verifier, and configured redirect_uri are required.");
        var user = users.signIn(github.authenticate(request.code(), request.code_verifier(), redirectUri));
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(AuthConfig.ISSUER).subject(user.id())
                .issuedAt(now).expiresAt(now.plusSeconds(3600)).build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new AuthResponse(token, "Bearer", user));
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserRepository.User> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(users.find(jwt.getSubject()));
    }

    public record CodeRequest(String code, String code_verifier, String redirect_uri) {}
    public record AuthResponse(String access_token, String token_type, UserRepository.User user) {}
}
