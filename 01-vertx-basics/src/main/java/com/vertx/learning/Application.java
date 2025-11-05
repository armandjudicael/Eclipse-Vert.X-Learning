package com.vertx.learning;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application Entry Point
 *
 * Demonstrates:
 * 1. Creating Vert.x instance
 * 2. Deploying standard verticles
 * 3. Deploying worker verticles
 * 4. Deployment options and configuration
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        // Create Vert.x instance
        Vertx vertx = Vertx.vertx();

        logger.info("=== Starting Vert.x Basics Application ===");

        // Deploy MainVerticle (standard event loop verticle)
        vertx.deployVerticle(new MainVerticle(), result -> {
            if (result.succeeded()) {
                logger.info("MainVerticle deployed successfully with ID: {}", result.result());
            } else {
                logger.error("Failed to deploy MainVerticle", result.cause());
            }
        });

        // Deploy WorkerVerticle with worker thread pool
        DeploymentOptions workerOptions = new DeploymentOptions()
                .setWorker(true)
                .setInstances(2); // Deploy 2 instances

        vertx.deployVerticle(WorkerVerticle.class.getName(), workerOptions, result -> {
            if (result.succeeded()) {
                logger.info("WorkerVerticle deployed successfully with ID: {}", result.result());
            } else {
                logger.error("Failed to deploy WorkerVerticle", result.cause());
            }
        });

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Vert.x...");
            vertx.close(result -> {
                if (result.succeeded()) {
                    logger.info("Vert.x closed successfully");
                } else {
                    logger.error("Failed to close Vert.x", result.cause());
                }
            });
        }));
    }
}
