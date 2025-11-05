package com.vertx.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database Integration with Vert.x Reactive PostgreSQL Client
 *
 * Key Concepts:
 * 1. Reactive SQL Client
 * 2. Connection pooling
 * 3. Prepared statements
 * 4. Transactions
 * 5. Batch operations
 */
public class DatabaseVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);
    private PgPool client;

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Database Integration Module ===");

        // Configure database connection
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(5432)
                .setHost("localhost")
                .setDatabase("vertxdb")
                .setUser("vertxuser")
                .setPassword("vertxpass");

        // Connection pool options
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        // Create the client pool
        client = PgPool.pool(vertx, connectOptions, poolOptions);

        // Initialize database
        initializeDatabase()
                .compose(v -> setupHttpServer())
                .onSuccess(v -> {
                    logger.info("✅ Database verticle started successfully");
                    startPromise.complete();
                })
                .onFailure(err -> {
                    logger.error("❌ Failed to start database verticle", err);
                    startPromise.fail(err);
                });
    }

    private io.vertx.core.Future<Void> initializeDatabase() {
        logger.info("Initializing database schema...");

        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    age INTEGER,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        return client.query(createTableSQL)
                .execute()
                .compose(result -> {
                    logger.info("✅ Users table created/verified");
                    return insertSampleData();
                })
                .mapEmpty();
    }

    private io.vertx.core.Future<Void> insertSampleData() {
        String checkSQL = "SELECT COUNT(*) as count FROM users";

        return client.query(checkSQL)
                .execute()
                .compose(result -> {
                    Row row = result.iterator().next();
                    long count = row.getLong("count");

                    if (count == 0) {
                        logger.info("Inserting sample data...");
                        String insertSQL = """
                                INSERT INTO users (name, email, age) VALUES
                                ('Alice Johnson', 'alice@example.com', 28),
                                ('Bob Smith', 'bob@example.com', 35),
                                ('Charlie Brown', 'charlie@example.com', 42)
                                """;

                        return client.query(insertSQL)
                                .execute()
                                .map(r -> {
                                    logger.info("✅ Sample data inserted");
                                    return null;
                                });
                    } else {
                        logger.info("Sample data already exists");
                        return io.vertx.core.Future.succeededFuture();
                    }
                });
    }

    private io.vertx.core.Future<Void> setupHttpServer() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // API Endpoints
        router.get("/api/users").handler(ctx -> getAllUsers(ctx));
        router.get("/api/users/:id").handler(ctx -> getUserById(ctx));
        router.post("/api/users").handler(ctx -> createUser(ctx));
        router.put("/api/users/:id").handler(ctx -> updateUser(ctx));
        router.delete("/api/users/:id").handler(ctx -> deleteUser(ctx));

        // Transaction example
        router.post("/api/users/batch").handler(ctx -> batchInsert(ctx));

        return vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .map(server -> {
                    logger.info("🌐 HTTP server started on port 8080");
                    return null;
                });
    }

    private void getAllUsers(io.vertx.ext.web.RoutingContext ctx) {
        client.query("SELECT * FROM users ORDER BY id")
                .execute()
                .onSuccess(rows -> {
                    JsonArray users = new JsonArray();
                    for (Row row : rows) {
                        users.add(rowToJson(row));
                    }
                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("users", users).encode());
                })
                .onFailure(err -> {
                    logger.error("Error fetching users", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void getUserById(io.vertx.ext.web.RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));

        client.preparedQuery("SELECT * FROM users WHERE id = $1")
                .execute(Tuple.of(id))
                .onSuccess(rows -> {
                    if (rows.size() == 0) {
                        ctx.response().setStatusCode(404).end();
                    } else {
                        Row row = rows.iterator().next();
                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(rowToJson(row).encode());
                    }
                })
                .onFailure(err -> {
                    logger.error("Error fetching user", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void createUser(io.vertx.ext.web.RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        String sql = "INSERT INTO users (name, email, age) VALUES ($1, $2, $3) RETURNING *";
        Tuple params = Tuple.of(
                body.getString("name"),
                body.getString("email"),
                body.getInteger("age")
        );

        client.preparedQuery(sql)
                .execute(params)
                .onSuccess(rows -> {
                    Row row = rows.iterator().next();
                    ctx.response()
                            .setStatusCode(201)
                            .putHeader("Content-Type", "application/json")
                            .end(rowToJson(row).encode());
                    logger.info("✅ User created: {}", body.getString("email"));
                })
                .onFailure(err -> {
                    logger.error("Error creating user", err);
                    ctx.response().setStatusCode(400).end();
                });
    }

    private void updateUser(io.vertx.ext.web.RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        JsonObject body = ctx.body().asJsonObject();

        String sql = "UPDATE users SET name = $1, age = $2 WHERE id = $3 RETURNING *";
        Tuple params = Tuple.of(
                body.getString("name"),
                body.getInteger("age"),
                id
        );

        client.preparedQuery(sql)
                .execute(params)
                .onSuccess(rows -> {
                    if (rows.size() == 0) {
                        ctx.response().setStatusCode(404).end();
                    } else {
                        Row row = rows.iterator().next();
                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(rowToJson(row).encode());
                        logger.info("✅ User updated: {}", id);
                    }
                })
                .onFailure(err -> {
                    logger.error("Error updating user", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void deleteUser(io.vertx.ext.web.RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));

        client.preparedQuery("DELETE FROM users WHERE id = $1")
                .execute(Tuple.of(id))
                .onSuccess(result -> {
                    if (result.rowCount() == 0) {
                        ctx.response().setStatusCode(404).end();
                    } else {
                        ctx.response().setStatusCode(204).end();
                        logger.info("✅ User deleted: {}", id);
                    }
                })
                .onFailure(err -> {
                    logger.error("Error deleting user", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private void batchInsert(io.vertx.ext.web.RoutingContext ctx) {
        JsonArray usersArray = ctx.body().asJsonArray();

        client.getConnection()
                .compose(connection -> {
                    // Start transaction
                    return connection.begin()
                            .compose(transaction -> {
                                String sql = "INSERT INTO users (name, email, age) VALUES ($1, $2, $3)";

                                // Prepare batch
                                var futures = new java.util.ArrayList<io.vertx.core.Future<RowSet<Row>>>();

                                for (int i = 0; i < usersArray.size(); i++) {
                                    JsonObject user = usersArray.getJsonObject(i);
                                    Tuple params = Tuple.of(
                                            user.getString("name"),
                                            user.getString("email"),
                                            user.getInteger("age")
                                    );
                                    futures.add(connection.preparedQuery(sql).execute(params));
                                }

                                return io.vertx.core.Future.all(futures)
                                        .compose(v -> transaction.commit())
                                        .eventually(v -> connection.close());
                            });
                })
                .onSuccess(v -> {
                    ctx.response()
                            .setStatusCode(201)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("message", "Batch insert successful").encode());
                    logger.info("✅ Batch insert completed");
                })
                .onFailure(err -> {
                    logger.error("Error in batch insert", err);
                    ctx.response().setStatusCode(500).end();
                });
    }

    private JsonObject rowToJson(Row row) {
        return new JsonObject()
                .put("id", row.getInteger("id"))
                .put("name", row.getString("name"))
                .put("email", row.getString("email"))
                .put("age", row.getInteger("age"))
                .put("createdAt", row.getLocalDateTime("created_at").toString());
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        logger.info("Closing database connection pool...");
        client.close()
                .onComplete(result -> {
                    logger.info("Database connection pool closed");
                    stopPromise.complete();
                });
    }
}
