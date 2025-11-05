package com.vertx.async;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Async Programming with Futures and Promises
 *
 * Key Concepts:
 * 1. Future and Promise API
 * 2. Future composition (compose, map, flatMap)
 * 3. Handling multiple futures (all, any, join)
 * 4. Error handling
 * 5. Timeout handling
 */
public class AsyncVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AsyncVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Async Programming with Futures ===");

        // Example 1: Basic Future and Promise
        basicFutureExample();

        // Example 2: Future Composition
        vertx.setTimer(2000, id -> futureCompositionExample());

        // Example 3: Multiple Futures
        vertx.setTimer(4000, id -> multipleFuturesExample());

        // Example 4: Error Handling
        vertx.setTimer(6000, id -> errorHandlingExample());

        // Example 5: Timeout
        vertx.setTimer(8000, id -> timeoutExample());

        // Example 6: Real-world scenario - User registration flow
        vertx.setTimer(10000, id -> userRegistrationFlow());

        startPromise.complete();
        logger.info("AsyncVerticle started successfully!");
    }

    /**
     * Example 1: Basic Future and Promise
     */
    private void basicFutureExample() {
        logger.info("\n=== Basic Future Example ===");

        // Create a promise
        Promise<String> promise = Promise.promise();

        // Simulate async operation
        vertx.setTimer(500, id -> {
            // Complete the promise
            promise.complete("Operation completed successfully");
        });

        // Use the future
        promise.future()
                .onSuccess(result -> logger.info("✅ Success: {}", result))
                .onFailure(err -> logger.error("❌ Failed: {}", err.getMessage()));
    }

    /**
     * Example 2: Future Composition with compose() and map()
     */
    private void futureCompositionExample() {
        logger.info("\n=== Future Composition Example ===");

        fetchUser("user123")
                .compose(user -> {
                    logger.info("📦 Step 1: Fetched user: {}", user.getString("name"));
                    return fetchUserOrders(user.getString("id"));
                })
                .compose(orders -> {
                    logger.info("📦 Step 2: Fetched {} orders", orders.size());
                    return calculateTotal(orders);
                })
                .map(total -> {
                    logger.info("📦 Step 3: Calculated total: ${}", total);
                    return new JsonObject()
                            .put("total", total)
                            .put("currency", "USD");
                })
                .onSuccess(result -> logger.info("✅ Final result: {}", result.encode()))
                .onFailure(err -> logger.error("❌ Pipeline failed: {}", err.getMessage()));
    }

    /**
     * Example 3: Multiple Futures - all, any, join
     */
    private void multipleFuturesExample() {
        logger.info("\n=== Multiple Futures Example ===");

        Future<String> service1 = callService("Service-1", 300);
        Future<String> service2 = callService("Service-2", 500);
        Future<String> service3 = callService("Service-3", 200);

        // CompositeFuture.all - all must succeed
        CompositeFuture.all(service1, service2, service3)
                .onSuccess(result -> {
                    logger.info("✅ All services completed:");
                    logger.info("   - {}", result.resultAt(0));
                    logger.info("   - {}", result.resultAt(1));
                    logger.info("   - {}", result.resultAt(2));
                })
                .onFailure(err -> logger.error("❌ At least one service failed"));

        // CompositeFuture.any - first to complete
        CompositeFuture.any(service1, service2, service3)
                .onSuccess(result -> logger.info("🏆 First service completed"))
                .onFailure(err -> logger.error("❌ All services failed"));

        // CompositeFuture.join - waits for all (doesn't fail if one fails)
        CompositeFuture.join(service1, service2, service3)
                .onComplete(result -> logger.info("📊 All services finished (success or failure)"));
    }

    /**
     * Example 4: Error Handling with recover and otherwise
     */
    private void errorHandlingExample() {
        logger.info("\n=== Error Handling Example ===");

        failingOperation()
                .recover(err -> {
                    logger.warn("⚠️  Operation failed, recovering: {}", err.getMessage());
                    return Future.succeededFuture("Recovered value");
                })
                .onSuccess(result -> logger.info("✅ Final result: {}", result));

        // Using otherwise
        failingOperation()
                .otherwise("Default value")
                .onSuccess(result -> logger.info("✅ With default: {}", result));

        // Mapping errors
        failingOperation()
                .onFailure(err -> logger.error("❌ Error caught: {}", err.getMessage()))
                .mapEmpty(); // Convert failed future to succeeded empty future
    }

    /**
     * Example 5: Timeout Handling
     */
    private void timeoutExample() {
        logger.info("\n=== Timeout Example ===");

        slowOperation(3000)
                .onSuccess(result -> logger.info("✅ Slow operation completed: {}", result))
                .onFailure(err -> logger.error("❌ Operation failed or timed out: {}", err.getMessage()));

        // Future with custom timeout
        Future<String> futureWithTimeout = slowOperation(5000);

        vertx.setTimer(2000, id -> {
            if (!futureWithTimeout.isComplete()) {
                logger.warn("⏱️  Operation is taking too long!");
            }
        });
    }

    /**
     * Example 6: Real-world User Registration Flow
     */
    private void userRegistrationFlow() {
        logger.info("\n=== User Registration Flow ===");

        String email = "john@example.com";
        String password = "securePassword123";

        validateEmail(email)
                .compose(valid -> {
                    logger.info("✓ Email validated");
                    return checkEmailExists(email);
                })
                .compose(exists -> {
                    if (exists) {
                        return Future.failedFuture("Email already registered");
                    }
                    logger.info("✓ Email available");
                    return hashPassword(password);
                })
                .compose(hashedPassword -> {
                    logger.info("✓ Password hashed");
                    return createUser(email, hashedPassword);
                })
                .compose(userId -> {
                    logger.info("✓ User created with ID: {}", userId);
                    return sendWelcomeEmail(email);
                })
                .onSuccess(v -> logger.info("✅ User registration completed successfully!"))
                .onFailure(err -> logger.error("❌ Registration failed: {}", err.getMessage()));
    }

    // ========== Helper Methods (Simulated Async Operations) ==========

    private Future<JsonObject> fetchUser(String userId) {
        Promise<JsonObject> promise = Promise.promise();
        vertx.setTimer(300, id -> {
            JsonObject user = new JsonObject()
                    .put("id", userId)
                    .put("name", "John Doe")
                    .put("email", "john@example.com");
            promise.complete(user);
        });
        return promise.future();
    }

    private Future<List<JsonObject>> fetchUserOrders(String userId) {
        Promise<List<JsonObject>> promise = Promise.promise();
        vertx.setTimer(400, id -> {
            List<JsonObject> orders = Arrays.asList(
                    new JsonObject().put("id", "order1").put("amount", 29.99),
                    new JsonObject().put("id", "order2").put("amount", 49.99),
                    new JsonObject().put("id", "order3").put("amount", 19.99)
            );
            promise.complete(orders);
        });
        return promise.future();
    }

    private Future<Double> calculateTotal(List<JsonObject> orders) {
        Promise<Double> promise = Promise.promise();
        vertx.setTimer(200, id -> {
            double total = orders.stream()
                    .mapToDouble(order -> order.getDouble("amount"))
                    .sum();
            promise.complete(total);
        });
        return promise.future();
    }

    private Future<String> callService(String serviceName, long delay) {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(delay, id -> promise.complete(serviceName + " response"));
        return promise.future();
    }

    private Future<String> failingOperation() {
        return Future.failedFuture(new RuntimeException("Simulated failure"));
    }

    private Future<String> slowOperation(long delay) {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(delay, id -> promise.complete("Completed after " + delay + "ms"));
        return promise.future();
    }

    // User registration helper methods
    private Future<Boolean> validateEmail(String email) {
        Promise<Boolean> promise = Promise.promise();
        vertx.setTimer(100, id -> {
            boolean valid = email != null && email.contains("@");
            if (valid) {
                promise.complete(true);
            } else {
                promise.fail("Invalid email format");
            }
        });
        return promise.future();
    }

    private Future<Boolean> checkEmailExists(String email) {
        Promise<Boolean> promise = Promise.promise();
        vertx.setTimer(200, id -> promise.complete(false)); // Email doesn't exist
        return promise.future();
    }

    private Future<String> hashPassword(String password) {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(300, id -> promise.complete("hashed_" + password));
        return promise.future();
    }

    private Future<String> createUser(String email, String hashedPassword) {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(400, id -> promise.complete("user_" + System.currentTimeMillis()));
        return promise.future();
    }

    private Future<Void> sendWelcomeEmail(String email) {
        Promise<Void> promise = Promise.promise();
        vertx.setTimer(200, id -> {
            logger.info("📧 Welcome email sent to: {}", email);
            promise.complete();
        });
        return promise.future();
    }
}
