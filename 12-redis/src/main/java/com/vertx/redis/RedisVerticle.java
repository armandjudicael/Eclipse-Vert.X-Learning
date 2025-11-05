package com.vertx.redis;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.redis.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis Caching Module
 *
 * Key Concepts:
 * 1. Cache-aside pattern
 * 2. Write-through caching
 * 3. Cache invalidation
 * 4. Distributed locks
 * 5. Pub/Sub messaging
 * 6. Session storage
 * 7. Rate limiting with Redis
 * 8. Sorted sets for leaderboards
 */
public class RedisVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);
    private static final int CACHE_TTL = 60; // 60 seconds

    private Redis redis;
    private RedisAPI redisAPI;

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Redis Caching Module Starting ===");

        String redisHost = config().getString("redis.host", "localhost");
        int redisPort = config().getInteger("redis.port", 6379);

        // Configure Redis
        RedisOptions options = new RedisOptions()
                .setConnectionString("redis://" + redisHost + ":" + redisPort)
                .setMaxPoolSize(10)
                .setMaxWaitingHandlers(100);

        redis = Redis.createClient(vertx, options);

        // Get Redis API
        redis.connect()
                .onSuccess(conn -> {
                    redisAPI = RedisAPI.api(conn);
                    logger.info("✅ Connected to Redis at {}:{}", redisHost, redisPort);

                    // Setup pub/sub
                    setupPubSub();

                    // Setup HTTP server
                    setupHttpServer()
                            .onSuccess(v -> startPromise.complete())
                            .onFailure(startPromise::fail);
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to connect to Redis", err);
                    startPromise.fail(err);
                });
    }

    private void setupPubSub() {
        // Subscribe to notifications channel
        Redis subscriber = Redis.createClient(vertx, new RedisOptions()
                .setConnectionString("redis://localhost:6379"));

        subscriber.connect()
                .onSuccess(conn -> {
                    RedisAPI subAPI = RedisAPI.api(conn);

                    subAPI.subscribe(Arrays.asList("notifications"), res -> {
                        if (res.succeeded()) {
                            logger.info("✅ Subscribed to 'notifications' channel");
                        }
                    });

                    conn.handler(message -> {
                        if (message.type() == ResponseType.PUSH) {
                            String channel = message.get(1).toString();
                            String msg = message.get(2).toString();
                            logger.info("📨 Received from channel '{}': {}", channel, msg);
                        }
                    });
                });
    }

    private io.vertx.core.Future<Void> setupHttpServer() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Home
        router.get("/").handler(ctx ->
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject()
                                .put("message", "Redis Caching API")
                                .put("endpoints", new JsonObject()
                                        .put("cache", "GET/PUT/DELETE /api/cache/:key")
                                        .put("session", "POST /api/session")
                                        .put("rateLimit", "GET /api/rate-limit")
                                        .put("lock", "POST /api/lock/:resource")
                                        .put("leaderboard", "GET/POST /api/leaderboard")
                                        .put("publish", "POST /api/publish")
                                        .put("health", "GET /health"))
                                .encode()));

        router.get("/health").handler(this::healthCheck);

        // Cache operations
        router.get("/api/cache/:key").handler(this::getFromCache);
        router.put("/api/cache/:key").handler(this::putToCache);
        router.delete("/api/cache/:key").handler(this::deleteFromCache);

        // Session management
        router.post("/api/session").handler(this::createSession);
        router.get("/api/session/:sessionId").handler(this::getSession);

        // Rate limiting
        router.get("/api/rate-limit").handler(this::checkRateLimit);

        // Distributed lock
        router.post("/api/lock/:resource").handler(this::acquireLock);

        // Leaderboard (sorted set)
        router.get("/api/leaderboard").handler(this::getLeaderboard);
        router.post("/api/leaderboard").handler(this::updateScore);

        // Pub/Sub
        router.post("/api/publish").handler(this::publishMessage);

        // Statistics
        router.get("/api/stats").handler(this::getStats);

        return vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .map(server -> {
                    logger.info("✅ HTTP server started on port 8080");
                    return null;
                });
    }

    private void healthCheck(RoutingContext ctx) {
        redisAPI.ping(Arrays.asList("PING"))
                .onSuccess(response -> {
                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("status", "UP")
                                    .put("redis", "connected")
                                    .encode());
                })
                .onFailure(err -> {
                    ctx.response()
                            .setStatusCode(503)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("status", "DOWN")
                                    .put("error", err.getMessage())
                                    .encode());
                });
    }

    /**
     * Cache-aside pattern: GET from cache
     */
    private void getFromCache(RoutingContext ctx) {
        String key = ctx.pathParam("key");

        redisAPI.get(key)
                .onSuccess(response -> {
                    if (response == null) {
                        logger.info("❌ Cache miss for key: {}", key);

                        // Simulate fetching from database
                        String value = fetchFromDatabase(key);

                        // Store in cache
                        redisAPI.setex(key, String.valueOf(CACHE_TTL), value)
                                .onSuccess(v -> logger.info("✅ Cached key: {}", key));

                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .putHeader("X-Cache", "MISS")
                                .end(new JsonObject()
                                        .put("key", key)
                                        .put("value", value)
                                        .put("source", "database")
                                        .encode());
                    } else {
                        logger.info("✅ Cache hit for key: {}", key);
                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .putHeader("X-Cache", "HIT")
                                .end(new JsonObject()
                                        .put("key", key)
                                        .put("value", response.toString())
                                        .put("source", "cache")
                                        .encode());
                    }
                })
                .onFailure(err -> {
                    logger.error("❌ Redis error", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    /**
     * Write-through caching: PUT to cache
     */
    private void putToCache(RoutingContext ctx) {
        String key = ctx.pathParam("key");
        JsonObject body = ctx.body().asJsonObject();
        String value = body.getString("value");
        Integer ttl = body.getInteger("ttl", CACHE_TTL);

        // Write to cache
        redisAPI.setex(key, String.valueOf(ttl), value)
                .onSuccess(response -> {
                    logger.info("✅ Cached: {} = {} (TTL: {}s)", key, value, ttl);

                    // Also write to database (write-through pattern)
                    writeToDatabase(key, value);

                    ctx.response()
                            .setStatusCode(201)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("message", "Cached successfully")
                                    .put("key", key)
                                    .put("ttl", ttl)
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to cache", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    /**
     * Cache invalidation: DELETE from cache
     */
    private void deleteFromCache(RoutingContext ctx) {
        String key = ctx.pathParam("key");

        redisAPI.del(Arrays.asList(key))
                .onSuccess(response -> {
                    long deleted = response.toLong();
                    if (deleted > 0) {
                        logger.info("✅ Deleted key from cache: {}", key);
                        ctx.response()
                                .setStatusCode(204)
                                .end();
                    } else {
                        ctx.response()
                                .setStatusCode(404)
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject()
                                        .put("error", "Key not found")
                                        .encode());
                    }
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to delete", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    /**
     * Session storage with expiration
     */
    private void createSession(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String sessionId = java.util.UUID.randomUUID().toString();
        String userId = body.getString("userId");

        JsonObject sessionData = new JsonObject()
                .put("userId", userId)
                .put("createdAt", System.currentTimeMillis());

        String key = "session:" + sessionId;

        redisAPI.setex(key, "3600", sessionData.encode()) // 1 hour
                .onSuccess(v -> {
                    logger.info("✅ Session created: {}", sessionId);
                    ctx.response()
                            .setStatusCode(201)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("sessionId", sessionId)
                                    .put("expiresIn", 3600)
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to create session", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void getSession(RoutingContext ctx) {
        String sessionId = ctx.pathParam("sessionId");
        String key = "session:" + sessionId;

        redisAPI.get(key)
                .onSuccess(response -> {
                    if (response == null) {
                        ctx.response().setStatusCode(404).end();
                    } else {
                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(response.toString());
                    }
                })
                .onFailure(err -> ctx.response().setStatusCode(500).end());
    }

    /**
     * Rate limiting with sliding window
     */
    private void checkRateLimit(RoutingContext ctx) {
        String clientIp = ctx.request().remoteAddress().host();
        String key = "rate:" + clientIp;
        long now = System.currentTimeMillis();
        long windowSize = 60000; // 1 minute
        int maxRequests = 10;

        // Add current timestamp to sorted set
        redisAPI.zadd(Arrays.asList(key, String.valueOf(now), String.valueOf(now)))
                .compose(v -> {
                    // Remove old entries
                    long cutoff = now - windowSize;
                    return redisAPI.zremrangebyscore(key, "0", String.valueOf(cutoff));
                })
                .compose(v -> {
                    // Count entries in window
                    return redisAPI.zcard(key);
                })
                .compose(count -> {
                    // Set expiration
                    redisAPI.expire(Arrays.asList(key, "60"));

                    long requestCount = count.toLong();
                    boolean allowed = requestCount <= maxRequests;

                    JsonObject response = new JsonObject()
                            .put("allowed", allowed)
                            .put("requestCount", requestCount)
                            .put("limit", maxRequests)
                            .put("remaining", Math.max(0, maxRequests - requestCount));

                    if (allowed) {
                        logger.info("✅ Rate limit check passed: {}/{}", requestCount, maxRequests);
                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .putHeader("X-RateLimit-Limit", String.valueOf(maxRequests))
                                .putHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxRequests - requestCount)))
                                .end(response.encode());
                    } else {
                        logger.warn("⚠️  Rate limit exceeded: {}", clientIp);
                        ctx.response()
                                .setStatusCode(429)
                                .putHeader("Content-Type", "application/json")
                                .putHeader("Retry-After", "60")
                                .end(response.encode());
                    }

                    return io.vertx.core.Future.succeededFuture();
                })
                .onFailure(err -> {
                    logger.error("❌ Rate limit error", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    /**
     * Distributed lock with expiration
     */
    private void acquireLock(RoutingContext ctx) {
        String resource = ctx.pathParam("resource");
        String lockKey = "lock:" + resource;
        String lockValue = java.util.UUID.randomUUID().toString();

        redisAPI.set(Arrays.asList(lockKey, lockValue, "NX", "EX", "10")) // 10 seconds
                .onSuccess(response -> {
                    if (response != null && response.toString().equals("OK")) {
                        logger.info("🔒 Lock acquired for: {}", resource);

                        // Simulate work
                        vertx.setTimer(2000, id -> {
                            // Release lock
                            redisAPI.del(Arrays.asList(lockKey))
                                    .onSuccess(v -> logger.info("🔓 Lock released for: {}", resource));
                        });

                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject()
                                        .put("locked", true)
                                        .put("lockId", lockValue)
                                        .encode());
                    } else {
                        logger.warn("❌ Failed to acquire lock for: {}", resource);
                        ctx.response()
                                .setStatusCode(409)
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject()
                                        .put("locked", false)
                                        .put("message", "Resource is locked")
                                        .encode());
                    }
                })
                .onFailure(err -> {
                    logger.error("❌ Lock error", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    /**
     * Leaderboard with sorted sets
     */
    private void getLeaderboard(RoutingContext ctx) {
        String key = "leaderboard:global";

        redisAPI.zrevrange(Arrays.asList(key, "0", "9", "WITHSCORES"))
                .onSuccess(response -> {
                    JsonArray leaderboard = new JsonArray();

                    for (int i = 0; i < response.size(); i += 2) {
                        String player = response.get(i).toString();
                        int score = response.get(i + 1).toInteger();

                        leaderboard.add(new JsonObject()
                                .put("rank", (i / 2) + 1)
                                .put("player", player)
                                .put("score", score));
                    }

                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("leaderboard", leaderboard)
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Leaderboard error", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void updateScore(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String player = body.getString("player");
        int score = body.getInteger("score");
        String key = "leaderboard:global";

        redisAPI.zadd(Arrays.asList(key, String.valueOf(score), player))
                .onSuccess(v -> {
                    logger.info("🏆 Score updated: {} = {}", player, score);
                    ctx.response()
                            .setStatusCode(201)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("message", "Score updated")
                                    .put("player", player)
                                    .put("score", score)
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to update score", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    /**
     * Pub/Sub: Publish message
     */
    private void publishMessage(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String channel = body.getString("channel", "notifications");
        String message = body.getString("message");

        redisAPI.publish(channel, message)
                .onSuccess(subscribers -> {
                    logger.info("📢 Published to '{}': {} ({} subscribers)",
                            channel, message, subscribers.toLong());
                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("published", true)
                                    .put("subscribers", subscribers.toLong())
                                    .encode());
                })
                .onFailure(err -> {
                    logger.error("❌ Publish error", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    /**
     * Get Redis statistics
     */
    private void getStats(RoutingContext ctx) {
        redisAPI.info(Arrays.asList("stats"))
                .onSuccess(response -> {
                    ctx.response()
                            .putHeader("Content-Type", "text/plain")
                            .end(response.toString());
                })
                .onFailure(err -> ctx.response().setStatusCode(500).end());
    }

    // Simulated database operations
    private String fetchFromDatabase(String key) {
        // Simulate database fetch
        return "value_from_db_" + key;
    }

    private void writeToDatabase(String key, String value) {
        // Simulate database write
        logger.info("📝 Written to database: {} = {}", key, value);
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        logger.info("Stopping Redis module...");
        if (redis != null) {
            redis.close();
        }
        stopPromise.complete();
    }
}
