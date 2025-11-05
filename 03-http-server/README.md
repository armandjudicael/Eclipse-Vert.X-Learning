# Module 03: HTTP Server and REST API

## Overview
This module demonstrates building RESTful web services with Vert.x using the Vert.x Web module. Learn how to create HTTP servers, handle routes, and implement CRUD operations.

## Key Concepts Covered

### 1. **HTTP Server Creation**
- Creating HTTP servers with Vert.x
- Server configuration and options
- Request handling
- Server lifecycle management

### 2. **Router and Routing**
- Vert.x Web Router
- Route matching and methods
- Path parameters
- Query parameters
- Route ordering

### 3. **RESTful API Design**
- CRUD operations (Create, Read, Update, Delete)
- HTTP methods (GET, POST, PUT, DELETE)
- Status codes
- Response formatting
- Error handling

### 4. **Middleware (Handlers)**
- Body parsing
- CORS configuration
- Request logging
- Error handling
- Handler chains

### 5. **Request/Response**
- Reading request data
- Path and query parameters
- Request body parsing
- Setting response headers
- JSON responses

## Project Structure
```
03-http-server/
├── src/main/java/com/vertx/http/
│   ├── HttpServerVerticle.java    # Main HTTP server
│   └── Product.java               # Data model
├── Dockerfile
├── docker-compose.yml
├── test-api.sh                    # API test script
└── README.md
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check endpoint |
| GET | `/` | API information |
| GET | `/api/products` | Get all products |
| GET | `/api/products/:id` | Get product by ID |
| POST | `/api/products` | Create new product |
| PUT | `/api/products/:id` | Update product |
| DELETE | `/api/products/:id` | Delete product |
| GET | `/api/products/search?name=xxx` | Search products by name |

## Code Examples Explained

### Creating HTTP Server
```java
HttpServer server = vertx.createHttpServer(options);
server.requestHandler(router).listen(PORT, result -> {
    // Server started
});
```

### Router Configuration
```java
Router router = Router.router(vertx);
router.get("/api/products").handler(this::getAllProducts);
router.post("/api/products").handler(this::createProduct);
```

### Handling Requests
```java
private void getAllProducts(RoutingContext ctx) {
    ctx.response()
        .putHeader("Content-Type", "application/json")
        .end(jsonData);
}
```

### Path Parameters
```java
router.get("/api/products/:id").handler(ctx -> {
    String id = ctx.pathParam("id");
    // Use id to fetch product
});
```

### Query Parameters
```java
router.get("/api/products/search").handler(ctx -> {
    String name = ctx.queryParams().get("name");
    // Search by name
});
```

### Request Body
```java
router.post("/api/products").handler(ctx -> {
    JsonObject body = ctx.body().asJsonObject();
    // Process body
});
```

## Running the Application

### Using Maven
```bash
cd 03-http-server
mvn clean package
java -jar target/http-server-fat.jar
```

### Using Docker
```bash
cd 03-http-server
docker-compose up --build
```

The server will start on `http://localhost:8080`

## Testing the API

### Using cURL

#### 1. Health Check
```bash
curl http://localhost:8080/health
```

#### 2. Get All Products
```bash
curl http://localhost:8080/api/products
```

#### 3. Get Product by ID
```bash
curl http://localhost:8080/api/products/{product-id}
```

#### 4. Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Monitor",
    "description": "27-inch 4K monitor",
    "price": 399.99,
    "quantity": 15
  }'
```

#### 5. Update Product
```bash
curl -X PUT http://localhost:8080/api/products/{product-id} \
  -H "Content-Type: application/json" \
  -d '{
    "price": 349.99,
    "quantity": 20
  }'
```

#### 6. Delete Product
```bash
curl -X DELETE http://localhost:8080/api/products/{product-id}
```

#### 7. Search Products
```bash
curl "http://localhost:8080/api/products/search?name=laptop"
```

### Using Test Script
```bash
chmod +x test-api.sh
./test-api.sh
```

### Using Postman or Insomnia
Import the following collection or create requests manually for all endpoints.

## Expected Responses

### GET /api/products
```json
{
  "count": 3,
  "products": [
    {
      "id": "uuid-1",
      "name": "Laptop",
      "description": "High-performance laptop",
      "price": 1299.99,
      "quantity": 10
    }
  ]
}
```

### POST /api/products (Success - 201)
```json
{
  "id": "uuid-4",
  "name": "Monitor",
  "description": "27-inch 4K monitor",
  "price": 399.99,
  "quantity": 15
}
```

### GET /api/products/:id (Not Found - 404)
```json
{
  "error": "Product not found",
  "id": "invalid-id"
}
```

## Important Concepts

### 1. **Middleware Chain**
```java
router.route()
    .handler(CorsHandler.create("*"))
    .handler(BodyHandler.create())
    .handler(ctx -> {
        // Custom middleware
        ctx.next();
    });
```

### 2. **Error Handling**
```java
router.errorHandler(500, ctx -> {
    ctx.response()
        .setStatusCode(500)
        .end(errorJson);
});
```

### 3. **Response Status Codes**
- `200 OK` - Successful GET, PUT
- `201 Created` - Successful POST
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Invalid input
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

### 4. **CORS Configuration**
```java
CorsHandler.create("*")
    .allowedMethod(HttpMethod.GET)
    .allowedMethod(HttpMethod.POST)
    .allowedHeader("Content-Type");
```

## Best Practices

1. **Always Set Content-Type**
   ```java
   ctx.response().putHeader("Content-Type", "application/json");
   ```

2. **Validate Input**
   ```java
   if (!isValidProduct(body)) {
       ctx.response().setStatusCode(400).end();
       return;
   }
   ```

3. **Use Appropriate Status Codes**
   - Follow REST conventions
   - Return meaningful error messages

4. **Handle Errors Gracefully**
   ```java
   try {
       // Process request
   } catch (Exception e) {
       ctx.fail(500, e);
   }
   ```

5. **Use Path/Query Parameters Correctly**
   - Path params for resource identification
   - Query params for filtering/searching

6. **Log Requests**
   ```java
   router.route().handler(ctx -> {
       logger.info("{} {}", ctx.request().method(), ctx.request().uri());
       ctx.next();
   });
   ```

## Learning Objectives

After completing this module, you should understand:
- ✅ How to create HTTP servers with Vert.x
- ✅ Router configuration and route handling
- ✅ RESTful API design principles
- ✅ CRUD operations implementation
- ✅ Request/Response handling
- ✅ Middleware and handler chains
- ✅ Error handling in web applications
- ✅ Path and query parameter usage

## Common Patterns

### 1. **Resource-Based Routing**
```java
router.get("/api/resources").handler(this::getAll);
router.get("/api/resources/:id").handler(this::getById);
router.post("/api/resources").handler(this::create);
router.put("/api/resources/:id").handler(this::update);
router.delete("/api/resources/:id").handler(this::delete);
```

### 2. **Async Response**
```java
router.get("/api/slow").handler(ctx -> {
    vertx.setTimer(1000, id -> {
        ctx.response().end("Delayed response");
    });
});
```

### 3. **File Upload**
```java
router.post("/upload").handler(ctx -> {
    for (FileUpload upload : ctx.fileUploads()) {
        // Process uploaded file
    }
});
```

## Performance Considerations

1. **Connection Pooling** - Vert.x handles connections efficiently
2. **Compression** - Enable compression for responses
3. **Caching** - Implement caching for frequently accessed data
4. **Async Processing** - Never block the event loop

## Next Steps
Proceed to [Module 04: Vert.x Web Router and Advanced Routing](../04-advanced-routing/) to learn about advanced routing patterns, authentication, and session management.

## References
- [Vert.x Web Documentation](https://vertx.io/docs/vertx-web/java/)
- [HTTP Server Documentation](https://vertx.io/docs/vertx-core/java/#_writing_http_servers_and_clients)
- [REST API Best Practices](https://restfulapi.net/)
