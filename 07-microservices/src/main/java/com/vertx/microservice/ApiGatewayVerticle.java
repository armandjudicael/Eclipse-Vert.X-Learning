package com.vertx.microservice;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API Gateway with Circuit Breaker Pattern
 *
 * Key Concepts:
 * 1. API Gateway pattern
 * 2. Circuit Breaker for resilience
 * 3. Service-to-service communication
 * 4. Timeout handling
 * 5. Fallback mechanisms
 */
public class ApiGatewayVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayVerticle.class);
    private WebClient client;
    private CircuitBreaker breakerServiceA;
    private CircuitBreaker breakerServiceB;

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== API Gateway Starting ===");

        client = WebClient.create(vertx);

        // Circuit Breaker for Service A
        breakerServiceA = CircuitBreaker.create("service-a-breaker", vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(3)
                        .setTimeout(2000)
                        .setFallbackOnFailure(true)
                        .setResetTimeout(5000))
                .openHandler(v -> logger.warn("🔴 Circuit breaker for Service A is OPEN"))
                .halfOpenHandler(v -> logger.info("🟡 Circuit breaker for Service A is HALF-OPEN"))
                .closeHandler(v -> logger.info("🟢 Circuit breaker for Service A is CLOSED"));

        // Circuit Breaker for Service B
        breakerServiceB = CircuitBreaker.create("service-b-breaker", vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(3)
                        .setTimeout(2000)
                        .setFallbackOnFailure(true)
                        .setResetTimeout(5000))
                .openHandler(v -> logger.warn("🔴 Circuit breaker for Service B is OPEN"))
                .halfOpenHandler(v -> logger.info("🟡 Circuit breaker for Service B is HALF-OPEN"))
                .closeHandler(v -> logger.info("🟢 Circuit breaker for Service B is CLOSED"));

        Router router = Router.router(vertx);

        // Gateway endpoints
        router.get("/api/service-a/:id").handler(ctx -> {
            String id = ctx.pathParam("id");
            logger.info("Gateway: Routing request to Service A for ID: {}", id);

            breakerServiceA.execute(promise -> {
                client.get(8081, "localhost", "/data/" + id)
                        .as(BodyCodec.jsonObject())
                        .timeout(2000)
                        .send()
                        .onSuccess(response -> promise.complete(response.body()))
                        .onFailure(promise::fail);
            }).onSuccess(result -> {
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(((JsonObject) result).encode());
            }).onFailure(err -> {
                logger.error("Service A failed: {}", err.getMessage());
                ctx.response()
                        .setStatusCode(503)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject()
                                .put("error", "Service A unavailable")
                                .put("fallback", true)
                                .encode());
            });
        });

        router.get("/api/service-b/:id").handler(ctx -> {
            String id = ctx.pathParam("id");
            logger.info("Gateway: Routing request to Service B for ID: {}", id);

            breakerServiceB.execute(promise -> {
                client.get(8082, "localhost", "/process/" + id)
                        .as(BodyCodec.jsonObject())
                        .timeout(2000)
                        .send()
                        .onSuccess(response -> promise.complete(response.body()))
                        .onFailure(promise::fail);
            }).onSuccess(result -> {
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(((JsonObject) result).encode());
            }).onFailure(err -> {
                logger.error("Service B failed: {}", err.getMessage());
                ctx.response()
                        .setStatusCode(503)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject()
                                .put("error", "Service B unavailable")
                                .put("fallback", true)
                                .encode());
            });
        });

        // Aggregate data from both services
        router.get("/api/aggregate/:id").handler(ctx -> {
            String id = ctx.pathParam("id");
            logger.info("Gateway: Aggregating data from both services for ID: {}", id);

            io.vertx.core.Future<JsonObject> futureA = breakerServiceA.execute(promise -> {
                client.get(8081, "localhost", "/data/" + id)
                        .as(BodyCodec.jsonObject())
                        .timeout(2000)
                        .send()
                        .onSuccess(response -> promise.complete(response.body()))
                        .onFailure(promise::fail);
            });

            io.vertx.core.Future<JsonObject> futureB = breakerServiceB.execute(promise -> {
                client.get(8082, "localhost", "/process/" + id)
                        .as(BodyCodec.jsonObject())
                        .timeout(2000)
                        .send()
                        .onSuccess(response -> promise.complete(response.body()))
                        .onFailure(promise::fail);
            });

            io.vertx.core.Future.all(futureA, futureB)
                    .onSuccess(composite -> {
                        JsonObject dataA = futureA.result();
                        JsonObject dataB = futureB.result();

                        JsonObject aggregated = new JsonObject()
                                .put("id", id)
                                .put("serviceA", dataA)
                                .put("serviceB", dataB)
                                .put("timestamp", System.currentTimeMillis());

                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(aggregated.encode());
                    })
                    .onFailure(err -> {
                        logger.error("Aggregation failed: {}", err.getMessage());
                        ctx.response()
                                .setStatusCode(503)
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject()
                                        .put("error", "Aggregation failed")
                                        .put("message", err.getMessage())
                                        .encode());
                    });
        });

        // Health check with breaker status
        router.get("/health").handler(ctx -> {
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("status", "UP")
                            .put("gateway", "API Gateway")
                            .put("circuitBreakers", new JsonObject()
                                    .put("serviceA", breakerServiceA.state().name())
                                    .put("serviceB", breakerServiceB.state().name()))
                            .encode());
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server -> {
                    logger.info("✅ API Gateway started on port 8080");
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }

    @Override
    public void stop() {
        logger.info("Stopping API Gateway...");
        if (client != null) client.close();
    }
}
