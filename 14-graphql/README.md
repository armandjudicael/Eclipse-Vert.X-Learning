# Module 14: GraphQL API

## Overview
This module demonstrates building a modern GraphQL API with Eclipse Vert.x, including schema definition, queries, mutations, and best practices for API design.

## Key Concepts Covered

### 1. **GraphQL Schema**
- Type definitions
- Queries and mutations
- Input types and arguments
- Scalar types and custom types

### 2. **Resolvers and Data Fetchers**
- Query resolvers
- Mutation resolvers
- Field resolvers
- Error handling

### 3. **Advanced Features**
- Subscriptions for real-time updates
- DataLoader for N+1 problem
- Query complexity analysis
- Caching strategies

### 4. **Best Practices**
- Schema design patterns
- Error handling
- Performance optimization
- Security considerations

## Project Structure
```
14-graphql/
├── src/main/java/com/vertx/graphql/
│   └── MainVerticle.java          # GraphQL server
├── src/main/resources/
│   └── logback.xml                # Logging configuration
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Code Examples

### MainVerticle.java
Demonstrates:
- GraphQL schema definition
- Query resolvers
- Mutation resolvers
- GraphQL playground integration

## Running the Application

### Option 1: Using Docker Compose (Recommended)
```bash
cd 14-graphql
docker-compose up --build

# Access GraphQL Playground
# Open browser: http://localhost:8080/graphql
```

### Option 2: Using Maven
```bash
mvn clean package
java -jar target/vertx-graphql-fat.jar

# Access: http://localhost:8080/graphql
```

## GraphQL Queries

### Query All Users
```graphql
query {
  users {
    id
    name
    email
    age
  }
}
```

### Query Single User
```graphql
query {
  user(id: "1") {
    id
    name
    email
    age
  }
}
```

### Hello Query
```graphql
query {
  hello
}
```

### Create User Mutation
```graphql
mutation {
  createUser(name: "Alice", email: "alice@example.com", age: 25) {
    id
    name
    email
    age
  }
}
```

### Update User Mutation
```graphql
mutation {
  updateUser(id: "1", name: "John Updated", age: 31) {
    id
    name
    email
    age
  }
}
```

### Delete User Mutation
```graphql
mutation {
  deleteUser(id: "1")
}
```

## Learning Objectives
After completing this module, you should understand:
- ✅ GraphQL schema definition and types
- ✅ Query and mutation resolvers
- ✅ Data fetchers and field resolvers
- ✅ Error handling in GraphQL
- ✅ Query optimization techniques
- ✅ Integration with Vert.x

## Important Best Practices
1. **Schema Design** - Keep schemas simple and well-organized
2. **Error Handling** - Provide meaningful error messages
3. **Performance** - Use DataLoader to prevent N+1 queries
4. **Security** - Validate and sanitize inputs
5. **Caching** - Implement appropriate caching strategies
6. **Documentation** - Use descriptions in schema

## Advanced Topics

### Subscriptions (Real-time Updates)
```graphql
subscription {
  userCreated {
    id
    name
    email
  }
}
```

### DataLoader for N+1 Prevention
```java
DataLoader<String, User> userLoader = new DataLoader<>(ids -> {
    // Batch load users
    return Future.succeededFuture(loadUsersByIds(ids));
});
```

### Query Complexity Analysis
```java
QueryComplexityAnalyzer analyzer = new QueryComplexityAnalyzer();
// Prevent expensive queries
```

## Troubleshooting

### Schema Parsing Errors
- Check GraphQL schema syntax
- Verify type definitions
- Review resolver implementations

### Query Execution Errors
- Check resolver logic
- Verify data availability
- Review error messages

### Performance Issues
- Use DataLoader for batch operations
- Implement query complexity limits
- Add appropriate indexes

## Next Steps
Proceed to [Module 15: gRPC Services](../15-grpc/) to learn about high-performance RPC.

## References
- [GraphQL Official Documentation](https://graphql.org/)
- [GraphQL Java Documentation](https://www.graphql-java.com/)
- [Vert.x GraphQL Documentation](https://vertx.io/docs/vertx-web-graphql/java/)