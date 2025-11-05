package com.vertx.learning;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker Verticle - Demonstrates blocking operations
 *
 * Key Concepts:
 * 1. Worker verticles run on worker thread pool
 * 2. Can perform blocking operations without blocking event loop
 * 3. Deployed with worker=true option
 */
public class WorkerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(WorkerVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Worker Verticle Starting ===");
        logger.info("Running on thread: {}", Thread.currentThread().getName());
        logger.info("Is worker context: {}", vertx.getOrCreateContext().isWorkerContext());

        // Simulate heavy computation
        performHeavyComputation();

        startPromise.complete();
        logger.info("WorkerVerticle started successfully!");
    }

    private void performHeavyComputation() {
        logger.info("Starting heavy computation...");
        try {
            // Simulate CPU-intensive task
            Thread.sleep(3000);
            long sum = 0;
            for (int i = 0; i < 1_000_000; i++) {
                sum += i;
            }
            logger.info("Heavy computation completed. Result: {}", sum);
        } catch (InterruptedException e) {
            logger.error("Computation interrupted", e);
        }
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        logger.info("Stopping WorkerVerticle...");
        stopPromise.complete();
    }
}
