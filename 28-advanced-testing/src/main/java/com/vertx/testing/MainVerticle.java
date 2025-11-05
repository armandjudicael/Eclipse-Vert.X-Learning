package com.vertx.testing;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advanced Testing Module - Main Application
 * Demonstrates TDD, DDD, BDD, and other testing methodologies
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
    private UserService userService;

    @Override
    public void start(Promise<Void> startPromise) {
        userService = new UserService();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Health check
        router.get("/health").handler(ctx -> {
            ctx.json(new JsonObject().put("status", "UP"));
        });

        // User endpoints
        router.post("/api/users").handler(ctx -> {
            JsonObject body = ctx.getBodyAsJson();
            try {
                User user = new User(
                    body.getString("id"),
                    body.getString("name"),
                    body.getString("email"),
                    body.getInteger("age")
                );
                userService.createUser(user);
                ctx.response().setStatusCode(201)
                    .json(user.toJson());
            } catch (IllegalArgumentException e) {
                ctx.response().setStatusCode(400)
                    .json(new JsonObject().put("error", e.getMessage()));
            }
        });

        router.get("/api/users/:id").handler(ctx -> {
            String userId = ctx.pathParam("id");
            User user = userService.getUserById(userId);
            if (user != null) {
                ctx.json(user.toJson());
            } else {
                ctx.response().setStatusCode(404)
                    .json(new JsonObject().put("error", "User not found"));
            }
        });

        router.get("/api/users").handler(ctx -> {
            ctx.json(new JsonObject()
                .put("users", userService.getAllUsers())
                .put("count", userService.getAllUsers().size()));
        });

        router.put("/api/users/:id").handler(ctx -> {
            String userId = ctx.pathParam("id");
            JsonObject body = ctx.getBodyAsJson();
            try {
                User updatedUser = userService.updateUser(userId, body);
                ctx.json(updatedUser.toJson());
            } catch (IllegalArgumentException e) {
                ctx.response().setStatusCode(400)
                    .json(new JsonObject().put("error", e.getMessage()));
            }
        });

        router.delete("/api/users/:id").handler(ctx -> {
            String userId = ctx.pathParam("id");
            boolean deleted = userService.deleteUser(userId);
            if (deleted) {
                ctx.response().setStatusCode(204).end();
            } else {
                ctx.response().setStatusCode(404)
                    .json(new JsonObject().put("error", "User not found"));
            }
        });

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, http -> {
                if (http.succeeded()) {
                    logger.info("Advanced Testing server started on port 8080");
                    startPromise.complete();
                } else {
                    logger.error("Server failed to start", http.cause());
                    startPromise.fail(http.cause());
                }
            });
    }
}