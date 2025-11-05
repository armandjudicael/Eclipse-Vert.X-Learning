package com.vertx.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publisher Verticle - Demonstrates Event Bus Publishing
 *
 * Key Concepts:
 * 1. Publishing messages to multiple subscribers
 * 2. Fire-and-forget messaging pattern
 * 3. Different message types (String, JSON, custom objects)
 */
public class PublisherVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(PublisherVerticle.class);
    private static final String NOTIFICATIONS_ADDRESS = "notifications.all";
    private static final String USER_UPDATES_ADDRESS = "user.updates";

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Publisher Verticle Starting ===");

        // Publish notifications periodically
        vertx.setPeriodic(3000, id -> {
            String message = "System notification at " + System.currentTimeMillis();

            // Publish - all subscribers receive the message
            vertx.eventBus().publish(NOTIFICATIONS_ADDRESS, message);
            logger.info("Published notification: {}", message);
        });

        // Publish user updates periodically
        vertx.setPeriodic(5000, id -> {
            User user = new User(
                    "user-" + System.currentTimeMillis(),
                    "John Doe",
                    "john@example.com"
            );

            // Publish JSON object
            vertx.eventBus().publish(USER_UPDATES_ADDRESS, user.toJson());
            logger.info("Published user update: {}", user);
        });

        // Publish complex JSON structures
        vertx.setPeriodic(7000, id -> {
            JsonObject complexMessage = new JsonObject()
                    .put("event", "data.sync")
                    .put("timestamp", System.currentTimeMillis())
                    .put("data", new JsonObject()
                            .put("records", 150)
                            .put("status", "completed"));

            vertx.eventBus().publish("data.events", complexMessage);
            logger.info("Published complex event: {}", complexMessage.encode());
        });

        startPromise.complete();
        logger.info("PublisherVerticle started successfully!");
    }
}
