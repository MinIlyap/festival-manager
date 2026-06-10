package ru.rsreu.festivalmanager.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityIntegrationTest {

    private static final String AUTH_BASE     = "http://localhost:8082";
    private static final String FESTIVAL_BASE = "http://localhost:8085";
    private static final String APP_BASE      = "http://localhost:8081";

    private static final String USER_EMAIL    = "bibu_duguvi34@yandex.ru";
    private static final String USER_PASSWORD = "Qwertry123456";

    private static final String EXPIRED_JWT =
            "eyJhbGciOiJIUzI1NiJ9" +
            ".eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWQiOjEsInJvbGUiOiJVU0VSIiwi" +
            "aWF0IjoxNTc3ODM2ODAwLCJleHAiOjE1Nzc4NDA0MDB9" +
            ".invalid_signature_for_test";

    private static String userToken;

    @BeforeAll
    static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        userToken = loginAndGetToken(USER_EMAIL, USER_PASSWORD);
        assertNotNull(userToken, "Не удалось получить токен пользователя");
    }

    static String loginAndGetToken(String email, String password) {
        return given()
                .baseUri(AUTH_BASE)
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");
    }

    @Test
    @Order(1)
    @DisplayName("Запрос без токена возвращает 401 Unauthorized")
    void noToken_returns401() {
        given()
                .baseUri(FESTIVAL_BASE)
                .contentType(ContentType.JSON)
                .body(Map.of("ticketTypeId", 1, "days", List.of("2026-07-17")))
            .when()
                .post("/ticket/purchase")
            .then()
                .statusCode(401);
    }

    @Test
    @Order(2)
    @DisplayName("Роль USER на admin-эндпоинте возвращает 403 Forbidden")
    void userRole_onAdminEndpoint_returns403() {
        given()
                .baseUri(APP_BASE)
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON)
                .body(Map.of("approved", true, "comment", ""))
            .when()
                .post("/application/admin/1/review")
            .then()
                .statusCode(403);
    }

    @Test
    @Order(3)
    @DisplayName("Истёкший JWT-токен возвращает 401 Unauthorized")
    void expiredJwt_returns401() {
        given()
                .baseUri(FESTIVAL_BASE)
                .header("Authorization", "Bearer " + EXPIRED_JWT)
                .contentType(ContentType.JSON)
                .body(Map.of("ticketTypeId", 1, "days", List.of("2026-07-17")))
            .when()
                .post("/ticket/purchase")
            .then()
                .statusCode(401);
    }

    @Test
    @Order(4)
    @DisplayName("Невалидный refreshToken не выдаёт новый accessToken")
    void invalidRefreshToken_returnsError() {
        given()
                .baseUri(AUTH_BASE)
                .contentType(ContentType.JSON)
                .body(Map.of("refreshToken", "expired.or.invalid.refresh.token.value"))
            .when()
                .post("/auth/refresh")
            .then()
                .statusCode(anyOf(is(400), is(401), is(403)));
    }

    @Test
    @Order(5)
    @DisplayName("Internal-эндпоинт без системного токена возвращает 403")
    void internalEndpoint_withoutSystemToken_returns403() {
        given()
                .baseUri(AUTH_BASE)
                .header("Authorization", "Bearer " + userToken)
            .when()
                .get("/auth/users/1")
            .then()
                .statusCode(403);
    }
}
