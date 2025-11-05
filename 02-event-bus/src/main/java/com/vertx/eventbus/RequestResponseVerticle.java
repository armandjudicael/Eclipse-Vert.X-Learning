package com.vertx.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request-Response Verticle - Demonstrates Request-Reply Pattern
 *
 * Key Concepts:
 * 1. Request-Response messaging pattern
 * 2. Replying to messages
 * 3. Handling requests with business logic
 * 4. Error handling in responses
 */
public class RequestResponseVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Request-Response Verticle Starting ===");

        // Handle user lookup requests
        vertx.eventBus().consumer("user.lookup", message -> {
            String userId = (String) message.body();
            logger.info("🔍 Received user lookup request for ID: {}", userId);

            // Simulate database lookup
            vertx.setTimer(500, id -> {
                User user = new User(userId, "John Doe", "john@example.com");
                message.reply(user.toJson());
                logger.info("✅ Sent user lookup response for ID: {}", userId);
            });
        });

        // Handle calculation requests
        vertx.eventBus().consumer("math.add", message -> {
            JsonObject request = (JsonObject) message.body();
            int a = request.getInteger("a");
            int b = request.getInteger("b");

            logger.info("🔢 Received addition request: {} + {}", a, b);

            int result = a + b;
            JsonObject response = new JsonObject()
                    .put("operation", "add")
                    .put("result", result);

            message.reply(response);
            logger.info("✅ Sent calculation result: {}", result);
        });

        // Handle validation requests with error handling
        vertx.eventBus().consumer("email.validate", message -> {
            String email = (String) message.body();
            logger.info("📧 Validating email: {}", email);

            if (email == null || !email.contains("@")) {
                message.fail(400, "Invalid email format");
                logger.warn("❌ Invalid email: {}", email);
            } else {
                JsonObject response = new JsonObject()
                        .put("email", email)
                        .put("valid", true);
                message.reply(response);
                logger.info("✅ Email is valid: {}", email);
            }
        });

        startPromise.complete();
        logger.info("RequestResponseVerticle started successfully!");
    }
}
