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

class GoogleClientTests {
    MockRestServiceServer server;
    GoogleClient google;

    @BeforeEach void setup() {
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        google = new GoogleClient("google-client", "server-only-secret", builder);
    }

    void token(String response) {
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("client_id=google-client&client_secret=server-only-secret&code=code&code_verifier=verifier&grant_type=authorization_code&redirect_uri=http%3A%2F%2Flocalhost%3A8081"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    }

    @Test void exchangesPkceAndUsesVerifiedGoogleIdentity() {
        token("{\"access_token\":\"google-access-token\"}");
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer google-access-token"))
                .andRespond(withSuccess("{\"sub\":\"google-42\",\"email\":\"diver@example.com\",\"email_verified\":true,\"name\":\"Diver\"}", MediaType.APPLICATION_JSON));

        assertEquals(new GoogleClient.Identity("google-42", "diver@example.com", "Diver"),
                google.authenticate("code", "verifier", "http://localhost:8081"));
        server.verify();
    }

    @Test void rejectsUnverifiedGoogleEmail() {
        token("{\"access_token\":\"google-access-token\"}");
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andRespond(withSuccess("{\"sub\":\"google-42\",\"email\":\"diver@example.com\",\"email_verified\":false,\"name\":\"Diver\"}", MediaType.APPLICATION_JSON));

        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> google.authenticate("code", "verifier", "http://localhost:8081")).getStatusCode().value());
        server.verify();
    }

    @Test void exchangesNativeCodeWithAndroidClientWithoutASecret() {
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        google = new GoogleClient("web-client", "web-secret", "android-client", "",
                "seaanimaldiscovery:/oauthredirect", builder);
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("client_id=android-client&code=code&code_verifier=verifier&grant_type=authorization_code&redirect_uri=seaanimaldiscovery%3A%2Foauthredirect"))
                .andRespond(withSuccess("{\"access_token\":\"native-token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andExpect(header("Authorization", "Bearer native-token"))
                .andRespond(withSuccess("{\"sub\":\"android-user\",\"email\":\"android@example.com\",\"email_verified\":true,\"name\":\"Android User\"}", MediaType.APPLICATION_JSON));

        assertEquals("android-user", google.authenticate("code", "verifier", "seaanimaldiscovery:/oauthredirect").id());
        server.verify();
    }

    @Test void rejectsInvalidCodeWithoutLeakingProviderError() {
        token("{\"error\":\"invalid_grant\",\"error_description\":\"private-details\"}");
        var error = assertThrows(ResponseStatusException.class,
                () -> google.authenticate("code", "verifier", "http://localhost:8081"));
        assertEquals(401, error.getStatusCode().value());
        assertFalse(error.getReason().contains("private-details"));
        server.verify();
    }

    @Test void translatesGoogleOutage() {
        server.expect(requestTo("https://oauth2.googleapis.com/token")).andRespond(withServerError());
        assertEquals(502, assertThrows(ResponseStatusException.class,
                () -> google.authenticate("code", "verifier", "http://localhost:8081")).getStatusCode().value());
    }
}
