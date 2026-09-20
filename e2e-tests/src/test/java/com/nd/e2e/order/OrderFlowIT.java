package com.nd.e2e.order;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.time.Duration;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since 2026
 */
class OrderFlowIT {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getProperty("e2e.base-url", "http://localhost:8080");
    }

    @Test
    void shouldCancelOrderWhenInventoryReservationFails() {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();

        var body = """
                {
                    "customerId": "%s",
                    "items": [
                        {
                            "productId": "%s",
                            "productName": "Test Product 4",
                            "productPrice": 100.00,
                            "quantity": 4
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
                .atMost(Duration.ofSeconds(40))
                .untilAsserted(() ->
                        given()

                                // WHEN
                                .when()
                                .get("/api/orders/{id}", orderId)

                                // THEN
                                .then()
                                .statusCode(200)
                                .body("id", equalTo(orderId))
                                .body("status", equalTo("CANCELED"))
                );
    }

    @Test
    void shouldConfirmOrderWhenInventoryReservationSucceeds() throws Exception {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();

        prepareInventory(productId, 10);

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

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .body("orderId", notNullValue())
                .body("status", equalTo("CREATED"))
                .extract()
                .path("orderId");

        await()
                .atMost(Duration.ofSeconds(40))
                .untilAsserted(() ->
                        given()
                                .when()
                                .get("/api/orders/{id}", orderId)
                                .then()
                                .statusCode(200)
                                .body("id", equalTo(orderId))
                                .body("status", equalTo("CONFIRMED"))
                );

        assertEquals(8, getAvailableQuantity(productId));
    }

    private void prepareInventory(UUID productId, int quantity) throws Exception {
        var dbUrl = System.getProperty(
                "e2e.inventory-db-url",
                "jdbc:postgresql://localhost:5432/inventory_db"
        );

        try (var connection = DriverManager.getConnection(dbUrl, "postgres", "postgres");
             var statement = connection.prepareStatement("""
                     insert into inventory_items (id, product_id, available_quantity)
                     values (?, ?, ?)
                     on conflict (product_id)
                     do update set available_quantity = excluded.available_quantity
                     """)) {

            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, productId);
            statement.setInt(3, quantity);

            statement.executeUpdate();
        }
    }

    private int getAvailableQuantity(UUID productId) throws Exception {
        var dbUrl = System.getProperty(
                "e2e.inventory-db-url",
                "jdbc:postgresql://localhost:5432/inventory_db"
        );

        try (var connection = DriverManager.getConnection(dbUrl, "postgres", "postgres");
             var statement = connection.prepareStatement("""
                     select available_quantity
                     from inventory_items
                     where product_id = ?
                     """)) {

            statement.setObject(1, productId);

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Inventory item not found: " + productId);
                }

                return resultSet.getInt("available_quantity");
            }
        }
    }
}
