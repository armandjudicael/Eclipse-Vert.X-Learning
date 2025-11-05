package com.vertx.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Service A - Mock Backend Service
 * Simulates a data service with occasional failures
 */
public class ServiceAVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAVerticle.class);
    private final Random random = new Random();

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Service A Starting ===");

        Router router = Router.router(vertx);

        router.get("/data/:id").handler(ctx -> {
            String id = ctx.pathParam("id");
            logger.info("Service A: Processing request for ID: {}", id);

            // Simulate random failures (20% chance)
            if (random.nextInt(100) < 20) {
                logger.warn("⚠️  Service A: Simulating failure for ID: {}", id);
                ctx.response().setStatusCode(500).end("Service A internal error");
                return;
            }

            // Simulate processing delay
            vertx.setTimer(random.nextInt(500) + 100, timerId -> {
                JsonObject data = new JsonObject()
                        .put("service", "Service-A")
                        .put("id", id)
                        .put("data", new JsonObject()
                                .put("name", "Product-" + id)
                                .put("price", random.nextDouble() * 100)
                                .put("stock", random.nextInt(100)))
                        .put("timestamp", System.currentTimeMillis());

                logger.info("✅ Service A: Returning data for ID: {}", id);
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(data.encode());
            });
        });

        router.get("/health").handler(ctx -> {
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("status", "UP")
                            .put("service", "Service-A")
                            .put("port", 8081)
                            .encode());
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8081)
                .onSuccess(server -> {
                    logger.info("✅ Service A started on port 8081");
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }
}
