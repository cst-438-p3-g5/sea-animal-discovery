package com.example.project03.auth;

import java.time.Duration;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GithubClient {
    private final RestClient client;
    private final String clientId;
    private final String clientSecret;

    @org.springframework.beans.factory.annotation.Autowired
    public GithubClient(@Value("${auth.github.client-id}") String clientId,
                        @Value("${auth.github.client-secret}") String clientSecret) {
        this(clientId, clientSecret, builder());
    }

    // Builder injection keeps provider HTTP requests testable without live credentials.
    GithubClient(String clientId, String clientSecret, RestClient.Builder builder) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        client = builder.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Tidepool").build();
    }

    private static RestClient.Builder builder() {
        var http = java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().requestFactory(factory);
    }

    public Identity authenticate(String code, String verifier, String redirectUri) {
        if (clientId.isBlank() || clientSecret.isBlank())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GitHub sign-in is not configured.");
        var form = new LinkedMultiValueMap<String, String>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);
        form.add("code_verifier", verifier);
        form.add("redirect_uri", redirectUri);
        try {
            Token token = client.post().uri("https://github.com/login/oauth/access_token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(Token.class);
            if (token == null || token.access_token() == null || token.access_token().isBlank()) throw rejected();
            Profile profile = client.get().uri("https://api.github.com/user")
                    .headers(h -> h.setBearerAuth(token.access_token())).retrieve().body(Profile.class);
            Email[] emails = client.get().uri("https://api.github.com/user/emails?per_page=100")
                    .headers(h -> h.setBearerAuth(token.access_token())).retrieve().body(Email[].class);
            if (profile == null || profile.id() <= 0 || profile.login() == null || emails == null) throw rejected();
            Email verified = Arrays.stream(emails).filter(e -> e.verified() && e.email() != null && !e.email().isBlank())
                    .sorted((a, b) -> Boolean.compare(b.primary(), a.primary())).findFirst().orElseThrow(GithubClient::rejected);
            String name = profile.name() == null || profile.name().isBlank() ? profile.login() : profile.name();
            return new Identity(Long.toString(profile.id()), verified.email(), name);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) throw rejected();
            throw unavailable();
        } catch (RestClientException e) {
            throw unavailable();
        }
    }

    private static ResponseStatusException rejected() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "GitHub sign-in requires a valid code and a verified email.");
    }
    private static ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub is unavailable. Try signing in again.");
    }
    record Token(String access_token) {}
    record Profile(long id, String login, String name) {}
    record Email(String email, boolean verified, boolean primary) {}
    public record Identity(String id, String email, String displayName) {}
}
