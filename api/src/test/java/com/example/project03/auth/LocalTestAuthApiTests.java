package com.example.project03.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class LocalTestAuthApiTests {
    @LocalServerPort
    private int port;

    @Test
    void seededCredentialsReturnTokenThatAuthenticatesProfile() {
        var response = given().port(port).contentType(ContentType.JSON)
                .body("{\"username\":\"testuser\",\"password\":\"tidepool123\"}")
                .when().post("/auth/test")
                .then().statusCode(200)
                .body("token_type", equalTo("Bearer"))
                .body("user.id", equalTo("local-test-user"))
                .body("user.email", equalTo("demo@sea-animal-discovery.local"))
                .extract().response();

        String token = response.jsonPath().getString("access_token");
        given().port(port).accept(ContentType.JSON).auth().oauth2(token)
                .when().get("/users/me")
                .then().statusCode(200)
                .body("id", equalTo("local-test-user"))
                .body("email", equalTo("demo@sea-animal-discovery.local"));
    }

    @Test
    void invalidSeededCredentialsAreRejected() {
        given().port(port).contentType(ContentType.JSON)
                .body("{\"username\":\"testuser\",\"password\":\"wrong-password\"}")
                .when().post("/auth/test")
                .then().statusCode(401)
                .body("detail", equalTo("Invalid username or password."));
    }
}
