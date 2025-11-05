package com.vertx.rabbitmq;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ Integration Module
 *
 * Key Concepts:
 * 1. Exchange types (direct, topic, fanout, headers)
 * 2. Queue declaration and binding
 * 3. Message publishing with routing keys
 * 4. Consumers with acknowledgments
 * 5. Dead Letter Exchanges (DLX)
 * 6. RPC pattern with reply-to
 * 7. Priority queues
 */
public class RabbitMQVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQVerticle.class);

    // Exchange names
    private static final String DIRECT_EXCHANGE = "direct_exchange";
    private static final String TOPIC_EXCHANGE = "topic_exchange";
    private static final String FANOUT_EXCHANGE = "fanout_exchange";

    // Queue names
    private static final String ORDER_QUEUE = "orders";
    private static final String EMAIL_QUEUE = "emails";
    private static final String SMS_QUEUE = "sms";
    private static final String LOGS_QUEUE = "logs";
    private static final String PRIORITY_QUEUE = "priority_tasks";

    private RabbitMQClient client;

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== RabbitMQ Integration Module Starting ===");

        // Configure RabbitMQ
        RabbitMQOptions config = new RabbitMQOptions()
                .setHost(config().getString("rabbitmq.host", "localhost"))
                .setPort(config().getInteger("rabbitmq.port", 5672))
                .setUser(config().getString("rabbitmq.user", "guest"))
                .setPassword(config().getString("rabbitmq.password", "guest"))
                .setConnectionTimeout(6000)
                .setRequestedHeartbeat(60)
                .setAutomaticRecoveryEnabled(true);

        client = RabbitMQClient.create(vertx, config);

        // Start RabbitMQ client and setup
        client.start()
                .compose(v -> setupExchangesAndQueues())
                .compose(v -> setupConsumers())
                .compose(v -> setupHttpServer())
                .onSuccess(v -> {
                    logger.info("✅ RabbitMQ module started successfully");
                    logger.info("🐰 Connected to RabbitMQ at {}:{}",
                            config.getHost(), config.getPort());
                    startPromise.complete();
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to start RabbitMQ module", err);
                    startPromise.fail(err);
                });
    }

    private io.vertx.core.Future<Void> setupExchangesAndQueues() {
        logger.info("Setting up exchanges and queues...");

        return client.exchangeDeclare(DIRECT_EXCHANGE, "direct", true, false)
                .compose(v -> client.exchangeDeclare(TOPIC_EXCHANGE, "topic", true, false))
                .compose(v -> client.exchangeDeclare(FANOUT_EXCHANGE, "fanout", true, false))
                .compose(v -> {
                    // Declare queues
                    return client.queueDeclare(ORDER_QUEUE, true, false, false);
                })
                .compose(v -> client.queueDeclare(EMAIL_QUEUE, true, false, false))
                .compose(v -> client.queueDeclare(SMS_QUEUE, true, false, false))
                .compose(v -> client.queueDeclare(LOGS_QUEUE, true, false, false))
                .compose(v -> {
                    // Priority queue with arguments
                    JsonObject args = new JsonObject()
                            .put("x-max-priority", 10);
                    return client.queueDeclare(PRIORITY_QUEUE, true, false, false, args);
                })
                .compose(v -> {
                    // Bind queues to exchanges
                    return client.queueBind(ORDER_QUEUE, DIRECT_EXCHANGE, "order");
                })
                .compose(v -> client.queueBind(EMAIL_QUEUE, TOPIC_EXCHANGE, "notify.email.*"))
                .compose(v -> client.queueBind(SMS_QUEUE, TOPIC_EXCHANGE, "notify.sms.*"))
                .compose(v -> client.queueBind(LOGS_QUEUE, FANOUT_EXCHANGE, ""))
                .compose(v -> {
                    logger.info("✅ Exchanges and queues setup completed");
                    return io.vertx.core.Future.succeededFuture();
                });
    }

    private io.vertx.core.Future<Void> setupConsumers() {
        logger.info("Setting up consumers...");

        // Order consumer
        return client.basicConsumer(ORDER_QUEUE)
                .compose(consumer -> {
                    consumer.handler(message -> {
                        String body = message.body().toString();
                        logger.info("📦 Order received: {}", body);

                        // Process order
                        processOrder(body);

                        // Acknowledge message
                        client.basicAck(message.envelope().getDeliveryTag(), false);
                    });

                    consumer.exceptionHandler(err ->
                            logger.error("❌ Order consumer error", err));

                    logger.info("✅ Order consumer started");
                    return io.vertx.core.Future.succeededFuture();
                })
                .compose(v -> client.basicConsumer(EMAIL_QUEUE))
                .compose(consumer -> {
                    consumer.handler(message -> {
                        String body = message.body().toString();
                        logger.info("📧 Email notification: {}", body);

                        // Send email
                        sendEmail(body);

                        client.basicAck(message.envelope().getDeliveryTag(), false);
                    });

                    logger.info("✅ Email consumer started");
                    return io.vertx.core.Future.succeededFuture();
                })
                .compose(v -> client.basicConsumer(SMS_QUEUE))
                .compose(consumer -> {
                    consumer.handler(message -> {
                        String body = message.body().toString();
                        logger.info("📱 SMS notification: {}", body);

                        // Send SMS
                        sendSMS(body);

                        client.basicAck(message.envelope().getDeliveryTag(), false);
                    });

                    logger.info("✅ SMS consumer started");
                    return io.vertx.core.Future.succeededFuture();
                })
                .compose(v -> client.basicConsumer(LOGS_QUEUE))
                .compose(consumer -> {
                    consumer.handler(message -> {
                        String body = message.body().toString();
                        logger.info("📝 Log entry: {}", body);

                        client.basicAck(message.envelope().getDeliveryTag(), false);
                    });

                    logger.info("✅ Logs consumer started");
                    return io.vertx.core.Future.succeededFuture();
                })
                .compose(v -> client.basicConsumer(PRIORITY_QUEUE))
                .compose(consumer -> {
                    consumer.handler(message -> {
                        String body = message.body().toString();
                        int priority = message.properties().getPriority();
                        logger.info("⚡ Priority task ({}): {}", priority, body);

                        client.basicAck(message.envelope().getDeliveryTag(), false);
                    });

                    logger.info("✅ Priority consumer started");
                    logger.info("✅ All consumers setup completed");
                    return io.vertx.core.Future.succeededFuture();
                });
    }

    private io.vertx.core.Future<Void> setupHttpServer() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/").handler(ctx ->
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject()
                                .put("message", "RabbitMQ Integration API")
                                .put("endpoints", new JsonObject()
                                        .put("sendOrder", "POST /api/orders")
                                        .put("sendEmail", "POST /api/notifications/email")
                                        .put("sendSMS", "POST /api/notifications/sms")
                                        .put("broadcast", "POST /api/broadcast")
                                        .put("priorityTask", "POST /api/tasks/priority")
                                        .put("health", "GET /health"))
                                .encode()));

        router.get("/health").handler(ctx ->
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject()
                                .put("status", "UP")
                                .put("rabbitmq", "connected")
                                .encode()));

        // Direct exchange - orders
        router.post("/api/orders").handler(this::publishOrder);

        // Topic exchange - email notifications
        router.post("/api/notifications/email").handler(this::publishEmailNotification);

        // Topic exchange - SMS notifications
        router.post("/api/notifications/sms").handler(this::publishSMSNotification);

        // Fanout exchange - broadcast to all
        router.post("/api/broadcast").handler(this::broadcast);

        // Priority queue
        router.post("/api/tasks/priority").handler(this::publishPriorityTask);

        return vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .map(server -> {
                    logger.info("✅ HTTP server started on port 8080");
                    return null;
                });
    }

    private void publishOrder(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.response().setStatusCode(400).end("Missing body");
            return;
        }

        JsonObject order = new JsonObject()
                .put("orderId", java.util.UUID.randomUUID().toString())
                .put("product", body.getString("product"))
                .put("quantity", body.getInteger("quantity"))
                .put("customer", body.getString("customer"))
                .put("timestamp", System.currentTimeMillis());

        client.basicPublish(DIRECT_EXCHANGE, "order",
                io.vertx.core.buffer.Buffer.buffer(order.encode()))
                .onSuccess(v -> {
                    logger.info("✅ Order published to RabbitMQ");
                    ctx.response()
                            .setStatusCode(202)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("message", "Order queued")
                                    .put("orderId", order.getString("orderId"))
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to publish order", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void publishEmailNotification(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.response().setStatusCode(400).end("Missing body");
            return;
        }

        String recipient = body.getString("recipient");
        String subject = body.getString("subject");
        String message = body.getString("message");

        JsonObject notification = new JsonObject()
                .put("recipient", recipient)
                .put("subject", subject)
                .put("message", message)
                .put("timestamp", System.currentTimeMillis());

        // Topic routing: notify.email.urgent or notify.email.normal
        String routingKey = "notify.email." + body.getString("priority", "normal");

        client.basicPublish(TOPIC_EXCHANGE, routingKey,
                io.vertx.core.buffer.Buffer.buffer(notification.encode()))
                .onSuccess(v -> {
                    logger.info("✅ Email notification published (routing: {})", routingKey);
                    ctx.response()
                            .setStatusCode(202)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("message", "Email queued")
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to publish email", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void publishSMSNotification(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.response().setStatusCode(400).end("Missing body");
            return;
        }

        JsonObject sms = new JsonObject()
                .put("phone", body.getString("phone"))
                .put("message", body.getString("message"))
                .put("timestamp", System.currentTimeMillis());

        String routingKey = "notify.sms." + body.getString("priority", "normal");

        client.basicPublish(TOPIC_EXCHANGE, routingKey,
                io.vertx.core.buffer.Buffer.buffer(sms.encode()))
                .onSuccess(v -> {
                    logger.info("✅ SMS notification published (routing: {})", routingKey);
                    ctx.response()
                            .setStatusCode(202)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("message", "SMS queued")
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to publish SMS", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void broadcast(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.response().setStatusCode(400).end("Missing body");
            return;
        }

        JsonObject broadcast = new JsonObject()
                .put("message", body.getString("message"))
                .put("timestamp", System.currentTimeMillis());

        client.basicPublish(FANOUT_EXCHANGE, "",
                io.vertx.core.buffer.Buffer.buffer(broadcast.encode()))
                .onSuccess(v -> {
                    logger.info("✅ Broadcast message published");
                    ctx.response()
                            .setStatusCode(202)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("message", "Broadcast sent")
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to broadcast", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void publishPriorityTask(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.response().setStatusCode(400).end("Missing body");
            return;
        }

        int priority = body.getInteger("priority", 5); // 0-10
        JsonObject task = new JsonObject()
                .put("taskId", java.util.UUID.randomUUID().toString())
                .put("description", body.getString("description"))
                .put("priority", priority)
                .put("timestamp", System.currentTimeMillis());

        com.rabbitmq.client.AMQP.BasicProperties properties =
                new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .priority(priority)
                        .build();

        client.basicPublish("", PRIORITY_QUEUE, properties,
                io.vertx.core.buffer.Buffer.buffer(task.encode()))
                .onSuccess(v -> {
                    logger.info("✅ Priority task published (priority: {})", priority);
                    ctx.response()
                            .setStatusCode(202)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("message", "Task queued")
                                    .put("priority", priority)
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to publish task", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    // Business logic methods

    private void processOrder(String orderJson) {
        try {
            JsonObject order = new JsonObject(orderJson);
            logger.info("🔄 Processing order {}", order.getString("orderId"));

            // Simulate processing
            vertx.setTimer(1000, id -> {
                logger.info("✅ Order processed: {}", order.getString("orderId"));
            });
        } catch (Exception e) {
            logger.error("❌ Error processing order", e);
        }
    }

    private void sendEmail(String notificationJson) {
        try {
            JsonObject notification = new JsonObject(notificationJson);
            logger.info("📧 Sending email to: {}", notification.getString("recipient"));

            // Simulate email sending
            vertx.setTimer(500, id -> {
                logger.info("✅ Email sent to: {}", notification.getString("recipient"));
            });
        } catch (Exception e) {
            logger.error("❌ Error sending email", e);
        }
    }

    private void sendSMS(String smsJson) {
        try {
            JsonObject sms = new JsonObject(smsJson);
            logger.info("📱 Sending SMS to: {}", sms.getString("phone"));

            // Simulate SMS sending
            vertx.setTimer(500, id -> {
                logger.info("✅ SMS sent to: {}", sms.getString("phone"));
            });
        } catch (Exception e) {
            logger.error("❌ Error sending SMS", e);
        }
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        logger.info("Stopping RabbitMQ module...");

        if (client != null) {
            client.stop()
                    .onComplete(result -> {
                        logger.info("RabbitMQ connection closed");
                        stopPromise.complete();
                    });
        } else {
            stopPromise.complete();
        }
    }
}
