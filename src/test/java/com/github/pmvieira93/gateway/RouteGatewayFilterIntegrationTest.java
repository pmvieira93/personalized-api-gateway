package com.github.pmvieira93.gateway;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import io.restassured.response.ValidatableResponse;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RouteGatewayFilterIntegrationTest {

    @LocalServerPort
    int LOCAL_SERVER_PORT;

    static final String RESPONSE_PATTERN = ".+(([0-9]{1,3}\\.?){4},? ?)+.+";

    static final String BASE_URL = "http://localhost";

    @Nested
    class ConfigVersion1 {

        @Test
        void givenARouteWithFilter_whenRequestingRoute_thenFilterIsApplied() {
            // Given
            System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);
            Pattern pattern = Pattern.compile(RESPONSE_PATTERN, Pattern.MULTILINE | Pattern.COMMENTS | Pattern.DOTALL);

            // When
            ValidatableResponse response = given()
                    .baseUri(BASE_URL)
                    .port(LOCAL_SERVER_PORT)
                    .when()
                    .header("ApiVersion", "4.5.0")
                    .get("/v1/ip")
                    .then()
                    .statusCode(200);

            // Then
            assertNotNull(response);
            String body = response.extract().body().asString();
            assertNotNull(body);
            System.out.println(body);
            assertTrue(pattern.matcher(body).matches());
        }

        @Test
        void givenARouteWithFilter_whenRequestingRouteWithDefaultHeader_thenFilterIsApplied() {
            // Given
            System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);
            Pattern pattern = Pattern.compile(RESPONSE_PATTERN, Pattern.MULTILINE | Pattern.COMMENTS | Pattern.DOTALL);

            // When
            ValidatableResponse response = given()
                    .baseUri(BASE_URL)
                    .port(LOCAL_SERVER_PORT)
                    .when()
                    .header("api-version", "4.5.0")
                    .get("/v1/ip")
                    .then()
                    .statusCode(200);

            // Then
            assertNotNull(response);
            String body = response.extract().body().asString();
            assertNotNull(body);
            System.out.println(body);
            assertTrue(pattern.matcher(body).matches());
        }

        @Test
        void givenARouteWithFilter_whenRequestingRouteWithInvalidVersion_thenReturn403() {
            // Given
            System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);

            // When
            ValidatableResponse response = given()
                    .baseUri(BASE_URL)
                    .port(LOCAL_SERVER_PORT)
                    .when()
                    .header("ApiVersion", "1.5.0")
                    .get("/v1/ip")
                    .then()
                    .statusCode(403);

            // Then
            assertNotNull(response);
        }

        @Test
        void givenARouteWithFilter_whenRequestingRouteWithUnknownHeader_thenReturn404() {
            // Given
            System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);

            // When
            ValidatableResponse response = given()
                    .baseUri(BASE_URL)
                    .port(LOCAL_SERVER_PORT)
                    .when()
                    .header("xyz", "1.5.0")
                    .get("/v1/ip")
                    .then()
                    .statusCode(404);

            // Then
            assertNotNull(response);
        }
    }

    @Nested
    class ConfigVersion2 {
        
        @Test
        void givenARouteWithFilter_whenRequestingRoute_thenFilterIsApplied() {
            // Given
            System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);
            Pattern pattern = Pattern.compile(RESPONSE_PATTERN, Pattern.MULTILINE | Pattern.COMMENTS | Pattern.DOTALL);

            // When
            ValidatableResponse response = given()
                    .baseUri(BASE_URL)
                    .port(LOCAL_SERVER_PORT)
                    .when()
                    .header("X-Api-Version", "4.5.0")
                    .get("/v2/ip")
                    .then()
                    .statusCode(200);

            // Then
            assertNotNull(response);
            String body = response.extract().body().asString();
            assertNotNull(body);
            System.out.println(body);
            assertTrue(pattern.matcher(body).matches());
        }

        @Test
        void givenARouteWithFilter_whenRequestingRouteWithDefaultHeader_thenFilterIsApplied() {
            // Given
            System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);
            Pattern pattern = Pattern.compile(RESPONSE_PATTERN, Pattern.MULTILINE | Pattern.COMMENTS | Pattern.DOTALL);

            // When
            ValidatableResponse response = given()
                    .baseUri(BASE_URL)
                    .port(LOCAL_SERVER_PORT)
                    .when()
                    .header("api-version", "4.5.0")
                    .get("/v2/ip")
                    .then()
                    .statusCode(200);

            // Then
            assertNotNull(response);
            String body = response.extract().body().asString();
            assertNotNull(body);
            System.out.println(body);
            assertTrue(pattern.matcher(body).matches());
        }

        @Test
        void givenARouteWithFilter_whenRequestingRouteWithInvalidVersion_thenReturn403() {
            // Given
            System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);

            // When
            ValidatableResponse response = given()
                    .baseUri(BASE_URL)
                    .port(LOCAL_SERVER_PORT)
                    .when()
                    .header("X-Api-Version", "1.5.0")
                    .get("/v2/ip")
                    .then()
                    .statusCode(403);

            // Then
            assertNotNull(response);
        }

        @Test
        void givenARouteWithFilter_whenRequestingRouteWithUnknownHeader_thenReturn404() {
            // Given
            System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);

            // When
            ValidatableResponse response = given()
                    .baseUri(BASE_URL)
                    .port(LOCAL_SERVER_PORT)
                    .when()
                    .header("xyz", "1.5.0")
                    .get("/v2/ip")
                    .then()
                    .statusCode(404);

            // Then
            assertNotNull(response);
        }
    }

}
