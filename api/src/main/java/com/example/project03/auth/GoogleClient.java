package com.example.project03.auth;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GoogleClient {
    private final RestClient client;
    private final String clientId;
    private final String clientSecret;
    private final String androidClientId;
    private final String androidClientSecret;
    private final String nativeRedirectUri;

    @org.springframework.beans.factory.annotation.Autowired
    public GoogleClient(@Value("${auth.google.client-id}") String clientId,
                        @Value("${auth.google.client-secret}") String clientSecret,
                        @Value("${auth.google.android-client-id}") String androidClientId,
                        @Value("${auth.google.android-client-secret}") String androidClientSecret,
                        @Value("${auth.google.native-redirect-uri}") String nativeRedirectUri) {
        this(clientId, clientSecret, androidClientId, androidClientSecret, nativeRedirectUri, builder());
    }

    // The short constructor keeps provider HTTP requests easy to test and is
    // also useful when only a web OAuth client is configured.
    GoogleClient(String clientId, String clientSecret, RestClient.Builder builder) {
        this(clientId, clientSecret, "", "", "seaanimaldiscovery:/oauthredirect", builder);
    }

    GoogleClient(String clientId, String clientSecret, String androidClientId,
                 String androidClientSecret, String nativeRedirectUri, RestClient.Builder builder) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.androidClientId = androidClientId;
        this.androidClientSecret = androidClientSecret;
        this.nativeRedirectUri = nativeRedirectUri;
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
        boolean nativeClient = nativeRedirectUri.equals(redirectUri);
        String selectedClientId = nativeClient ? androidClientId : clientId;
        String selectedClientSecret = nativeClient ? androidClientSecret : clientSecret;
        if (selectedClientId == null || selectedClientId.isBlank()
                || (!nativeClient && (selectedClientSecret == null || selectedClientSecret.isBlank()))) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google sign-in is not configured.");
        }

        var form = new LinkedMultiValueMap<String, String>();
        form.add("client_id", selectedClientId);
        if (selectedClientSecret != null && !selectedClientSecret.isBlank()) {
            form.add("client_secret", selectedClientSecret);
        }
        form.add("code", code);
        form.add("code_verifier", verifier);
        form.add("grant_type", "authorization_code");
        form.add("redirect_uri", redirectUri);
        try {
            Token token = client.post().uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(Token.class);
            if (token == null || token.access_token() == null || token.access_token().isBlank()) throw rejected();
            Profile profile = client.get().uri("https://openidconnect.googleapis.com/v1/userinfo")
                    .headers(h -> h.setBearerAuth(token.access_token())).retrieve().body(Profile.class);
            if (profile == null || profile.sub() == null || profile.sub().isBlank()
                    || profile.email() == null || profile.email().isBlank() || !profile.email_verified()) {
                throw rejected();
            }
            String name = profile.name() == null || profile.name().isBlank()
                    ? profile.email() : profile.name();
            return new Identity(profile.sub(), profile.email(), name);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) throw rejected();
            throw unavailable();
        } catch (RestClientException e) {
            throw unavailable();
        }
    }

    private static ResponseStatusException rejected() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Google sign-in requires a valid code and a verified email.");
    }

    private static ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Google is unavailable. Try signing in again.");
    }

    record Token(String access_token) {}
    record Profile(String sub, String email, boolean email_verified, String name) {}
    public record Identity(String id, String email, String displayName) {}
}
