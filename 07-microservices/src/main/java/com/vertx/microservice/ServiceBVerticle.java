package com.vertx.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Service B - Mock Backend Service
 * Simulates a processing service with occasional delays
 */
public class ServiceBVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBVerticle.class);
    private final Random random = new Random();

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Service B Starting ===");

        Router router = Router.router(vertx);

        router.get("/process/:id").handler(ctx -> {
            String id = ctx.pathParam("id");
            logger.info("Service B: Processing request for ID: {}", id);

            // Simulate random slow responses (15% chance)
            int delay = random.nextInt(100) < 15 ? 3000 : random.nextInt(300) + 100;

            vertx.setTimer(delay, timerId -> {
                JsonObject data = new JsonObject()
                        .put("service", "Service-B")
                        .put("id", id)
                        .put("processing", new JsonObject()
                                .put("status", "completed")
                                .put("duration", delay + "ms")
                                .put("result", "Processed-" + id))
                        .put("timestamp", System.currentTimeMillis());

                logger.info("✅ Service B: Returning processed data for ID: {}", id);
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
                            .put("service", "Service-B")
                            .put("port", 8082)
                            .encode());
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8082)
                .onSuccess(server -> {
                    logger.info("✅ Service B started on port 8082");
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }
}
