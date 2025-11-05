package com.vertx.auth;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication and JWT Module
 *
 * Key Concepts:
 * 1. JWT (JSON Web Tokens) authentication
 * 2. Password hashing with BCrypt
 * 3. User registration and login
 * 4. Protected routes with JWT validation
 * 5. Token refresh mechanism
 * 6. Role-based access control (RBAC)
 */
public class AuthVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AuthVerticle.class);
    private static final String JWT_SECRET = "your-256-bit-secret-key-change-this-in-production";

    // In-memory user store (replace with database in production)
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private JWTAuth jwtAuth;

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("=== Authentication & JWT Module Starting ===");

        // Configure JWT Auth
        jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer(JWT_SECRET)));

        // Initialize sample users
        initializeSampleUsers();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Public routes
        router.post("/api/auth/register").handler(this::register);
        router.post("/api/auth/login").handler(this::login);
        router.post("/api/auth/refresh").handler(this::refreshToken);

        // Protected routes (require JWT)
        router.route("/api/protected/*")
                .handler(JWTAuthHandler.create(jwtAuth));

        router.get("/api/protected/profile").handler(this::getProfile);
        router.put("/api/protected/profile").handler(this::updateProfile);

        // Admin-only routes
        router.get("/api/protected/admin/users")
                .handler(this::requireRole("admin"))
                .handler(this::getAllUsers);

        // Health check
        router.get("/health").handler(ctx ->
            ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject().put("status", "UP").encode()));

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server -> {
                    logger.info("✅ Auth server started on port 8080");
                    logger.info("🔐 JWT Authentication enabled");
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }

    private void initializeSampleUsers() {
        // Create admin user
        User admin = new User("admin", "admin@example.com",
                hashPassword("admin123"), "admin");
        users.put(admin.username, admin);

        // Create regular user
        User user = new User("john", "john@example.com",
                hashPassword("pass123"), "user");
        users.put(user.username, user);

        logger.info("✅ Sample users initialized (admin/admin123, john/pass123)");
    }

    private void register(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        String username = body.getString("username");
        String email = body.getString("email");
        String password = body.getString("password");

        // Validation
        if (username == null || email == null || password == null) {
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Missing required fields")
                            .encode());
            return;
        }

        if (users.containsKey(username)) {
            ctx.response()
                    .setStatusCode(409)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Username already exists")
                            .encode());
            return;
        }

        // Create user
        String hashedPassword = hashPassword(password);
        User newUser = new User(username, email, hashedPassword, "user");
        users.put(username, newUser);

        logger.info("✅ New user registered: {}", username);

        // Generate token
        String token = generateToken(newUser);

        ctx.response()
                .setStatusCode(201)
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("message", "User registered successfully")
                        .put("token", token)
                        .put("user", newUser.toPublicJson())
                        .encode());
    }

    private void login(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();

        String username = body.getString("username");
        String password = body.getString("password");

        if (username == null || password == null) {
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Missing username or password")
                            .encode());
            return;
        }

        User user = users.get(username);

        if (user == null || !verifyPassword(password, user.passwordHash)) {
            logger.warn("❌ Failed login attempt for: {}", username);
            ctx.response()
                    .setStatusCode(401)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Invalid credentials")
                            .encode());
            return;
        }

        logger.info("✅ User logged in: {}", username);

        String token = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("message", "Login successful")
                        .put("token", token)
                        .put("refreshToken", refreshToken)
                        .put("user", user.toPublicJson())
                        .encode());
    }

    private void refreshToken(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String refreshToken = body.getString("refreshToken");

        if (refreshToken == null) {
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "Missing refresh token")
                            .encode());
            return;
        }

        jwtAuth.authenticate(new JsonObject().put("token", refreshToken))
                .onSuccess(user -> {
                    String username = user.principal().getString("username");
                    User dbUser = users.get(username);

                    if (dbUser != null) {
                        String newToken = generateToken(dbUser);
                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject()
                                        .put("token", newToken)
                                        .encode());
                    } else {
                        ctx.response()
                                .setStatusCode(401)
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject()
                                        .put("error", "Invalid refresh token")
                                        .encode());
                    }
                })
                .onFailure(err -> {
                    ctx.response()
                            .setStatusCode(401)
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject()
                                    .put("error", "Invalid or expired refresh token")
                                    .encode());
                });
    }

    private void getProfile(RoutingContext ctx) {
        JsonObject principal = ctx.user().principal();
        String username = principal.getString("username");

        User user = users.get(username);

        if (user != null) {
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(user.toPublicJson().encode());
        } else {
            ctx.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", "User not found")
                            .encode());
        }
    }

    private void updateProfile(RoutingContext ctx) {
        JsonObject principal = ctx.user().principal();
        String username = principal.getString("username");
        JsonObject body = ctx.body().asJsonObject();

        User user = users.get(username);

        if (user != null) {
            if (body.containsKey("email")) {
                user.email = body.getString("email");
            }

            logger.info("✅ Profile updated for: {}", username);

            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("message", "Profile updated")
                            .put("user", user.toPublicJson())
                            .encode());
        } else {
            ctx.response()
                    .setStatusCode(404)
                    .end();
        }
    }

    private void getAllUsers(RoutingContext ctx) {
        JsonObject response = new JsonObject();
        users.values().forEach(user ->
            response.put(user.username, user.toPublicJson()));

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(response.encode());
    }

    private io.vertx.ext.web.handler.AuthenticationHandler requireRole(String role) {
        return ctx -> {
            JsonObject principal = ctx.user().principal();
            String userRole = principal.getString("role");

            if (role.equals(userRole)) {
                ctx.next();
            } else {
                ctx.response()
                        .setStatusCode(403)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject()
                                .put("error", "Forbidden: Insufficient permissions")
                                .encode());
            }
        };
    }

    private String generateToken(User user) {
        return jwtAuth.generateToken(
                new JsonObject()
                        .put("username", user.username)
                        .put("email", user.email)
                        .put("role", user.role),
                new JWTOptions()
                        .setExpiresInMinutes(60) // 1 hour
                        .setIssuer("vertx-auth-service"));
    }

    private String generateRefreshToken(User user) {
        return jwtAuth.generateToken(
                new JsonObject()
                        .put("username", user.username)
                        .put("type", "refresh"),
                new JWTOptions()
                        .setExpiresInMinutes(10080)); // 7 days
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }

    static class User {
        String username;
        String email;
        String passwordHash;
        String role;

        User(String username, String email, String passwordHash, String role) {
            this.username = username;
            this.email = email;
            this.passwordHash = passwordHash;
            this.role = role;
        }

        JsonObject toPublicJson() {
            return new JsonObject()
                    .put("username", username)
                    .put("email", email)
                    .put("role", role);
        }
    }
}
