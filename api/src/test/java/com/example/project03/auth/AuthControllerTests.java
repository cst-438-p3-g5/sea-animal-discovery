package com.example.project03.auth;

import java.time.Instant;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests {
    @Autowired MockMvc mvc;
    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;
    @MockitoBean GithubClient github;
    @MockitoBean UserRepository users;
    static final String VERIFIER = "v".repeat(43);
    static final GithubClient.Identity IDENTITY = new GithubClient.Identity("42", "diver@example.com", "Diver");
    static final UserRepository.User USER = new UserRepository.User("app-user-id", "diver@example.com", "Diver");

    String body(String verifier, String redirect) {
        return "{\"code\":\"code\",\"code_verifier\":\"" + verifier + "\",\"redirect_uri\":\"" + redirect + "\"}";
    }

    @Test void signInIssuesAppJwtThatAuthenticatesProfile() throws Exception {
        when(github.authenticate("code", VERIFIER, "http://localhost:8081")).thenReturn(IDENTITY);
        when(users.signIn(IDENTITY)).thenReturn(USER);
        when(users.find(USER.id())).thenReturn(USER);
        String response = mvc.perform(post("/auth/github").contentType(MediaType.APPLICATION_JSON)
                        .content(body(VERIFIER, "http://localhost:8081")))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(USER.email()))
                .andReturn().getResponse().getContentAsString();
        String token = response.split("\"access_token\":\"")[1].split("\"")[0];
        Jwt jwt = decoder.decode(token);
        assertEquals(USER.id(), jwt.getSubject());
        assertEquals(3600, jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
        assertFalse(response.contains("provider-token"));
        mvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(USER.id()));
        // Tampering with the signature cannot authenticate.
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "a".repeat(43);
        mvc.perform(get("/users/me").header("Authorization", "Bearer " + tampered)).andExpect(status().isUnauthorized());
    }

    @Test void rejectsBadVerifierAndUnapprovedRedirectBeforeProviderCall() throws Exception {
        mvc.perform(post("/auth/github").contentType(MediaType.APPLICATION_JSON).content(body("short", "http://localhost:8081")))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        mvc.perform(post("/auth/github").contentType(MediaType.APPLICATION_JSON).content(body(VERIFIER, "https://attacker.example")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/github").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(github, users);
    }

    @Test void rejectsMissingMalformedExpiredAndWrongIssuerJwt() throws Exception {
        mvc.perform(get("/users/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/users/me").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
        for (String token : new String[]{signed(AuthConfig.ISSUER, Instant.now().minusSeconds(120)), signed("wrong-issuer", Instant.now().plusSeconds(3600))}) {
            mvc.perform(get("/users/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(users);
    }

    String signed(String issuer, Instant expires) {
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().issuer(issuer).subject(USER.id()).issuedAt(expires.minusSeconds(3600)).expiresAt(expires).build())).getTokenValue();
    }

    @Test void allowsConfiguredOriginPreflightOnly() throws Exception {
        mvc.perform(options("/auth/github").header("Origin", "http://localhost:8081")
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
        mvc.perform(options("/auth/github").header("Origin", "https://attacker.example")
                .header("Access-Control-Request-Method", "POST")).andExpect(status().isForbidden());
    }

    @Test void rejectedProviderIdentityCannotCreateUserOrIssueToken() throws Exception {
        when(github.authenticate("code", VERIFIER, "http://localhost:8081"))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "A verified GitHub email is required."));
        mvc.perform(post("/auth/github").contentType(MediaType.APPLICATION_JSON)
                .content(body(VERIFIER, "http://localhost:8081")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.access_token").doesNotExist());
        verifyNoInteractions(users);
    }

    @Test void requiresStrongSigningKey() {
        assertThrows(IllegalStateException.class, () -> new AuthConfig().jwtKey(""));
        assertThrows(IllegalStateException.class, () -> new AuthConfig().jwtKey("too-short"));
    }
}
