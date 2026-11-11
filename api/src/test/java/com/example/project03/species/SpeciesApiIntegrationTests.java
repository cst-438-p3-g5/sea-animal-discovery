package com.example.project03.species;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Species catalog against real Postgres, seeded by schema.sql and data.sql. No sign-in needed. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class SpeciesApiIntegrationTests {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
        properties.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Test
    void listsTheSeededCatalogSortedByNameWithoutSignIn() {
        given().port(port).accept(ContentType.JSON)
                .when().get("/species")
                .then().statusCode(200)
                .body("$", hasSize(10))
                .body("[0].id", equalTo("sp-003"))
                .body("[0].common_name", equalTo("Aggregating anemone"));
    }

    @Test
    void returnsAFullSpeciesProfile() {
        given().port(port).accept(ContentType.JSON)
                .when().get("/species/sp-001")
                .then().statusCode(200)
                .body("id", equalTo("sp-001"))
                .body("common_name", equalTo("Ochre sea star"))
                .body("identification_details", not(emptyOrNullString()))
                .body("habitat", not(emptyOrNullString()))
                .body("conservation_information", not(emptyOrNullString()));
    }

    @Test
    void unknownSpeciesReturnsNotFoundProblemDetails() {
        given().port(port)
                .when().get("/species/sp-999")
                .then().statusCode(404)
                .contentType(containsString("application/problem+json"))
                .body("type", equalTo("/problems/not-found"))
                .body("title", equalTo("Not found"))
                .body("status", equalTo(404))
                .body("detail", equalTo("No species with id sp-999."))
                .body("instance", equalTo("/species/sp-999"));
    }
}
