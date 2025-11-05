package com.vertx.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Server Verticle - Demonstrates REST API with Vert.x Web
 *
 * Key Concepts:
 * 1. Creating HTTP server
 * 2. Router and routing
 * 3. RESTful API design
 * 4. Request/Response handling
 * 5. CORS configuration
 * 6. Body parsing
 * 7. Path parameters and query parameters
 */
public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);
    private static final int PORT = 8080;

    // In-memory storage
    private final Map<String, Product> products = new HashMap<>();

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== HTTP Server Verticle Starting ===");

        // Initialize sample data
        initializeSampleData();

        // Create router
        Router router = Router.router(vertx);

        // Configure middleware
        setupMiddleware(router);

        // Configure routes
        setupRoutes(router);

        // Create and start HTTP server
        HttpServerOptions options = new HttpServerOptions()
                .setCompressionSupported(true)
                .setPort(PORT);

        HttpServer server = vertx.createHttpServer(options);

        server.requestHandler(router)
                .listen(PORT, http -> {
                    if (http.succeeded()) {
                        logger.info("✅ HTTP server started on port {}", PORT);
                        logger.info("🌐 Access the API at: http://localhost:{}", PORT);
                        startPromise.complete();
                    } else {
                        logger.error("❌ Failed to start HTTP server", http.cause());
                        startPromise.fail(http.cause());
                    }
                });
    }

    private void initializeSampleData() {
        Product p1 = new Product("Laptop", "High-performance laptop", 1299.99, 10);
        Product p2 = new Product("Mouse", "Wireless mouse", 29.99, 50);
        Product p3 = new Product("Keyboard", "Mechanical keyboard", 89.99, 30);

        products.put(p1.getId(), p1);
        products.put(p2.getId(), p2);
        products.put(p3.getId(), p3);

        logger.info("Initialized {} products", products.size());
    }

    private void setupMiddleware(Router router) {
        // CORS handler
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
                .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
                .allowedHeader("Content-Type"));

        // Body handler for parsing request bodies
        router.route().handler(BodyHandler.create());

        // Request logger
        router.route().handler(ctx -> {
            logger.info("{} {} from {}",
                    ctx.request().method(),
                    ctx.request().uri(),
                    ctx.request().remoteAddress());
            ctx.next();
        });
    }

    private void setupRoutes(Router router) {
        // Health check
        router.get("/health").handler(this::healthCheck);

        // API Info
        router.get("/").handler(this::apiInfo);

        // Product CRUD endpoints
        router.get("/api/products").handler(this::getAllProducts);
        router.get("/api/products/:id").handler(this::getProductById);
        router.post("/api/products").handler(this::createProduct);
        router.put("/api/products/:id").handler(this::updateProduct);
        router.delete("/api/products/:id").handler(this::deleteProduct);

        // Search endpoint (query parameters)
        router.get("/api/products/search").handler(this::searchProducts);

        // 404 handler
        router.route().handler(this::notFound);

        // Error handler
        router.errorHandler(500, ctx -> {
            logger.error("Internal server error", ctx.failure());
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Internal server error")
                            .put("message", ctx.failure().getMessage())
                            .encode());
        });
    }

    private void healthCheck(RoutingContext ctx) {
        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("status", "UP")
                        .put("timestamp", System.currentTimeMillis())
                        .encode());
    }

    private void apiInfo(RoutingContext ctx) {
        JsonObject info = new JsonObject()
                .put("name", "Vert.x Product REST API")
                .put("version", "1.0.0")
                .put("endpoints", new JsonArray()
                        .add("/health - Health check")
                        .add("GET /api/products - Get all products")
                        .add("GET /api/products/:id - Get product by ID")
                        .add("POST /api/products - Create new product")
                        .add("PUT /api/products/:id - Update product")
                        .add("DELETE /api/products/:id - Delete product")
                        .add("GET /api/products/search?name=xxx - Search products"));

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(info.encodePrettily());
    }

    private void getAllProducts(RoutingContext ctx) {
        JsonArray productList = new JsonArray();
        products.values().forEach(p -> productList.add(p.toJson()));

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("count", productList.size())
                        .put("products", productList)
                        .encode());
    }

    private void getProductById(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        Product product = products.get(id);

        if (product == null) {
            ctx.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Product not found")
                            .put("id", id)
                            .encode());
        } else {
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(product.toJson().encode());
        }
    }

    private void createProduct(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        if (body == null || !isValidProduct(body)) {
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Invalid product data")
                            .encode());
            return;
        }

        Product product = Product.fromJson(body);
        products.put(product.getId(), product);

        logger.info("Created product: {}", product.getId());

        ctx.response()
                .setStatusCode(201)
                .putHeader("Content-Type", "application/json")
                .putHeader("Location", "/api/products/" + product.getId())
                .end(product.toJson().encode());
    }

    private void updateProduct(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        Product existingProduct = products.get(id);

        if (existingProduct == null) {
            ctx.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Product not found")
                            .put("id", id)
                            .encode());
            return;
        }

        JsonObject body = ctx.body().asJsonObject();
        if (body == null) {
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Invalid request body")
                            .encode());
            return;
        }

        // Update fields
        if (body.containsKey("name")) existingProduct.setName(body.getString("name"));
        if (body.containsKey("description")) existingProduct.setDescription(body.getString("description"));
        if (body.containsKey("price")) existingProduct.setPrice(body.getDouble("price"));
        if (body.containsKey("quantity")) existingProduct.setQuantity(body.getInteger("quantity"));

        logger.info("Updated product: {}", id);

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(existingProduct.toJson().encode());
    }

    private void deleteProduct(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        Product removed = products.remove(id);

        if (removed == null) {
            ctx.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Product not found")
                            .put("id", id)
                            .encode());
        } else {
            logger.info("Deleted product: {}", id);
            ctx.response()
                    .setStatusCode(204)
                    .end();
        }
    }

    private void searchProducts(RoutingContext ctx) {
        String nameQuery = ctx.queryParams().get("name");

        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Missing 'name' query parameter")
                            .encode());
            return;
        }

        JsonArray results = new JsonArray();
        products.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(nameQuery.toLowerCase()))
                .forEach(p -> results.add(p.toJson()));

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("query", nameQuery)
                        .put("count", results.size())
                        .put("results", results)
                        .encode());
    }

    private void notFound(RoutingContext ctx) {
        ctx.response()
                .setStatusCode(404)
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("error", "Not found")
                        .put("path", ctx.request().uri())
                        .encode());
    }

    private boolean isValidProduct(JsonObject json) {
        return json.containsKey("name") &&
               json.containsKey("price") &&
               json.containsKey("quantity");
    }
}
