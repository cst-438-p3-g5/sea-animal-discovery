package com.example.project03.sighting;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Map;
import io.restassured.http.ContentType;
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
import com.example.project03.auth.GithubClient;

/** Verifies sighting creation and database relationships against PostgreSQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class SightingApiIntegrationTests {
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
        jdbc.sql("DELETE FROM sighting").update();
        jdbc.sql("DELETE FROM app_user").update();
        org.mockito.Mockito.reset(github);
    }

    @Test
    void createsSightingLinkedToSignedInUserAndSelectedSpecies() {
        String token = signIn("sighting-user-1", "Diver");
        LocalDate date = LocalDate.now().minusDays(1);

        var response = given().port(port).auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("species_id", "sp-001", "sighting_date", date.toString()))
                .when().post("/sightings")
                .then().statusCode(201)
                .body("species_id", equalTo("sp-001"))
                .body("sighting_date", equalTo(date.toString()))
                .extract().response();

        String sightingId = response.jsonPath().getString("id");
        String userId = response.jsonPath().getString("user_id");
        String savedSpeciesId = jdbc.sql("SELECT species_id FROM sighting WHERE id = :id")
                .param("id", sightingId).query(String.class).single();
        String savedUserId = jdbc.sql("SELECT user_id FROM sighting WHERE id = :id")
                .param("id", sightingId).query(String.class).single();
        assertEquals("sp-001", savedSpeciesId);
        assertEquals(userId, savedUserId);
    }

    @Test
    void rejectsFutureSightingDate() {
        String token = signIn("sighting-user-2", "Diver Two");
        given().port(port).auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("species_id", "sp-001", "sighting_date", LocalDate.now().plusDays(1).toString()))
                .when().post("/sightings")
                .then().statusCode(400);
    }

    @Test
    void requiresAuthenticationToCreateSighting() {
        given().port(port).contentType(ContentType.JSON)
                .body(Map.of("species_id", "sp-001", "sighting_date", LocalDate.now().toString()))
                .when().post("/sightings")
                .then().statusCode(401);
    }

    @Test
    void rejectsUnknownSpecies() {
        String token = signIn("sighting-user-3", "Diver Three");
        given().port(port).auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("species_id", "sp-999", "sighting_date", LocalDate.now().toString()))
                .when().post("/sightings")
                .then().statusCode(404);
    }

    private String signIn(String githubId, String name) {
        var identity = new GithubClient.Identity(githubId, githubId + "@example.com", name);
        org.mockito.Mockito.when(github.authenticate("valid-code", VERIFIER, REDIRECT_URI)).thenReturn(identity);
        return given().port(port).contentType(ContentType.JSON)
                .body(Map.of("code", "valid-code", "code_verifier", VERIFIER, "redirect_uri", REDIRECT_URI))
                .when().post("/auth/github")
                .then().statusCode(200)
                .extract().path("access_token");
    }
}
