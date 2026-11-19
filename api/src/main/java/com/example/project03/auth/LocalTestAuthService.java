package com.example.project03.auth;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Checks the local seeded account before issuing an app JWT. */
@Service
@Profile("local")
public class LocalTestAuthService {
    private static final String INVALID_CREDENTIALS = "Invalid username or password.";

    private final JdbcClient jdbc;
    private final AuthService auth;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LocalTestAuthService(JdbcClient jdbc, AuthService auth) {
        this.jdbc = jdbc;
        this.auth = auth;
    }

    public AuthService.SignInResponse signIn(String username, String password) {
        if (username == null || username.isBlank() || password == null
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw invalidCredentials();
        }

        var credential = jdbc.sql("""
                SELECT user_id, password_hash
                FROM local_test_credential
                WHERE username = :username
                """).param("username", username.trim()).query(TestCredential.class).optional();

        if (credential.isEmpty() || !passwordEncoder.matches(password, credential.get().password_hash())) {
            throw invalidCredentials();
        }
        return auth.signInAsTestUser(credential.get().user_id());
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
    }

    private record TestCredential(String user_id, String password_hash) {}
}
