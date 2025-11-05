package com.vertx.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Verticle - Demonstrates Event Bus Client Operations
 *
 * Key Concepts:
 * 1. Sending point-to-point messages
 * 2. Making requests and handling responses
 * 3. Error handling for failed requests
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Main Verticle Starting ===");

        // Deploy all other verticles first
        deployVerticles()
                .onSuccess(v -> {
                    logger.info("All verticles deployed successfully!");

                    // Start making requests after deployment
                    startRequestExamples();

                    startPromise.complete();
                })
                .onFailure(err -> {
                    logger.error("Failed to deploy verticles", err);
                    startPromise.fail(err);
                });
    }

    private Promise<Void> deployVerticles() {
        Promise<Void> promise = Promise.promise();

        // Deploy Publisher
        vertx.deployVerticle(new PublisherVerticle())
                .compose(id -> {
                    logger.info("PublisherVerticle deployed: {}", id);
                    // Deploy Subscriber
                    return vertx.deployVerticle(new SubscriberVerticle());
                })
                .compose(id -> {
                    logger.info("SubscriberVerticle deployed: {}", id);
                    // Deploy another Subscriber to show multiple consumers
                    return vertx.deployVerticle(new SubscriberVerticle());
                })
                .compose(id -> {
                    logger.info("Second SubscriberVerticle deployed: {}", id);
                    // Deploy Request-Response verticle
                    return vertx.deployVerticle(new RequestResponseVerticle());
                })
                .onSuccess(id -> {
                    logger.info("RequestResponseVerticle deployed: {}", id);
                    promise.complete();
                })
                .onFailure(promise::fail);

        return promise;
    }

    private void startRequestExamples() {
        // Example 1: Request user lookup
        vertx.setPeriodic(8000, id -> {
            String userId = "user-123";
            logger.info("🔍 Requesting user lookup for: {}", userId);

            vertx.eventBus().<JsonObject>request("user.lookup", userId, reply -> {
                if (reply.succeeded()) {
                    User user = User.fromJson(reply.result().body());
                    logger.info("✅ Received user: {}", user);
                } else {
                    logger.error("❌ User lookup failed", reply.cause());
                }
            });
        });

        // Example 2: Request calculation
        vertx.setPeriodic(6000, id -> {
            JsonObject request = new JsonObject()
                    .put("a", 10)
                    .put("b", 25);

            logger.info("🔢 Requesting calculation: {}", request.encode());

            vertx.eventBus().<JsonObject>request("math.add", request, reply -> {
                if (reply.succeeded()) {
                    JsonObject result = reply.result().body();
                    logger.info("✅ Calculation result: {}", result.getInteger("result"));
                } else {
                    logger.error("❌ Calculation failed", reply.cause());
                }
            });
        });

        // Example 3: Email validation with error handling
        vertx.setTimer(2000, id -> {
            // Valid email
            validateEmail("test@example.com");

            // Invalid email
            vertx.setTimer(1000, tid -> validateEmail("invalid-email"));
        });

        // Example 4: Point-to-point send (only one consumer receives)
        vertx.setPeriodic(10000, id -> {
            String message = "Point-to-point message at " + System.currentTimeMillis();
            vertx.eventBus().send("notifications.all", message);
            logger.info("📤 Sent point-to-point message: {}", message);
        });
    }

    private void validateEmail(String email) {
        logger.info("📧 Validating email: {}", email);

        vertx.eventBus().<JsonObject>request("email.validate", email, reply -> {
            if (reply.succeeded()) {
                JsonObject result = reply.result().body();
                logger.info("✅ Email validation result: {}", result.encode());
            } else {
                logger.error("❌ Email validation failed: {}", reply.cause().getMessage());
            }
        });
    }
}
