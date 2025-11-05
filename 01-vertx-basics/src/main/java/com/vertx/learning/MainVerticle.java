package com.vertx.learning;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Verticle - Introduction to Vert.x Basics
 *
 * Key Concepts:
 * 1. Verticle: The basic unit of deployment in Vert.x
 * 2. Event Loop: Non-blocking I/O model
 * 3. Asynchronous operations with Promises
 * 4. Periodic tasks
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
    private long timerId;

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Vert.x Basics Module ===");
        logger.info("Verticle starting on thread: {}", Thread.currentThread().getName());

        // Example 1: Simple async operation
        vertx.setTimer(1000, id -> {
            logger.info("Timer fired after 1 second on thread: {}", Thread.currentThread().getName());
        });

        // Example 2: Periodic task
        timerId = vertx.setPeriodic(3000, id -> {
            logger.info("Periodic task executed every 3 seconds");
            logger.info("Current time: {}", System.currentTimeMillis());
        });

        // Example 3: Execute blocking code
        vertx.executeBlocking(promise -> {
            logger.info("Executing blocking code on worker thread: {}", Thread.currentThread().getName());
            // Simulate blocking operation
            try {
                Thread.sleep(2000);
                promise.complete("Blocking operation completed");
            } catch (InterruptedException e) {
                promise.fail(e);
            }
        }, result -> {
            if (result.succeeded()) {
                logger.info("Blocking result: {}", result.result());
            } else {
                logger.error("Blocking operation failed", result.cause());
            }
        });

        // Example 4: Demonstrating context data
        vertx.getOrCreateContext().put("verticle-id", this.deploymentID());
        logger.info("Verticle deployment ID: {}", vertx.getOrCreateContext().get("verticle-id"));

        // Complete the start promise
        startPromise.complete();
        logger.info("MainVerticle started successfully!");
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        logger.info("Stopping MainVerticle...");

        // Cancel periodic timer
        if (timerId != 0) {
            vertx.cancelTimer(timerId);
            logger.info("Periodic timer cancelled");
        }

        stopPromise.complete();
        logger.info("MainVerticle stopped successfully!");
    }
}
