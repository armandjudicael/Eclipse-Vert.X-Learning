package com.vertx.mongodb;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MongoDB Integration Module
 * Demonstrates reactive MongoDB operations with Vert.x
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
    private MongoClient mongoClient;

    @Override
    public void start(Promise<Void> startPromise) {
        // MongoDB connection configuration
        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", "mongodb://mongodb:27017")
                .put("db_name", "vertx_learning");

        // Create MongoDB client
        mongoClient = MongoClient.createShared(vertx, mongoConfig);

        // Create HTTP server with routes
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Health check
        router.get("/health").handler(ctx -> {
            ctx.json(new JsonObject().put("status", "UP"));
        });

        // Create collection and insert sample data
        router.post("/api/users").handler(ctx -> {
            JsonObject user = ctx.getBodyAsJson();
            mongoClient.insertOne("users", user, res -> {
                if (res.succeeded()) {
                    ctx.response().setStatusCode(201)
                            .json(new JsonObject()
                                    .put("id", res.result())
                                    .put("message", "User created"));
                } else {
                    ctx.response().setStatusCode(500)
                            .json(new JsonObject()
                                    .put("error", res.cause().getMessage()));
                }
            });
        });

        // Get all users
        router.get("/api/users").handler(ctx -> {
            mongoClient.find("users", new JsonObject(), res -> {
                if (res.succeeded()) {
                    ctx.json(new JsonObject()
                            .put("users", res.result())
                            .put("count", res.result().size()));
                } else {
                    ctx.response().setStatusCode(500)
                            .json(new JsonObject()
                                    .put("error", res.cause().getMessage()));
                }
            });
        });

        // Get user by ID
        router.get("/api/users/:id").handler(ctx -> {
            String userId = ctx.pathParam("id");
            JsonObject query = new JsonObject().put("_id", userId);
            mongoClient.findOne("users", query, null, res -> {
                if (res.succeeded()) {
                    if (res.result() != null) {
                        ctx.json(res.result());
                    } else {
                        ctx.response().setStatusCode(404)
                                .json(new JsonObject()
                                        .put("error", "User not found"));
                    }
                } else {
                    ctx.response().setStatusCode(500)
                            .json(new JsonObject()
                                    .put("error", res.cause().getMessage()));
                }
            });
        });

        // Update user
        router.put("/api/users/:id").handler(ctx -> {
            String userId = ctx.pathParam("id");
            JsonObject query = new JsonObject().put("_id", userId);
            JsonObject update = new JsonObject()
                    .put("$set", ctx.getBodyAsJson());

            mongoClient.updateCollection("users", query, update, res -> {
                if (res.succeeded()) {
                    ctx.json(new JsonObject()
                            .put("message", "User updated")
                            .put("modified", res.result().getDocModified()));
                } else {
                    ctx.response().setStatusCode(500)
                            .json(new JsonObject()
                                    .put("error", res.cause().getMessage()));
                }
            });
        });

        // Delete user
        router.delete("/api/users/:id").handler(ctx -> {
            String userId = ctx.pathParam("id");
            JsonObject query = new JsonObject().put("_id", userId);
            mongoClient.removeOne("users", query, res -> {
                if (res.succeeded()) {
                    ctx.json(new JsonObject()
                            .put("message", "User deleted")
                            .put("removed", res.result().getRemovedCount()));
                } else {
                    ctx.response().setStatusCode(500)
                            .json(new JsonObject()
                                    .put("error", res.cause().getMessage()));
                }
            });
        });

        // Aggregation example
        router.get("/api/users/stats/count").handler(ctx -> {
            mongoClient.count("users", new JsonObject(), res -> {
                if (res.succeeded()) {
                    ctx.json(new JsonObject()
                            .put("total_users", res.result()));
                } else {
                    ctx.response().setStatusCode(500)
                            .json(new JsonObject()
                                    .put("error", res.cause().getMessage()));
                }
            });
        });

        // Start HTTP server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, http -> {
                    if (http.succeeded()) {
                        logger.info("HTTP server started on port 8080");
                        startPromise.complete();
                    } else {
                        logger.error("HTTP server failed to start", http.cause());
                        startPromise.fail(http.cause());
                    }
                });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (mongoClient != null) {
            mongoClient.close();
        }
        stopPromise.complete();
    }
}