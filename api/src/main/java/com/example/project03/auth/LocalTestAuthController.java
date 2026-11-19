package com.example.project03.auth;

import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Local-only sign-in for the seeded demo account. */
@RestController
@Profile("local")
public class LocalTestAuthController {
    private final LocalTestAuthService auth;

    public LocalTestAuthController(LocalTestAuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/auth/test")
    public ResponseEntity<AuthService.SignInResponse> signInAsTestUser(@RequestBody Credentials request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(auth.signIn(request.username(), request.password()));
    }

    public record Credentials(String username, String password) {}
}
