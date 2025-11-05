package com.vertx.security;

import com.google.common.util.concurrent.RateLimiter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Security Best Practices Module
 *
 * Key Concepts:
 * 1. Input validation and sanitization
 * 2. XSS (Cross-Site Scripting) prevention
 * 3. SQL injection prevention
 * 4. CSRF token protection
 * 5. Security headers (HSTS, CSP, X-Frame-Options)
 * 6. Rate limiting and brute force protection
 * 7. Secure session management
 * 8. OWASP Top 10 mitigation
 */
@SuppressWarnings("UnstableApiUsage")
public class SecurityVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(SecurityVerticle.class);

    // Rate limiters per IP
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    // CSRF tokens
    private final Map<String, String> csrfTokens = new ConcurrentHashMap<>();

    // Input validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            ".*(union|select|insert|update|delete|drop|;|--|/\\*|\\*/|xp_).*",
            Pattern.CASE_INSENSITIVE);

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Security Best Practices Module Starting ===");

        Router router = Router.router(vertx);

        // 1. Security Headers Middleware
        router.route().handler(this::addSecurityHeaders);

        // 2. CORS Configuration (strict)
        CorsHandler corsHandler = CorsHandler.create()
                .addOrigin("https://trusted-domain.com")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization")
                .maxAgeSeconds(3600);
        router.route().handler(corsHandler);

        // 3. Body parsing with size limit
        router.route().handler(BodyHandler.create()
                .setBodyLimit(1024 * 1024) // 1MB limit
                .setUploadsDirectory("uploads")
                .setDeleteUploadedFilesOnEnd(true));

        // 4. Request logging with sanitization
        router.route().handler(this::logRequest);

        // 5. Rate limiting middleware
        router.route().handler(this::rateLimitHandler);

        // Public endpoints
        router.get("/").handler(this::homeHandler);
        router.get("/health").handler(this::healthHandler);

        // CSRF token generation
        router.get("/api/csrf-token").handler(this::getCsrfToken);

        // Secure endpoints with validation
        router.post("/api/register")
                .handler(this::validateCsrfToken)
                .handler(this::validateRegistration)
                .handler(this::registerUser);

        router.post("/api/search")
                .handler(this::validateCsrfToken)
                .handler(this::searchHandler);

        // Demonstrate XSS prevention
        router.post("/api/comment")
                .handler(this::validateCsrfToken)
                .handler(this::addComment);

        // SQL Injection prevention demo
        router.get("/api/user/:id")
                .handler(this::validateUserId)
                .handler(this::getUser);

        // Secure file upload
        router.post("/api/upload")
                .handler(this::validateCsrfToken)
                .handler(this::validateFileUpload)
                .handler(this::handleFileUpload);

        // Error handlers
        router.route().failureHandler(this::errorHandler);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server -> {
                    logger.info("✅ Secure server started on port 8080");
                    logger.info("🔒 Security features enabled:");
                    logger.info("   - Security headers (HSTS, CSP, X-Frame-Options)");
                    logger.info("   - Input validation and sanitization");
                    logger.info("   - XSS prevention");
                    logger.info("   - SQL injection prevention");
                    logger.info("   - CSRF protection");
                    logger.info("   - Rate limiting");
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }

    /**
     * Add security headers to all responses
     */
    private void addSecurityHeaders(RoutingContext ctx) {
        // Strict-Transport-Security (HSTS)
        ctx.response().putHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains; preload");

        // Content-Security-Policy (CSP)
        ctx.response().putHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; font-src 'self'; connect-src 'self'; frame-ancestors 'none'");

        // X-Frame-Options (Clickjacking protection)
        ctx.response().putHeader("X-Frame-Options", "DENY");

        // X-Content-Type-Options (MIME sniffing protection)
        ctx.response().putHeader("X-Content-Type-Options", "nosniff");

        // X-XSS-Protection
        ctx.response().putHeader("X-XSS-Protection", "1; mode=block");

        // Referrer-Policy
        ctx.response().putHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions-Policy (formerly Feature-Policy)
        ctx.response().putHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=()");

        ctx.next();
    }

    /**
     * Rate limiting per IP address
     */
    private void rateLimitHandler(RoutingContext ctx) {
        String clientIp = ctx.request().remoteAddress().host();

        // Get or create rate limiter for this IP (10 requests per second)
        RateLimiter limiter = rateLimiters.computeIfAbsent(
                clientIp,
                ip -> RateLimiter.create(10.0));

        if (!limiter.tryAcquire()) {
            logger.warn("⚠️  Rate limit exceeded for IP: {}", clientIp);
            ctx.response()
                    .setStatusCode(429)
                    .putHeader("Retry-After", "1")
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Too many requests")
                            .put("message", "Rate limit exceeded. Please try again later.")
                            .encode());
        } else {
            ctx.next();
        }
    }

    /**
     * Log requests with sanitized data
     */
    private void logRequest(RoutingContext ctx) {
        String method = ctx.request().method().toString();
        String uri = sanitizeForLog(ctx.request().uri());
        String ip = ctx.request().remoteAddress().host();

        logger.info("{} {} from {}", method, uri, ip);
        ctx.next();
    }

    /**
     * Generate CSRF token
     */
    private void getCsrfToken(RoutingContext ctx) {
        String sessionId = ctx.request().getHeader("X-Session-ID");

        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }

        String csrfToken = UUID.randomUUID().toString();
        csrfTokens.put(sessionId, csrfToken);

        // Clean up old tokens (simple cleanup)
        if (csrfTokens.size() > 10000) {
            csrfTokens.clear();
        }

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("sessionId", sessionId)
                        .put("csrfToken", csrfToken)
                        .encode());
    }

    /**
     * Validate CSRF token
     */
    private void validateCsrfToken(RoutingContext ctx) {
        String sessionId = ctx.request().getHeader("X-Session-ID");
        String csrfToken = ctx.request().getHeader("X-CSRF-Token");

        if (sessionId == null || csrfToken == null) {
            ctx.response()
                    .setStatusCode(403)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Missing CSRF token")
                            .encode());
            return;
        }

        String expectedToken = csrfTokens.get(sessionId);

        if (expectedToken == null || !expectedToken.equals(csrfToken)) {
            logger.warn("❌ Invalid CSRF token from IP: {}",
                    ctx.request().remoteAddress().host());
            ctx.response()
                    .setStatusCode(403)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Invalid CSRF token")
                            .encode());
            return;
        }

        ctx.next();
    }

    /**
     * Validate registration input
     */
    private void validateRegistration(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            ctx.fail(400, new IllegalArgumentException("Missing request body"));
            return;
        }

        String username = body.getString("username");
        String email = body.getString("email");
        String password = body.getString("password");

        // Validate username
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            ctx.fail(400, new IllegalArgumentException(
                    "Invalid username. Must be 3-20 alphanumeric characters"));
            return;
        }

        // Validate email
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            ctx.fail(400, new IllegalArgumentException("Invalid email format"));
            return;
        }

        // Validate password strength
        if (password == null || password.length() < 8) {
            ctx.fail(400, new IllegalArgumentException(
                    "Password must be at least 8 characters"));
            return;
        }

        // Check for SQL injection attempts
        if (containsSqlInjection(username) || containsSqlInjection(email)) {
            logger.error("🚨 SQL injection attempt detected from IP: {}",
                    ctx.request().remoteAddress().host());
            ctx.fail(400, new IllegalArgumentException("Invalid input detected"));
            return;
        }

        ctx.next();
    }

    private void registerUser(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        // Sanitize output (XSS prevention)
        String username = Encode.forHtml(body.getString("username"));
        String email = Encode.forHtml(body.getString("email"));

        logger.info("✅ User registered: {}", username);

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("message", "User registered successfully")
                        .put("username", username)
                        .put("email", email)
                        .encode());
    }

    /**
     * Search with SQL injection prevention
     */
    private void searchHandler(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String query = body.getString("query");

        if (query == null || query.trim().isEmpty()) {
            ctx.fail(400, new IllegalArgumentException("Query parameter required"));
            return;
        }

        // Check for SQL injection
        if (containsSqlInjection(query)) {
            logger.error("🚨 SQL injection attempt in search: {}",
                    sanitizeForLog(query));
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Invalid search query")
                            .encode());
            return;
        }

        // Sanitize for XSS
        String sanitizedQuery = Encode.forHtml(query);

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("query", sanitizedQuery)
                        .put("results", "[]")
                        .encode());
    }

    /**
     * Add comment with XSS prevention
     */
    private void addComment(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String comment = body.getString("comment");

        if (comment == null || comment.trim().isEmpty()) {
            ctx.fail(400, new IllegalArgumentException("Comment required"));
            return;
        }

        // Encode for HTML to prevent XSS
        String safeComment = Encode.forHtml(comment);

        logger.info("✅ Comment added (sanitized)");

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("message", "Comment added")
                        .put("comment", safeComment)
                        .encode());
    }

    /**
     * Validate user ID parameter
     */
    private void validateUserId(RoutingContext ctx) {
        String userId = ctx.pathParam("id");

        // Only allow numeric IDs
        if (!userId.matches("^[0-9]+$")) {
            logger.warn("⚠️  Invalid user ID format: {}", sanitizeForLog(userId));
            ctx.fail(400, new IllegalArgumentException("Invalid user ID"));
            return;
        }

        ctx.next();
    }

    private void getUser(RoutingContext ctx) {
        String userId = ctx.pathParam("id");

        // Safe to use in query (validated as numeric)
        logger.info("Fetching user with ID: {}", userId);

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("id", userId)
                        .put("name", "John Doe")
                        .put("email", "john@example.com")
                        .encode());
    }

    /**
     * Validate file upload
     */
    private void validateFileUpload(RoutingContext ctx) {
        // Check file uploads
        if (ctx.fileUploads().isEmpty()) {
            ctx.fail(400, new IllegalArgumentException("No file uploaded"));
            return;
        }

        var upload = ctx.fileUploads().iterator().next();
        String filename = upload.fileName();
        long size = upload.size();

        // Validate file extension (whitelist)
        if (!filename.matches(".*\\.(jpg|jpeg|png|pdf)$")) {
            logger.warn("⚠️  Invalid file type: {}", filename);
            ctx.fail(400, new IllegalArgumentException(
                    "Only JPG, PNG, and PDF files allowed"));
            return;
        }

        // Validate file size (5MB max)
        if (size > 5 * 1024 * 1024) {
            ctx.fail(400, new IllegalArgumentException("File too large (max 5MB)"));
            return;
        }

        ctx.next();
    }

    private void handleFileUpload(RoutingContext ctx) {
        var upload = ctx.fileUploads().iterator().next();

        logger.info("✅ File uploaded: {}", upload.fileName());

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("message", "File uploaded successfully")
                        .put("filename", Encode.forHtml(upload.fileName()))
                        .put("size", upload.size())
                        .encode());
    }

    private void homeHandler(RoutingContext ctx) {
        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("message", "Security Best Practices API")
                        .put("endpoints", new JsonObject()
                                .put("csrf", "GET /api/csrf-token")
                                .put("register", "POST /api/register")
                                .put("search", "POST /api/search")
                                .put("comment", "POST /api/comment")
                                .put("user", "GET /api/user/:id")
                                .put("upload", "POST /api/upload"))
                        .encode());
    }

    private void healthHandler(RoutingContext ctx) {
        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("status", "UP")
                        .put("security", "enabled")
                        .encode());
    }

    private void errorHandler(RoutingContext ctx) {
        Throwable failure = ctx.failure();
        int statusCode = ctx.statusCode();

        if (statusCode == -1) {
            statusCode = 500;
        }

        logger.error("Error: {}", failure != null ? failure.getMessage() : "Unknown");

        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("error", failure != null ?
                                Encode.forHtml(failure.getMessage()) : "Internal error")
                        .encode());
    }

    // Utility methods

    private boolean containsSqlInjection(String input) {
        return input != null && SQL_INJECTION_PATTERN.matcher(input).matches();
    }

    private String sanitizeForLog(String input) {
        if (input == null) return "";
        return input.replaceAll("[\\r\\n]", "")
                    .replaceAll("[^a-zA-Z0-9/?=&.-]", "_");
    }
}
