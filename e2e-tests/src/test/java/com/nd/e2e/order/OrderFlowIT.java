package com.nd.e2e.order;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @since 2026
 */
class OrderFlowIT {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getProperty("e2e.base-url", "http://localhost:18080");
    }

    @Test
    void shouldConfirmOrderAfterSuccessfulPayment() {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();

        var body = """
                {
                    "customerId": "%s",
                    "items": [
                        {
                            "productId": "%s",
                            "productName": "Test Product",
                            "productPrice": 1000.00,
                            "quantity": 2
                        }
                    ]
                }
                """.formatted(customerId, productId);

        // GIVEN
        String orderId = given()
                .contentType(ContentType.JSON)
                .body(body)

                // WHEN
                .when()
                .post("/api/orders")

                // THEN
                .then()
                .statusCode(201)
                .body("orderId", notNullValue())
                .body("status", equalTo("CREATED"))

                .extract()
                .path("orderId");

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        given()

                                // WHEN
                                .when()
                                .get("/api/orders/{id}", orderId)

                                // THEN
                                .then()
                                .statusCode(200)
                                .body("id", equalTo(orderId))
                                .body("status", equalTo("CONFIRMED"))
                );
    }

}
