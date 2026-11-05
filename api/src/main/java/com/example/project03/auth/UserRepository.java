package com.example.project03.auth;

import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Repository
public class UserRepository {
    private final JdbcClient jdbc;
    public UserRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    public User signIn(GithubClient.Identity identity) {
        // Provider ID is stable across email/username changes. Never link accounts by email.
        return jdbc.sql("""
                INSERT INTO app_user (id, github_id, email, display_name) VALUES (:id, :github, :email, :name)
                ON CONFLICT (github_id) DO UPDATE SET email = EXCLUDED.email
                RETURNING id, email, display_name
                """).param("id", UUID.randomUUID().toString()).param("github", identity.id())
                .param("email", identity.email()).param("name", identity.displayName()).query(User.class).single();
    }

    public User find(String id) {
        return jdbc.sql("SELECT id, email, display_name FROM app_user WHERE id = :id").param("id", id)
                .query(User.class).optional().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists."));
    }

    public record User(String id, String email, String display_name) {}
}
