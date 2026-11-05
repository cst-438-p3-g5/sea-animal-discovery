package com.example.project03.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GithubClientTests {
    MockRestServiceServer server;
    GithubClient github;

    @BeforeEach void setup() {
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        github = new GithubClient("client", "server-only-secret", builder);
    }

    void token(String response) {
        server.expect(requestTo("https://github.com/login/oauth/access_token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("client_id=client&client_secret=server-only-secret&code=code&code_verifier=verifier&redirect_uri=http%3A%2F%2Flocalhost%3A8081"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    }

    void profileAndEmails(String emails) {
        server.expect(requestTo("https://api.github.com/user"))
                .andExpect(header("Authorization", "Bearer provider-token"))
                .andRespond(withSuccess("{\"id\":42,\"login\":\"diver\",\"name\":null,\"email\":\"untrusted@example.com\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.github.com/user/emails?per_page=100"))
                .andExpect(header("Authorization", "Bearer provider-token"))
                .andRespond(withSuccess(emails, MediaType.APPLICATION_JSON));
    }

    @Test void exchangesPkceAndUsesVerifiedPrimaryEmail() {
        token("{\"access_token\":\"provider-token\"}");
        profileAndEmails("""
                [{"email":"secondary@example.com","verified":true,"primary":false},
                 {"email":"primary@example.com","verified":true,"primary":true}]
                """);
        var identity = github.authenticate("code", "verifier", "http://localhost:8081");
        assertEquals(new GithubClient.Identity("42", "primary@example.com", "diver"), identity);
        server.verify();
    }

    @Test void acceptsVerifiedSecondaryWhenPrimaryIsUnverified() {
        token("{\"access_token\":\"provider-token\"}");
        profileAndEmails("""
                [{"email":"bad@example.com","verified":false,"primary":true},
                 {"email":"verified@example.com","verified":true,"primary":false}]
                """);
        assertEquals("verified@example.com", github.authenticate("code", "verifier", "http://localhost:8081").email());
        server.verify();
    }

    @Test void rejectsUnverifiedEmailEvenWhenProfileHasEmail() {
        token("{\"access_token\":\"provider-token\"}");
        profileAndEmails("[{\"email\":\"bad@example.com\",\"verified\":false,\"primary\":true}]");
        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> github.authenticate("code", "verifier", "http://localhost:8081")).getStatusCode().value());
        server.verify();
    }

    @Test void rejectsInvalidOrReplayedCodeWithoutLeakingProviderError() {
        token("{\"error\":\"bad_verification_code\",\"error_description\":\"private-details\"}");
        var error = assertThrows(ResponseStatusException.class,
                () -> github.authenticate("code", "verifier", "http://localhost:8081"));
        assertEquals(401, error.getStatusCode().value());
        assertFalse(error.getReason().contains("private-details"));
        server.verify();
    }

    @Test void translatesProviderOutage() {
        server.expect(requestTo("https://github.com/login/oauth/access_token")).andRespond(withServerError());
        assertEquals(502, assertThrows(ResponseStatusException.class,
                () -> github.authenticate("code", "verifier", "http://localhost:8081")).getStatusCode().value());
    }
}
