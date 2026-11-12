package com.example.project03.auth;

import java.util.UUID;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Repository
public class UserRepository {
    private final JdbcClient jdbc;
    public UserRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    public User signIn(GithubClient.Identity identity) {
        return signIn("github", identity.id(), identity.email(), identity.displayName());
    }

    /**
     * Upserts an OAuth identity without merging accounts by email. The provider
     * and provider ID are stable, while the verified email may change.
     */
    public User signIn(String provider, String providerId, String email, String displayName) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        var existingProvider = jdbc.sql("SELECT provider FROM app_user WHERE email = :email")
                .param("email", normalizedEmail).query(String.class).optional();
        if (existingProvider.isPresent() && !provider.equals(existingProvider.get())) {
            throw duplicateProvider(existingProvider.get());
        }

        try {
            return jdbc.sql("""
                    INSERT INTO app_user (id, github_id, provider, provider_id, email, display_name)
                    VALUES (:id, :github, :provider, :providerId, :email, :name)
                    ON CONFLICT (provider, provider_id) DO UPDATE
                    SET email = EXCLUDED.email, display_name = EXCLUDED.display_name
                    RETURNING id, email, display_name
                    """).param("id", UUID.randomUUID().toString())
                    .param("github", "github".equals(provider) ? providerId : null)
                    .param("provider", provider).param("providerId", providerId)
                    .param("email", normalizedEmail).param("name", displayName)
                    .query(User.class).single();
        } catch (DuplicateKeyException error) {
            // A concurrent sign-in can pass the read above before the other
            // provider commits. Re-read the owner and return the same 409.
            var owner = jdbc.sql("SELECT provider FROM app_user WHERE email = :email")
                    .param("email", normalizedEmail).query(String.class).optional();
            if (owner.isPresent() && !provider.equals(owner.get())) {
                throw duplicateProvider(owner.get());
            }
            throw error;
        }
    }

    private static ResponseStatusException duplicateProvider(String provider) {
        String label = switch (provider.toLowerCase(Locale.ROOT)) {
            case "github" -> "GitHub";
            case "google" -> "Google";
            default -> provider;
        };
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "This email is already registered with " + label + ".");
    }

    public User find(String id) {
        return jdbc.sql("SELECT id, email, display_name FROM app_user WHERE id = :id").param("id", id)
                .query(User.class).optional().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists."));
    }

    public record User(String id, String email, String display_name) {}
}
