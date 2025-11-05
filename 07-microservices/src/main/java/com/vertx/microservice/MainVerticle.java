package com.vertx.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Verticle - Deploys all microservices
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Microservices Application Starting ===");

        // Deploy Service A
        vertx.deployVerticle(new ServiceAVerticle())
                .compose(idA -> {
                    logger.info("Service A deployed: {}", idA);
                    // Deploy Service B
                    return vertx.deployVerticle(new ServiceBVerticle());
                })
                .compose(idB -> {
                    logger.info("Service B deployed: {}", idB);
                    // Deploy API Gateway
                    return vertx.deployVerticle(new ApiGatewayVerticle());
                })
                .onSuccess(idGateway -> {
                    logger.info("API Gateway deployed: {}", idGateway);
                    logger.info("✅ All microservices started successfully!");
                    logger.info("🌐 API Gateway: http://localhost:8080");
                    logger.info("🔧 Service A: http://localhost:8081");
                    logger.info("🔧 Service B: http://localhost:8082");
                    startPromise.complete();
                })
                .onFailure(err -> {
                    logger.error("Failed to deploy microservices", err);
                    startPromise.fail(err);
                });
    }
}
