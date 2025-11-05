package com.vertx.graphql;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

/**
 * GraphQL API Module
 * Demonstrates GraphQL schema definition, queries, and mutations
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        // Define GraphQL schema
        String schema = "type Query {\n" +
                "  user(id: ID!): User\n" +
                "  users: [User]\n" +
                "  hello: String\n" +
                "}\n" +
                "\n" +
                "type User {\n" +
                "  id: ID!\n" +
                "  name: String!\n" +
                "  email: String!\n" +
                "  age: Int\n" +
                "}\n" +
                "\n" +
                "type Mutation {\n" +
                "  createUser(name: String!, email: String!, age: Int): User\n" +
                "  updateUser(id: ID!, name: String, email: String, age: Int): User\n" +
                "  deleteUser(id: ID!): Boolean\n" +
                "}\n";

        // Parse schema
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        // Create runtime wiring
        RuntimeWiring runtimeWiring = newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("hello", env -> "Hello from GraphQL!")
                        .dataFetcher("users", env -> getSampleUsers())
                        .dataFetcher("user", env -> {
                            String id = env.getArgument("id");
                            return getSampleUsers().stream()
                                    .filter(u -> u.getString("id").equals(id))
                                    .findFirst()
                                    .orElse(null);
                        }))
                .type("Mutation", builder -> builder
                        .dataFetcher("createUser", env -> {
                            String name = env.getArgument("name");
                            String email = env.getArgument("email");
                            Integer age = env.getArgument("age");
                            return new JsonObject()
                                    .put("id", System.currentTimeMillis())
                                    .put("name", name)
                                    .put("email", email)
                                    .put("age", age);
                        })
                        .dataFetcher("updateUser", env -> {
                            String id = env.getArgument("id");
                            String name = env.getArgument("name");
                            String email = env.getArgument("email");
                            Integer age = env.getArgument("age");
                            return new JsonObject()
                                    .put("id", id)
                                    .put("name", name)
                                    .put("email", email)
                                    .put("age", age);
                        })
                        .dataFetcher("deleteUser", env -> true))
                .build();

        // Build GraphQL schema
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

        // Create GraphQL instance
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        // Create router
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Health check
        router.get("/health").handler(ctx -> {
            ctx.json(new JsonObject().put("status", "UP"));
        });

        // GraphQL endpoint
        router.post("/graphql").handler(GraphQLHandler.create(graphQL));

        // GraphQL playground (optional)
        router.get("/graphql").handler(ctx -> {
            ctx.response()
                    .putHeader("Content-Type", "text/html")
                    .end(getGraphQLPlayground());
        });

        // Start HTTP server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, http -> {
                    if (http.succeeded()) {
                        logger.info("GraphQL server started on port 8080");
                        logger.info("GraphQL Playground: http://localhost:8080/graphql");
                        startPromise.complete();
                    } else {
                        logger.error("HTTP server failed to start", http.cause());
                        startPromise.fail(http.cause());
                    }
                });
    }

    private JsonArray getSampleUsers() {
        return new JsonArray()
                .add(new JsonObject()
                        .put("id", "1")
                        .put("name", "John Doe")
                        .put("email", "john@example.com")
                        .put("age", 30))
                .add(new JsonObject()
                        .put("id", "2")
                        .put("name", "Jane Smith")
                        .put("email", "jane@example.com")
                        .put("age", 28))
                .add(new JsonObject()
                        .put("id", "3")
                        .put("name", "Bob Johnson")
                        .put("email", "bob@example.com")
                        .put("age", 35));
    }

    private String getGraphQLPlayground() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <title>GraphQL Playground</title>\n" +
                "  <style>\n" +
                "    body { margin: 0; padding: 0; font-family: Arial; }\n" +
                "    #playground { width: 100%; height: 100vh; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div id=\"playground\"></div>\n" +
                "  <script src=\"https://cdn.jsdelivr.net/npm/graphql-playground-react/build/static/js/middleware.js\"></script>\n" +
                "</body>\n" +
                "</html>";
    }
}