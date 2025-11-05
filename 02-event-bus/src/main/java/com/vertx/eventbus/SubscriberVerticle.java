package com.vertx.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subscriber Verticle - Demonstrates Event Bus Subscriptions
 *
 * Key Concepts:
 * 1. Subscribing to event bus addresses
 * 2. Message consumers
 * 3. Handling different message types
 * 4. Unregistering consumers
 */
public class SubscriberVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(SubscriberVerticle.class);
    private MessageConsumer<String> notificationConsumer;
    private MessageConsumer<JsonObject> userUpdateConsumer;
    private MessageConsumer<JsonObject> dataEventConsumer;

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Subscriber Verticle Starting ===");

        // Subscribe to notifications
        notificationConsumer = vertx.eventBus().consumer("notifications.all", message -> {
            logger.info("📬 Received notification: {}", message.body());
        });

        // Subscribe to user updates
        userUpdateConsumer = vertx.eventBus().consumer("user.updates", message -> {
            JsonObject userJson = (JsonObject) message.body();
            User user = User.fromJson(userJson);
            logger.info("👤 Received user update: {}", user);
        });

        // Subscribe to data events
        dataEventConsumer = vertx.eventBus().consumer("data.events", message -> {
            JsonObject event = (JsonObject) message.body();
            logger.info("📊 Received data event: {} - {} records",
                    event.getString("event"),
                    event.getJsonObject("data").getInteger("records"));
        });

        // Log when consumers are ready
        notificationConsumer.completionHandler(result -> {
            if (result.succeeded()) {
                logger.info("✅ Notification consumer registered successfully");
            }
        });

        startPromise.complete();
        logger.info("SubscriberVerticle started successfully!");
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        logger.info("Stopping SubscriberVerticle...");

        // Unregister consumers
        if (notificationConsumer != null) {
            notificationConsumer.unregister();
        }
        if (userUpdateConsumer != null) {
            userUpdateConsumer.unregister();
        }
        if (dataEventConsumer != null) {
            dataEventConsumer.unregister();
        }

        stopPromise.complete();
        logger.info("SubscriberVerticle stopped!");
    }
}
