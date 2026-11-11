package com.example.project03.auth;

import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    private final AuthService auth;
    private final UserRepository users;

    public AuthController(AuthService auth, UserRepository users) {
        this.auth = auth;
        this.users = users;
    }

    @PostMapping("/auth/github")
    public ResponseEntity<AuthService.SignInResponse> signIn(@RequestBody CodeRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(auth.signIn(request.code(), request.code_verifier(), request.redirect_uri()));
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserRepository.User> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(users.find(jwt.getSubject()));
    }

    public record CodeRequest(String code, String code_verifier, String redirect_uri) {}
}
