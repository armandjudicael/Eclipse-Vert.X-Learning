# Module 01: Vert.x Basics and First Verticle

## Overview
This module introduces the fundamental concepts of Eclipse Vert.x, focusing on verticles, the event loop model, and asynchronous programming basics.

## Key Concepts Covered

### 1. **Verticles**
- Basic unit of deployment in Vert.x
- Lightweight, actor-like components
- Two types: Standard (event loop) and Worker verticles

### 2. **Event Loop**
- Non-blocking I/O model
- Each verticle runs on an event loop thread
- Never block the event loop!

### 3. **Asynchronous Operations**
- Timers and periodic tasks
- Promises and futures
- Execute blocking code safely

### 4. **Threading Model**
- Event loop threads for non-blocking operations
- Worker threads for blocking operations
- Context propagation

## Project Structure
```
01-vertx-basics/
├── src/main/java/com/vertx/learning/
│   ├── MainVerticle.java      # Standard event loop verticle
│   ├── WorkerVerticle.java    # Worker verticle for blocking ops
│   └── Application.java       # Entry point
├── src/main/resources/
│   └── logback.xml            # Logging configuration
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Code Examples

### MainVerticle.java
Demonstrates:
- Verticle lifecycle (start/stop)
- Timers and periodic tasks
- Execute blocking operations
- Context data storage

### WorkerVerticle.java
Demonstrates:
- Worker verticle deployment
- Blocking operations without blocking event loop
- CPU-intensive tasks

### Application.java
Demonstrates:
- Creating Vert.x instance
- Deploying verticles
- Deployment options
- Multiple instances

## Running the Application

### Option 1: Using Maven
```bash
# Build the project
mvn clean package

# Run the application
java -jar target/vertx-basics-fat.jar
```

### Option 2: Using Docker
```bash
# Build and run with Docker Compose
docker-compose up --build

# Stop the application
docker-compose down
```

### Option 3: Direct Maven execution
```bash
mvn clean compile exec:java -Dexec.mainClass=com.vertx.learning.Application
```

## Expected Output
When you run the application, you should see:
1. MainVerticle deployment with deployment ID
2. Timer execution after 1 second
3. Periodic task every 3 seconds
4. Worker verticle heavy computation
5. Blocking operation completion

## Learning Objectives
After completing this module, you should understand:
- ✅ What verticles are and how to create them
- ✅ The difference between event loop and worker verticles
- ✅ How to deploy verticles programmatically
- ✅ How to use timers and periodic tasks
- ✅ How to execute blocking code safely
- ✅ The Vert.x threading model

## Important Best Practices
1. **Never block the event loop** - Use worker verticles or `executeBlocking()`
2. **Complete promises** - Always call `complete()` or `fail()` on promises
3. **Clean up resources** - Cancel timers in the `stop()` method
4. **Use proper logging** - Helps debug asynchronous code

## Next Steps
Proceed to [Module 02: Event Bus and Message Passing](../02-event-bus/) to learn about inter-verticle communication.

## References
- [Vert.x Core Documentation](https://vertx.io/docs/vertx-core/java/)
- [Vert.x Best Practices](https://vertx.io/docs/guide-for-java-devs/)
