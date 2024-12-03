package com.github.pmvieira93.gateway.infrastructure.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pmvieira93.gateway.DockerComposeUtils;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientRelationshipCondition;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.CreateStoreRequest;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Profile("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResourcePolicyFilterIntegrationTest {

    static final String RESPONSE_PATTERN = ".+(([0-9]{1,3}\\.?){4},? ?)+.+";
    static final String BASE_URL = "http://localhost";
    static final String FGA_STORE = "integration-test";
    static final String FGA_MODEL = "{\"schema_version\":\"1.1\",\"type_definitions\":[{\"type\":\"user\",\"relations\":{},\"metadata\":null},{\"type\":\"resource\",\"relations\":{\"get\":{\"this\":{}},\"post\":{\"this\":{}}},\"metadata\":{\"relations\":{\"get\":{\"directly_related_user_types\":[{\"type\":\"user\"},{\"type\":\"user\",\"condition\":\"ttl\"}]},\"post\":{\"directly_related_user_types\":[{\"type\":\"user\"}]}}}}],\"conditions\":{\"ttl\":{\"name\":\"ttl\",\"expression\":\"current <= expiry\",\"parameters\":{\"current\":{\"type_name\":\"TYPE_NAME_UINT\"},\"expiry\":{\"type_name\":\"TYPE_NAME_UINT\"}}}}}";
    static final Map<String, String> TOKEN = Map.of(
            "unknown","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJqd3Qtd2ViIiwic3ViIjoiMTIzNDU2Nzg5MCIsImF1ZCI6ImFwaS1nYXRld2F5IiwibmFtZSI6ImZvbyIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNzYzNzE1ODkxLCJqdGkiOiI5NmM3ZmMyNi1kNjQwLTRmZGYtODlmNi1hYjRkYmJiNmNlYTQifQ.trSAHOMPsvCCpc6AL5GSB7o7I2NLzVd1yEOnnpI0tAM",
            "free","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJqd3Qtd2ViIiwic3ViIjoiMTIzNDU2Nzg5MCIsInNpZ24iOiJmcmVlIiwiYXVkIjoiYXBpLWdhdGV3YXkiLCJuYW1lIjoiZm9vIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE3NjM3MTU4OTEsImp0aSI6Ijk2YzdmYzI2LWQ2NDAtNGZkZi04OWY2LWFiNGRiYmI2Y2VhNCJ9.7PI33Vcjv8u5vK6X6iJ-Gp4b4AXNZjcUvFMRZaiHUiQ",
            "premium","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJqd3Qtd2ViIiwic3ViIjoiYmFyMTIzIiwic2lnbiI6InByZW1pdW0iLCJhdWQiOiJhcGktZ2F0ZXdheSIsIm5hbWUiOiJiYXIiLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTc2MzcxNTg5MSwianRpIjoiOTZjN2ZjMjYtZDY0MC00ZmRmLTg5ZjYtYWI0ZGJiYjZjZWE0In0.0WilbbQQee8uKpra5yF2mOvuOJbVrez6GTOBCE-m_MU",
            "guest","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJqd3Qtd2ViIiwic3ViIjoiam9objkwOSIsInNpZ24iOiJndWVzdCIsImF1ZCI6ImFwaS1nYXRld2F5IiwibmFtZSI6ImpvaG4iLCJvbmJlaGFsZm9mIjoiYmFyMTIzIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE3NjM3MTU4OTEsImp0aSI6Ijk2YzdmYzI2LWQ2NDAtNGZkZi04OWY2LWFiNGRiYmI2Y2VhNCJ9.f08hYjB7mjOUG_y4n4pakwyRagOBqthRplIo9tkJTPc"
    );

    static final Pattern pattern = Pattern.compile(RESPONSE_PATTERN, Pattern.MULTILINE | Pattern.COMMENTS | Pattern.DOTALL);
    static ProcessBuilder builder = new ProcessBuilder();

    String storeId;
    String modelId;

    @LocalServerPort
    int LOCAL_SERVER_PORT;

    @Autowired
    OpenFgaClient fgaClient;

    @Autowired
    ClientConfiguration fgaConfig;

    @BeforeAll
    static void beforeAll() {
        DockerComposeUtils.up();
    }

    @BeforeEach
    void beforeEach() {
        //ReflectionTestUtils.setField(openFgaConfig,"apiUrl", openfga.getHttpEndpoint());
        try {
            var mapper = new ObjectMapper().findAndRegisterModules();
            var storeResponse = fgaClient.createStore(new CreateStoreRequest().name(FGA_STORE)).get();
            storeId = storeResponse.getId();
            fgaClient.setStoreId(storeId);

            var modelResponse = fgaClient.writeAuthorizationModel(mapper.readValue(FGA_MODEL, WriteAuthorizationModelRequest.class)).get();
            modelId = modelResponse.getAuthorizationModelId();
            fgaClient.setAuthorizationModelId(modelId);

        } catch (FgaInvalidParameterException | InterruptedException | ExecutionException |
                 JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @AfterEach
    void afterEach() {
        try {
            fgaClient.deleteStore();
        } catch (FgaInvalidParameterException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void givenRouteWithResourcePolicyFilter_whenRequestingRouteWithUnexpectedPath_thenFilterIsAppliedAndReturn403() {
        // Given
        System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);
        try {
            var options = new ClientWriteOptions()
                    .authorizationModelId(modelId);
            var body = buildTupleRequest(List.of(new Tuple("user:1234567890", "get", "resource:/ip/invalid", null)));
            var response = fgaClient.write(body, options).get();
        } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
            throw new RuntimeException(e);
        }
        final String bearer = "Bearer " + TOKEN.get("unknown");

        // When
        ValidatableResponse response = given()
                .baseUri(BASE_URL)
                .port(LOCAL_SERVER_PORT)
                .when()
                .header("ApiVersion", "4.5.0")
                .header("Authorization", bearer)
                .get("/version/ip")
                .then()
                .statusCode(403);

        // Then
        assertThat(response).isNotNull();
        String body = response.extract().body().asString();
        assertThat(body).isNotNull();
    }

    @Test
    void givenRouteWithResourcePolicyFilter_whenRequestingRouteWithExpectedPath_thenFilterIsApplied() {
        // Given
        System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);
        try {
            var options = new ClientWriteOptions()
                    .authorizationModelId(modelId);
            var body = buildTupleRequest(List.of(new Tuple("user:1234567890", "get", "resource:/ip", null)));
            var response = fgaClient.write(body, options).get();
        } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
            throw new RuntimeException(e);
        }
        final String bearer = "Bearer " + TOKEN.get("unknown");

        // When
        ValidatableResponse response = given()
                .baseUri(BASE_URL)
                .port(LOCAL_SERVER_PORT)
                .when()
                .header("ApiVersion", "4.5.0")
                .header("Authorization", bearer)
                .get("/version/ip")
                .then()
                .statusCode(200);

        // Then
        assertThat(response).isNotNull();
        String body = response.extract().body().asString();
        assertThat(body).isNotNull().matches(pattern);
    }

    @Test
    void givenRouteWithResourcePolicyFilter_whenRequestingRouteWithFreeUser_thenFilterIsApplied() {
        // Given
        System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);
        try {
            var options = new ClientWriteOptions()
                    .authorizationModelId(modelId);
            var body = buildTupleRequest(List.of(new Tuple("user:1234567890", "get", "resource:/ip", null)));
            var response = fgaClient.write(body, options).get();
        } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
            throw new RuntimeException(e);
        }
        final String bearer = "Bearer " + TOKEN.get("free");

        // When
        ValidatableResponse response = given()
                .baseUri(BASE_URL)
                .port(LOCAL_SERVER_PORT)
                .when()
                .header("Authorization", bearer)
                .get("/policy/v1/ip")
                .then()
                .statusCode(200);

        // Then
        assertThat(response).isNotNull();
        String body = response.extract().body().asString();
        assertThat(body).isNotNull().matches(pattern);
    }

    @ParameterizedTest
    @MethodSource("provideTokenKeyAndUserKey")
    void givenRouteWithResourcePolicyFilter_whenRequestingRouteWithNonFreeUser_thenFilterIsApplied(String tokenKey, String userKey) {
        // Given
        System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);
        try {
            var options = new ClientWriteOptions()
                    .authorizationModelId(modelId);
            long currentSec = System.currentTimeMillis()/1000;
            var body = buildTupleRequest(List.of(new Tuple("user:"+userKey, "get", "resource:/ip",
                    new TupleCondition("ttl", Map.of("expiry", currentSec+60)))));
            var response = fgaClient.write(body, options).get();
        } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
            throw new RuntimeException(e);
        }
        final String bearer = "Bearer " + TOKEN.get(tokenKey);

        // When
        ValidatableResponse response = given()
                .baseUri(BASE_URL)
                .port(LOCAL_SERVER_PORT)
                .when()
                .header("Authorization", bearer)
                .get("/policy/v1/ip")
                .then()
                .statusCode(200);

        // Then
        assertThat(response).isNotNull();
        String body = response.extract().body().asString();
        assertThat(body).isNotNull().matches(pattern);
    }

    @ParameterizedTest
    @MethodSource("provideTokenKeyAndUserKey")
    void givenRouteWithResourcePolicyFilter_whenRequestingRouteWithNonFreeUserExpired_thenFilterIsApplied(String tokenKey, String userKey) {
        // Given
        System.out.println("LOCAL SERVER PORT: " + LOCAL_SERVER_PORT);
        try {
            var options = new ClientWriteOptions()
                    .authorizationModelId(modelId);
            long currentSec = System.currentTimeMillis()/1000;
            var body = buildTupleRequest(List.of(new Tuple("user:"+userKey, "get", "resource:/ip",
                    new TupleCondition("ttl", Map.of("expiry", (currentSec-10))))));
            var response = fgaClient.write(body, options).get();
        } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
            throw new RuntimeException(e);
        }
        final String bearer = "Bearer " + TOKEN.get(tokenKey);

        // When
        ValidatableResponse response = given()
                .baseUri(BASE_URL)
                .port(LOCAL_SERVER_PORT)
                .when()
                .header("Authorization", bearer)
                .get("/policy/v1/ip")
                .then()
                .statusCode(403);

        // Then
        assertThat(response).isNotNull();
        String body = response.extract().body().asString();
        assertThat(body).isNotNull();
    }

    private static Stream<Arguments> provideTokenKeyAndUserKey() {
        return Stream.of(
                Arguments.of("premium", "bar123"),
                Arguments.of("guest", "john909+bar123")
        );
    }

    private ClientWriteRequest buildTupleRequest(final List<Tuple> tuples) {
        return new ClientWriteRequest()
                .writes(tuples.stream()
                        .map(t -> new ClientTupleKey()
                                .user(t.user())
                                .relation(t.relation())
                                ._object(t.object())
                                .condition( t.condition != null? new ClientRelationshipCondition().
                                        name(t.condition.name()).context(t.condition.context()) : null)
                        )
                        .toList());
    }

    record Tuple(String user, String relation, String object, TupleCondition condition) {
    }

    record TupleCondition(String name, Map<String, Object> context) {
    }

}