# Module 13: MongoDB Integration

## Overview
This module demonstrates reactive MongoDB operations with Eclipse Vert.x, including CRUD operations, aggregations, and best practices for NoSQL database integration.

## Key Concepts Covered

### 1. **MongoDB Reactive Client**
- Non-blocking MongoDB operations
- Connection pooling
- Automatic connection management

### 2. **CRUD Operations**
- Create (insertOne, insertMany)
- Read (find, findOne)
- Update (updateCollection, replaceOne)
- Delete (removeOne, removeMany)

### 3. **Advanced Features**
- Aggregation pipeline
- Indexing strategies
- Change streams
- GridFS for file storage
- Transactions

### 4. **Best Practices**
- Connection pooling
- Error handling
- Query optimization
- Data validation

## Project Structure
```
13-mongodb/
├── src/main/java/com/vertx/mongodb/
│   └── MainVerticle.java          # MongoDB operations
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
- MongoDB client initialization
- CRUD operations via REST API
- Error handling
- Connection management

## Running the Application

### Option 1: Using Docker Compose (Recommended)
```bash
cd 13-mongodb
docker-compose up --build

# In another terminal, test the API
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'
```

### Option 2: Using Maven
```bash
# Start MongoDB separately
docker run -d --name mongodb -p 27017:27017 mongo:7-alpine

# Build and run
mvn clean package
java -jar target/vertx-mongodb-fat.jar
```

## API Endpoints

### Create User
```bash
POST /api/users
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "age": 30
}
```

### Get All Users
```bash
GET /api/users
```

### Get User by ID
```bash
GET /api/users/{id}
```

### Update User
```bash
PUT /api/users/{id}
Content-Type: application/json

{
  "name": "Jane Doe",
  "age": 31
}
```

### Delete User
```bash
DELETE /api/users/{id}
```

### Get User Count
```bash
GET /api/users/stats/count
```

## Learning Objectives
After completing this module, you should understand:
- ✅ How to connect to MongoDB reactively
- ✅ CRUD operations with Vert.x MongoDB client
- ✅ Error handling and connection management
- ✅ Query optimization and indexing
- ✅ Aggregation pipelines
- ✅ Transaction handling

## Important Best Practices
1. **Always close connections** - Use `stop()` method
2. **Use connection pooling** - Improves performance
3. **Validate input** - Prevent injection attacks
4. **Handle errors gracefully** - Provide meaningful responses
5. **Use indexes** - Optimize query performance
6. **Batch operations** - For bulk inserts/updates

## Advanced Topics

### Aggregation Pipeline
```java
JsonArray pipeline = new JsonArray()
    .add(new JsonObject().put("$match", new JsonObject().put("age", new JsonObject().put("$gte", 18))))
    .add(new JsonObject().put("$group", new JsonObject()
        .put("_id", "$city")
        .put("count", new JsonObject().put("$sum", 1))));

mongoClient.aggregate("users", pipeline, res -> {
    // Handle results
});
```

### Change Streams
```java
mongoClient.watch("users", res -> {
    if (res.succeeded()) {
        // Handle document changes in real-time
    }
});
```

### GridFS for File Storage
```java
mongoClient.gridFSBucket("fs", bucket -> {
    // Upload/download files
});
```

## Troubleshooting

### Connection Issues
- Ensure MongoDB is running: `docker ps | grep mongodb`
- Check connection string in MainVerticle
- Verify network connectivity

### Query Errors
- Check JSON format of queries
- Verify collection names
- Review MongoDB query syntax

### Performance Issues
- Add indexes to frequently queried fields
- Use projection to limit returned fields
- Batch operations when possible

## Next Steps
Proceed to [Module 14: GraphQL API](../14-graphql/) to learn about modern API design patterns.

## References
- [Vert.x MongoDB Client Documentation](https://vertx.io/docs/vertx-mongo-client/java/)
- [MongoDB Documentation](https://www.mongodb.com/docs/)
- [MongoDB Query Language](https://www.mongodb.com/docs/manual/reference/operator/query/)