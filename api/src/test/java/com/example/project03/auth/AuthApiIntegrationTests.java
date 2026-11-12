package com.example.project03.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthApiIntegrationTests {
    private static final String REDIRECT_URI = "http://localhost:8081";
    private static final String VERIFIER = "v".repeat(43);

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcClient jdbc;

    @MockitoBean
    private GithubClient github;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
        properties.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @BeforeEach
    void resetDatabaseAndProviderMock() {
        jdbc.sql("DELETE FROM app_user").update();
        reset(github);
    }

    @Test
    void signsInAndReadsPersistedUserFromPostgres() {
        var identity = new GithubClient.Identity("integration-user-1", "diver@example.com", "Diver");
        when(github.authenticate("valid-code", VERIFIER, REDIRECT_URI)).thenReturn(identity);

        var response = given().port(port).contentType(ContentType.JSON)
                .body(signInBody("valid-code", VERIFIER, REDIRECT_URI))
                .when().post("/auth/github")
                .then().statusCode(200)
                .body("token_type", equalTo("Bearer"))
                .body("user.email", equalTo(identity.email()))
                .extract().response();

        String token = response.jsonPath().getString("access_token");
        given().port(port).accept(ContentType.JSON).contentType(ContentType.JSON).auth().oauth2(token)
                .when().get("/users/me")
                .then().statusCode(200)
                .body("id", equalTo(response.jsonPath().getString("user.id")))
                .body("email", equalTo(identity.email()));

        Long savedUsers = jdbc.sql("SELECT COUNT(*) FROM app_user WHERE github_id = :githubId")
                .param("githubId", identity.id()).query(Long.class).single();
        org.junit.jupiter.api.Assertions.assertEquals(1L, savedUsers);
    }

    @Test
    void returnsBadRequestForInvalidPkceVerifier() {
        given().port(port).contentType(ContentType.JSON)
                .body(signInBody("valid-code", "short", REDIRECT_URI))
                .when().post("/auth/github")
                .then().statusCode(400);
    }

    @Test
    void rejectsUnverifiedProviderEmailWithoutCreatingAnAccount() {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "GitHub sign-in requires a valid code and a verified email."))
                .when(github).authenticate("unverified-code", VERIFIER, REDIRECT_URI);

        given().port(port).contentType(ContentType.JSON)
                .body(signInBody("unverified-code", VERIFIER, REDIRECT_URI))
                .when().post("/auth/github")
                .then().statusCode(401)
                .body("detail", containsString("verified email"));

        Long savedUsers = jdbc.sql("SELECT COUNT(*) FROM app_user").query(Long.class).single();
        org.junit.jupiter.api.Assertions.assertEquals(0L, savedUsers);
    }

    @Test
    void returnsConflictAndProviderWhenEmailBelongsToAnotherProvider() {
        jdbc.sql("""
                INSERT INTO app_user (id, provider, provider_id, email, display_name)
                VALUES ('google-user', 'google', 'google-1', 'shared@example.com', 'Google User')
                """).update();
        var identity = new GithubClient.Identity("github-1", "shared@example.com", "GitHub User");
        when(github.authenticate("duplicate-code", VERIFIER, REDIRECT_URI)).thenReturn(identity);

        given().port(port).contentType(ContentType.JSON)
                .body(signInBody("duplicate-code", VERIFIER, REDIRECT_URI))
                .when().post("/auth/github")
                .then().statusCode(409)
                .body("detail", equalTo("This email is already registered with Google."));

        Long savedUsers = jdbc.sql("SELECT COUNT(*) FROM app_user").query(Long.class).single();
        org.junit.jupiter.api.Assertions.assertEquals(1L, savedUsers);
    }

    @Test
    void returnsUnauthorizedForProfileWithoutBearerToken() {
        given().port(port).accept(ContentType.JSON).contentType(ContentType.JSON)
                .when().get("/users/me")
                .then().statusCode(401);
    }

    @Test
    void rejectsPreflightFromUnapprovedOrigin() {
        given().port(port).accept(ContentType.JSON).contentType(ContentType.JSON)
                .header("Origin", "https://attacker.example")
                .header("Access-Control-Request-Method", "POST")
                .when().options("/auth/github")
                .then().statusCode(403);
    }

    @Test
    void returnsNotFoundForUnknownRouteToAuthenticatedUser() {
        var identity = new GithubClient.Identity("integration-user-404", "notfound@example.com", "Not Found");
        when(github.authenticate("valid-code", VERIFIER, REDIRECT_URI)).thenReturn(identity);
        String token = given().port(port).contentType(ContentType.JSON)
                .body(signInBody("valid-code", VERIFIER, REDIRECT_URI))
                .when().post("/auth/github")
                .then().statusCode(200)
                .extract().path("access_token");

        given().port(port).accept(ContentType.JSON).contentType(ContentType.JSON).auth().oauth2(token)
                .when().get("/route-that-does-not-exist")
                .then().statusCode(404);
    }

    private static String signInBody(String code, String verifier, String redirectUri) {
        return "{\"code\":\"" + code + "\",\"code_verifier\":\"" + verifier
                + "\",\"redirect_uri\":\"" + redirectUri + "\"}";
    }
}
