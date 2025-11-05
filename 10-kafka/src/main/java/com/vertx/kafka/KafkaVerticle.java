package com.vertx.kafka;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Apache Kafka Integration Module
 *
 * Key Concepts:
 * 1. Kafka Producer - Send messages to topics
 * 2. Kafka Consumer - Read messages from topics
 * 3. Consumer Groups - Load balancing
 * 4. Partitions - Parallel processing
 * 5. Offset Management - Message tracking
 * 6. Error Handling - Retry strategies
 */
public class KafkaVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(KafkaVerticle.class);
    private static final String TOPIC_ORDERS = "orders";
    private static final String TOPIC_NOTIFICATIONS = "notifications";
    private static final String TOPIC_EVENTS = "events";

    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> ordersConsumer;
    private KafkaConsumer<String, String> notificationsConsumer;

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Kafka Integration Module Starting ===");

        // Kafka broker configuration
        String bootstrapServers = config().getString("kafka.bootstrap.servers", "localhost:9092");

        // Initialize producer
        initializeProducer(bootstrapServers);

        // Initialize consumers
        initializeConsumers(bootstrapServers);

        // Setup HTTP API
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // API endpoints
        router.get("/").handler(ctx ->
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject()
                                .put("message", "Kafka Integration API")
                                .put("endpoints", new JsonObject()
                                        .put("sendOrder", "POST /api/orders")
                                        .put("sendNotification", "POST /api/notifications")
                                        .put("sendEvent", "POST /api/events")
                                        .put("health", "GET /health"))
                                .encode()));

        router.get("/health").handler(ctx ->
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject()
                                .put("status", "UP")
                                .put("kafka", "connected")
                                .encode()));

        // Producer endpoints
        router.post("/api/orders").handler(this::sendOrder);
        router.post("/api/notifications").handler(this::sendNotification);
        router.post("/api/events").handler(this::sendEvent);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server -> {
                    logger.info("✅ Kafka module started on port 8080");
                    logger.info("📡 Connected to Kafka: {}", bootstrapServers);
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }

    private void initializeProducer(String bootstrapServers) {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", bootstrapServers);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("acks", "all"); // Wait for all replicas
        config.put("retries", "3");
        config.put("linger.ms", "10");
        config.put("batch.size", "16384");

        producer = KafkaProducer.create(vertx, config);
        logger.info("✅ Kafka Producer initialized");
    }

    private void initializeConsumers(String bootstrapServers) {
        // Orders Consumer (Consumer Group)
        Map<String, String> ordersConfig = new HashMap<>();
        ordersConfig.put("bootstrap.servers", bootstrapServers);
        ordersConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        ordersConfig.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        ordersConfig.put("group.id", "orders-consumer-group");
        ordersConfig.put("auto.offset.reset", "earliest");
        ordersConfig.put("enable.auto.commit", "true");

        ordersConsumer = KafkaConsumer.create(vertx, ordersConfig);
        ordersConsumer.subscribe(Set.of(TOPIC_ORDERS));

        ordersConsumer.handler(record -> {
            logger.info("📦 Order received:");
            logger.info("   Topic: {}, Partition: {}, Offset: {}",
                    record.topic(), record.partition(), record.offset());
            logger.info("   Key: {}, Value: {}", record.key(), record.value());

            // Process order
            processOrder(record.value());
        });

        ordersConsumer.exceptionHandler(err -> {
            logger.error("❌ Orders consumer error", err);
        });

        logger.info("✅ Orders Consumer initialized (group: orders-consumer-group)");

        // Notifications Consumer
        Map<String, String> notifConfig = new HashMap<>();
        notifConfig.put("bootstrap.servers", bootstrapServers);
        notifConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        notifConfig.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        notifConfig.put("group.id", "notifications-consumer-group");
        notifConfig.put("auto.offset.reset", "latest");
        notifConfig.put("enable.auto.commit", "true");

        notificationsConsumer = KafkaConsumer.create(vertx, notifConfig);
        notificationsConsumer.subscribe(Set.of(TOPIC_NOTIFICATIONS));

        notificationsConsumer.handler(record -> {
            logger.info("📧 Notification received:");
            logger.info("   Key: {}, Value: {}", record.key(), record.value());

            // Send notification
            sendNotificationEmail(record.value());
        });

        notificationsConsumer.exceptionHandler(err -> {
            logger.error("❌ Notifications consumer error", err);
        });

        logger.info("✅ Notifications Consumer initialized (group: notifications-consumer-group)");
    }

    private void sendOrder(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", "Missing request body").encode());
            return;
        }

        String orderId = body.getString("orderId", UUID.randomUUID().toString());
        String customerId = body.getString("customerId");
        String product = body.getString("product");
        Double amount = body.getDouble("amount");

        if (customerId == null || product == null || amount == null) {
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", "Missing required fields").encode());
            return;
        }

        // Create order message
        JsonObject orderMessage = new JsonObject()
                .put("orderId", orderId)
                .put("customerId", customerId)
                .put("product", product)
                .put("amount", amount)
                .put("timestamp", System.currentTimeMillis())
                .put("status", "pending");

        // Send to Kafka
        KafkaProducerRecord<String, String> record =
                KafkaProducerRecord.create(TOPIC_ORDERS, orderId, orderMessage.encode());

        producer.send(record)
                .onSuccess(metadata -> {
                    logger.info("✅ Order published to Kafka:");
                    logger.info("   Topic: {}, Partition: {}, Offset: {}",
                            metadata.getTopic(), metadata.getPartition(), metadata.getOffset());

                    ctx.response()
                            .setStatusCode(202)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("message", "Order accepted")
                                    .put("orderId", orderId)
                                    .put("partition", metadata.getPartition())
                                    .put("offset", metadata.getOffset())
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to publish order", err);
                    ctx.response()
                            .setStatusCode(500)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("error", "Failed to process order")
                                    .encode());
                });
    }

    private void sendNotification(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.response().setStatusCode(400).end("Missing body");
            return;
        }

        String recipient = body.getString("recipient");
        String message = body.getString("message");

        JsonObject notification = new JsonObject()
                .put("recipient", recipient)
                .put("message", message)
                .put("timestamp", System.currentTimeMillis());

        KafkaProducerRecord<String, String> record =
                KafkaProducerRecord.create(TOPIC_NOTIFICATIONS, recipient, notification.encode());

        producer.send(record)
                .onSuccess(metadata -> {
                    logger.info("✅ Notification published (partition: {}, offset: {})",
                            metadata.getPartition(), metadata.getOffset());

                    ctx.response()
                            .setStatusCode(202)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("message", "Notification queued")
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to publish notification", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void sendEvent(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.response().setStatusCode(400).end("Missing body");
            return;
        }

        String eventType = body.getString("type");
        JsonObject eventData = body.getJsonObject("data", new JsonObject());

        JsonObject event = new JsonObject()
                .put("type", eventType)
                .put("data", eventData)
                .put("timestamp", System.currentTimeMillis());

        String key = eventType + "-" + System.currentTimeMillis();

        KafkaProducerRecord<String, String> record =
                KafkaProducerRecord.create(TOPIC_EVENTS, key, event.encode());

        producer.send(record)
                .onSuccess(metadata -> {
                    logger.info("✅ Event published: {} (partition: {}, offset: {})",
                            eventType, metadata.getPartition(), metadata.getOffset());

                    ctx.response()
                            .setStatusCode(202)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("message", "Event published")
                                    .put("type", eventType)
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to publish event", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void processOrder(String orderJson) {
        try {
            JsonObject order = new JsonObject(orderJson);
            String orderId = order.getString("orderId");
            String product = order.getString("product");

            logger.info("🔄 Processing order: {} for product: {}", orderId, product);

            // Simulate order processing
            vertx.setTimer(1000, id -> {
                logger.info("✅ Order processed successfully: {}", orderId);

                // Send notification about order
                JsonObject notification = new JsonObject()
                        .put("recipient", order.getString("customerId"))
                        .put("message", "Your order " + orderId + " has been processed");

                KafkaProducerRecord<String, String> notifRecord =
                        KafkaProducerRecord.create(TOPIC_NOTIFICATIONS,
                                order.getString("customerId"),
                                notification.encode());

                producer.send(notifRecord);
            });

        } catch (Exception e) {
            logger.error("❌ Error processing order", e);
        }
    }

    private void sendNotificationEmail(String notificationJson) {
        try {
            JsonObject notification = new JsonObject(notificationJson);
            String recipient = notification.getString("recipient");
            String message = notification.getString("message");

            logger.info("📧 Sending notification to {}: {}", recipient, message);

            // Simulate email sending
            vertx.setTimer(500, id -> {
                logger.info("✅ Notification sent to {}", recipient);
            });

        } catch (Exception e) {
            logger.error("❌ Error sending notification", e);
        }
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        logger.info("Stopping Kafka module...");

        List<io.vertx.core.Future<?>> futures = new ArrayList<>();

        if (producer != null) {
            futures.add(producer.close());
        }
        if (ordersConsumer != null) {
            futures.add(ordersConsumer.close());
        }
        if (notificationsConsumer != null) {
            futures.add(notificationsConsumer.close());
        }

        io.vertx.core.Future.all(futures)
                .onComplete(result -> {
                    logger.info("Kafka connections closed");
                    stopPromise.complete();
                });
    }
}
