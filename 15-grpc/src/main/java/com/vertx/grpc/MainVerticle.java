package com.vertx.grpc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC Services Module
 * Demonstrates high-performance RPC with Protocol Buffers
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        // Create gRPC server
        VertxServerBuilder builder = VertxServerBuilder.forAddress(vertx, "0.0.0.0", 50051);

        // Add services
        // Note: In a real implementation, you would add your gRPC service implementations
        // builder.addService(new UserServiceImpl());

        builder.build().start(ar -> {
            if (ar.succeeded()) {
                logger.info("gRPC server started on port 50051");
                startPromise.complete();
            } else {
                logger.error("gRPC server failed to start", ar.cause());
                startPromise.fail(ar.cause());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        stopPromise.complete();
    }
}