package ru.rsreu.festivalmanager.festival;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("e2e")
public class BoundaryIntegrationTest {
    private static final String AUTH_BASE = "http://localhost:8082";
    private static final String FESTIVAL_BASE = "http://localhost:8085";
    private static final String EVENT_BASE = "http://localhost:8084";
    private static final String FILE_BASE = "http://localhost:8086";

    private static final int LAST_TICKET_TYPE_ID = 14;
    private static final int LAST_EVENT_ID = 18;
    private static final int NORMAL_TICKET_TYPE_ID = 1;

    private static String tokenUser1;
    private static String tokenUser2;
    private static String tokenUser3Blocked;
    private static String tokenCompany;

    @BeforeAll
    static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        tokenUser1 = login("bibu_duguvi34@yandex.ru", "Qwertry123456");
        tokenUser2 = login("xuh-opitucu79@list.ru", "Qwertry123456");
        tokenUser3Blocked = login("fucibox-upu58@bk.ru", "Qwertry123456");
        tokenCompany = login("info@novacore.tech", "yLuE1ehi");
    }

    static String login(String email, String password) {
        return given()
                .baseUri(AUTH_BASE)
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                .extract().jsonPath().getString("access_token");
    }

    @Test
    @Order(1)
    @DisplayName("Последний билет: один получает, второй — отказ")
    void lastTicket_concurrentPurchase_onlyOneSucceeds() throws InterruptedException {
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        int[] statuses = new int[2];

        Runnable buy1 = () -> {
            try {
                startGate.await();
                statuses[0] = given()
                        .baseUri(FESTIVAL_BASE)
                        .header("Authorization", "Bearer " + tokenUser1)
                        .contentType(ContentType.JSON)
                        .body(Map.of("ticketTypeId", LAST_TICKET_TYPE_ID,
                                     "days", List.of("2026-07-17")))
                    .when()
                        .post("/ticket/purchase")
                    .then()
                        .extract().statusCode();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally { done.countDown(); }
        };

        Runnable buy2 = () -> {
            try {
                startGate.await();
                statuses[1] = given()
                        .baseUri(FESTIVAL_BASE)
                        .header("Authorization", "Bearer " + tokenUser2)
                        .contentType(ContentType.JSON)
                        .body(Map.of("ticketTypeId", LAST_TICKET_TYPE_ID,
                                     "days", List.of("2026-07-17")))
                    .when()
                        .post("/ticket/purchase")
                    .then()
                        .extract().statusCode();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally { done.countDown(); }
        };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(buy1);
        pool.submit(buy2);
        startGate.countDown();
        done.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        long successCount = Arrays.stream(statuses).filter(s -> s == 200 || s == 201).count();
        long errorCount = Arrays.stream(statuses).filter(s -> s >= 400).count();

        assertEquals(1, successCount, "Ровно один пользователь должен купить билет");
        assertEquals(1, errorCount, "Второй пользователь должен получить отказ");
    }

    @Test
    @Order(2)
    @DisplayName("Последнее место на мероприятие: один проходит, второй — нет")
    void lastEventSlot_concurrentRegistration_onlyOneSucceeds() throws InterruptedException {
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        int[] statuses = new int[2];

        Runnable reg1 = () -> {
            try {
                startGate.await();
                statuses[0] = given()
                        .baseUri(EVENT_BASE)
                        .header("Authorization", "Bearer " + tokenUser1)
                    .when()
                        .post("/events/registrations/" + LAST_EVENT_ID)
                    .then()
                        .extract().statusCode();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally { done.countDown(); }
        };

        Runnable reg2 = () -> {
            try {
                startGate.await();
                statuses[1] = given()
                        .baseUri(EVENT_BASE)
                        .header("Authorization", "Bearer " + tokenUser2)
                    .when()
                        .post("/events/registrations/" + LAST_EVENT_ID)
                    .then()
                        .extract().statusCode();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally { done.countDown(); }
        };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(reg1);
        pool.submit(reg2);
        startGate.countDown();
        done.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        long ok  = Arrays.stream(statuses).filter(s -> s == 200 || s == 201).count();
        long err = Arrays.stream(statuses).filter(s -> s >= 400).count();

        assertEquals(1, ok, "Ровно один пользователь должен зарегистрироваться");
        assertEquals(1, err, "Второй должен получить отказ");
    }

    @Test
    @Order(3)
    @DisplayName("Истёкшая блокировка не препятствует покупке билета")
    void expiredBlacklist_purchaseSucceeds() {
        given()
                .baseUri(FESTIVAL_BASE)
                .header("Authorization", "Bearer " + tokenUser3Blocked)
                .contentType(ContentType.JSON)
                .body(Map.of("ticketTypeId", NORMAL_TICKET_TYPE_ID,
                             "days", List.of("2026-07-17")))
            .when()
                .post("/ticket/purchase")
            .then()
                .statusCode(anyOf(is(200), is(201)));
    }

    @Test
    @Order(4)
    @DisplayName("Файл сверх лимита отклоняется сервером (413 / 400)")
    void oversizedFile_isRejected() {
        byte[] oversized = new byte[15 * 1024 * 1024];
        new Random().nextBytes(oversized);

        given()
                .baseUri(FILE_BASE)
                .header("Authorization", "Bearer " + tokenCompany)
                .contentType("application/octet-stream")
                .body(oversized)
            .when()
                .post("/files/FESTIVAL_LOCATION/1")
            .then()
                .statusCode(anyOf(is(400), is(413)));
    }
}
