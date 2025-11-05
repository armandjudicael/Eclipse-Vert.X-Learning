package com.vertx.testing;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration Tests for MainVerticle (API Testing)
 * Tests the HTTP endpoints and API behavior
 */
@ExtendWith(VertxExtension.class)
@DisplayName("Main Verticle Integration Tests")
class MainVerticleTest {

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {
            testContext.completeNow();
        }));
    }

    @Test
    @DisplayName("Should return health status")
    void testHealthCheck() {
        given()
            .when()
            .get("http://localhost:8080/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUser() {
        JsonObject user = new JsonObject()
            .put("id", "1")
            .put("name", "John Doe")
            .put("email", "john@example.com")
            .put("age", 30);

        given()
            .contentType("application/json")
            .body(user.toString())
            .when()
            .post("http://localhost:8080/api/users")
            .then()
            .statusCode(201)
            .body("id", equalTo("1"))
            .body("name", equalTo("John Doe"))
            .body("email", equalTo("john@example.com"))
            .body("age", equalTo(30));
    }

    @Test
    @DisplayName("Should return 400 for invalid user")
    void testCreateInvalidUser() {
        JsonObject user = new JsonObject()
            .put("id", "1")
            .put("name", "John")
            .put("email", "invalid-email")
            .put("age", 30);

        given()
            .contentType("application/json")
            .body(user.toString())
            .when()
            .post("http://localhost:8080/api/users")
            .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("Should get user by ID")
    void testGetUserById() {
        // Create user first
        JsonObject user = new JsonObject()
            .put("id", "1")
            .put("name", "John Doe")
            .put("email", "john@example.com")
            .put("age", 30);

        given()
            .contentType("application/json")
            .body(user.toString())
            .when()
            .post("http://localhost:8080/api/users")
            .then()
            .statusCode(201);

        // Get user
        given()
            .when()
            .get("http://localhost:8080/api/users/1")
            .then()
            .statusCode(200)
            .body("id", equalTo("1"))
            .body("name", equalTo("John Doe"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent user")
    void testGetNonExistentUser() {
        given()
            .when()
            .get("http://localhost:8080/api/users/999")
            .then()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("Should get all users")
    void testGetAllUsers() {
        given()
            .when()
            .get("http://localhost:8080/api/users")
            .then()
            .statusCode(200)
            .body("users", notNullValue())
            .body("count", notNullValue());
    }

    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser() {
        // Create user first
        JsonObject user = new JsonObject()
            .put("id", "1")
            .put("name", "John Doe")
            .put("email", "john@example.com")
            .put("age", 30);

        given()
            .contentType("application/json")
            .body(user.toString())
            .when()
            .post("http://localhost:8080/api/users")
            .then()
            .statusCode(201);

        // Update user
        JsonObject updates = new JsonObject()
            .put("name", "John Updated")
            .put("age", 31);

        given()
            .contentType("application/json")
            .body(updates.toString())
            .when()
            .put("http://localhost:8080/api/users/1")
            .then()
            .statusCode(200)
            .body("name", equalTo("John Updated"))
            .body("age", equalTo(31));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser() {
        // Create user first
        JsonObject user = new JsonObject()
            .put("id", "1")
            .put("name", "John Doe")
            .put("email", "john@example.com")
            .put("age", 30);

        given()
            .contentType("application/json")
            .body(user.toString())
            .when()
            .post("http://localhost:8080/api/users")
            .then()
            .statusCode(201);

        // Delete user
        given()
            .when()
            .delete("http://localhost:8080/api/users/1")
            .then()
            .statusCode(204);

        // Verify deletion
        given()
            .when()
            .get("http://localhost:8080/api/users/1")
            .then()
            .statusCode(404);
    }
}