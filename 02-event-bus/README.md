# Module 02: Event Bus and Message Passing

## Overview
This module demonstrates the Vert.x Event Bus - the nervous system of Vert.x applications. Learn how verticles communicate through publish-subscribe and request-response patterns.

## Key Concepts Covered

### 1. **Event Bus Basics**
- Lightweight messaging system
- Decoupled communication between verticles
- Addresses as message destinations
- Multiple messaging patterns

### 2. **Messaging Patterns**

#### a) **Publish-Subscribe**
- One-to-many communication
- All registered handlers receive the message
- Fire-and-forget pattern
- Use case: Broadcasting events, notifications

#### b) **Point-to-Point (Send)**
- One-to-one communication
- Only one handler receives the message (round-robin)
- Use case: Load balancing, task distribution

#### c) **Request-Response**
- Synchronous-style communication
- Sender waits for reply
- Supports timeouts and error handling
- Use case: RPC-style calls, data queries

### 3. **Message Types**
- Primitive types (String, Integer, etc.)
- JSON objects
- Custom objects (with codecs)
- Binary data

### 4. **Consumer Management**
- Registering consumers
- Unregistering consumers
- Consumer completion handlers

## Project Structure
```
02-event-bus/
├── src/main/java/com/vertx/eventbus/
│   ├── MainVerticle.java              # Coordinator and request sender
│   ├── PublisherVerticle.java         # Publishes messages
│   ├── SubscriberVerticle.java        # Subscribes to messages
│   ├── RequestResponseVerticle.java   # Handles request-reply
│   └── User.java                      # Model class
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Code Examples Explained

### PublisherVerticle.java
Demonstrates:
- Publishing notifications (all subscribers receive)
- Publishing JSON objects
- Publishing complex data structures
- Periodic publishing

**Key Code:**
```java
vertx.eventBus().publish("notifications.all", message);
```

### SubscriberVerticle.java
Demonstrates:
- Subscribing to multiple addresses
- Handling different message types
- Consumer registration
- Proper cleanup (unregister consumers)

**Key Code:**
```java
vertx.eventBus().consumer("notifications.all", message -> {
    // Handle message
});
```

### RequestResponseVerticle.java
Demonstrates:
- Request-reply pattern
- Business logic in message handlers
- Replying to messages
- Error handling with `message.fail()`

**Key Code:**
```java
vertx.eventBus().consumer("user.lookup", message -> {
    // Process request
    message.reply(response);
});
```

### MainVerticle.java
Demonstrates:
- Making requests and handling responses
- Point-to-point sending
- Error handling for failed requests
- Orchestrating multiple verticles

**Key Code:**
```java
vertx.eventBus().request("user.lookup", userId, reply -> {
    if (reply.succeeded()) {
        // Handle response
    }
});
```

## Running the Application

### Using Maven
```bash
cd 02-event-bus
mvn clean package
java -jar target/event-bus-fat.jar
```

### Using Docker
```bash
cd 02-event-bus
docker-compose up --build
```

## Expected Output

You should see:
1. **Publish-Subscribe**: Multiple subscribers receiving the same notification
2. **Point-to-Point**: Only one subscriber receiving sent messages
3. **Request-Response**: User lookups, calculations, and validation requests/responses
4. **Periodic Events**: System notifications, user updates, data events

Sample output:
```
📬 Received notification: System notification at 1234567890
👤 Received user update: User{id='user-1234', name='John Doe', email='john@example.com'}
🔍 Requesting user lookup for: user-123
✅ Received user: User{id='user-123', name='John Doe', email='john@example.com'}
🔢 Requesting calculation: {"a":10,"b":25}
✅ Calculation result: 35
📧 Validating email: test@example.com
✅ Email validation result: {"email":"test@example.com","valid":true}
```

## Event Bus Addresses Used

| Address | Type | Purpose |
|---------|------|---------|
| `notifications.all` | Pub/Sub | System notifications |
| `user.updates` | Pub/Sub | User update events |
| `data.events` | Pub/Sub | Data synchronization events |
| `user.lookup` | Request-Reply | User lookup service |
| `math.add` | Request-Reply | Addition calculator |
| `email.validate` | Request-Reply | Email validation |

## Important Concepts

### 1. **Pub/Sub vs Send**
```java
// Publish - ALL consumers receive
eventBus.publish("address", message);

// Send - ONE consumer receives (round-robin)
eventBus.send("address", message);
```

### 2. **Request with Timeout**
```java
DeliveryOptions options = new DeliveryOptions().setSendTimeout(5000);
eventBus.request("address", message, options, reply -> {
    // Handle response or timeout
});
```

### 3. **Headers**
```java
DeliveryOptions options = new DeliveryOptions()
    .addHeader("action", "create")
    .addHeader("user-id", "123");
eventBus.send("address", message, options);
```

## Best Practices

1. **Use Meaningful Addresses**
   - Use dot notation: `user.created`, `order.updated`
   - Be descriptive: `payment.process.request`

2. **Always Unregister Consumers**
   - Clean up in `stop()` method
   - Prevents memory leaks

3. **Handle Failures**
   - Check `reply.succeeded()` for requests
   - Use `message.fail()` for errors in handlers

4. **Choose Right Pattern**
   - Pub/Sub: Events, notifications
   - Send: Task distribution
   - Request-Reply: Queries, RPC calls

5. **Type Safety**
   - Use generic types: `request<JsonObject>(...)`
   - Validate message types in handlers

## Learning Objectives

After completing this module, you should understand:
- ✅ Event Bus architecture and addressing
- ✅ Publish-Subscribe pattern
- ✅ Point-to-point messaging
- ✅ Request-Response pattern
- ✅ Message types and serialization
- ✅ Consumer lifecycle management
- ✅ Error handling in messaging

## Common Patterns

### 1. Orchestration Pattern
```java
// Coordinate multiple async operations
vertx.eventBus().request("service1", data1)
    .compose(result1 -> vertx.eventBus().request("service2", result1.body()))
    .onSuccess(finalResult -> {
        // Process final result
    });
```

### 2. Fan-out Pattern
```java
// Send to multiple services and collect results
List<Future> futures = addresses.stream()
    .map(addr -> vertx.eventBus().request(addr, message))
    .collect(Collectors.toList());

CompositeFuture.all(futures).onSuccess(results -> {
    // All services responded
});
```

## Next Steps
Proceed to [Module 03: HTTP Server and REST API](../03-http-server/) to learn how to build web services with Vert.x.

## References
- [Vert.x Event Bus Documentation](https://vertx.io/docs/vertx-core/java/#event_bus)
- [Event Bus Examples](https://github.com/vert-x3/vertx-examples/tree/master/core-examples)
