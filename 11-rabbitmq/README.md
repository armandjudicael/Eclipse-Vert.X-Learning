# Module 11: RabbitMQ Integration

## Overview
Complete RabbitMQ integration demonstrating exchanges, queues, routing patterns, priority queues, and message acknowledgments.

## Key Concepts

### 1. **Exchange Types**
- **Direct**: Route by exact routing key match
- **Topic**: Route by pattern matching (wildcards)
- **Fanout**: Broadcast to all bound queues
- **Headers**: Route by message headers

### 2. **Queues**
- Durable queues (survive broker restart)
- Priority queues (0-10 priority levels)
- Message acknowledgments (manual/auto)
- Queue binding to exchanges

### 3. **Routing Keys**
- Direct: Exact match (`order`, `payment`)
- Topic: Pattern matching (`notify.email.*`, `notify.sms.*`)
- Fanout: No routing key needed

## Architecture

```
HTTP API → RabbitMQ Client → Exchanges → Queues → Consumers
                               ├─ Direct Exchange → orders
                               ├─ Topic Exchange  → emails, sms
                               ├─ Fanout Exchange → logs
                               └─ Priority Queue  → tasks
```

## Running the Module

### Using Docker Compose (Recommended)
```bash
cd 11-rabbitmq
docker-compose up --build
```

Starts:
- RabbitMQ (ports 5672, 15672)
- Vert.x app (port 8080)

**RabbitMQ Management UI**: http://localhost:15672
- Username: `guest`
- Password: `guest`

### Using Maven
```bash
# Start RabbitMQ first
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 \
  rabbitmq:3-management-alpine

cd 11-rabbitmq
mvn clean package
java -jar target/rabbitmq-integration-fat.jar
```

## API Endpoints

- `GET /` - API information
- `GET /health` - Health check
- `POST /api/orders` - Send order (direct exchange)
- `POST /api/notifications/email` - Send email (topic exchange)
- `POST /api/notifications/sms` - Send SMS (topic exchange)
- `POST /api/broadcast` - Broadcast message (fanout)
- `POST /api/tasks/priority` - Priority task

## Testing Examples

### 1. Send Order (Direct Exchange)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "product": "Laptop",
    "quantity": 2,
    "customer": "John Doe"
  }'

# Logs show:
# ✅ Order published to RabbitMQ
# 📦 Order received: {...}
# 🔄 Processing order uuid
# ✅ Order processed: uuid
```

### 2. Send Email (Topic Exchange)
```bash
curl -X POST http://localhost:8080/api/notifications/email \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "subject": "Order Confirmation",
    "message": "Your order has been confirmed",
    "priority": "urgent"
  }'

# Routes to: notify.email.urgent
# Matches binding: notify.email.*
```

### 3. Send SMS (Topic Exchange)
```bash
curl -X POST http://localhost:8080/api/notifications/sms \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+1234567890",
    "message": "Your order shipped!",
    "priority": "normal"
  }'

# Routes to: notify.sms.normal
```

### 4. Broadcast (Fanout Exchange)
```bash
curl -X POST http://localhost:8080/api/broadcast \
  -H "Content-Type: application/json" \
  -d '{
    "message": "System maintenance in 1 hour"
  }'

# Broadcasts to ALL queues bound to fanout exchange
```

### 5. Priority Task
```bash
# High priority task (processed first)
curl -X POST http://localhost:8080/api/tasks/priority \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Critical bug fix",
    "priority": 10
  }'

# Low priority task
curl -X POST http://localhost:8080/api/tasks/priority \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Code cleanup",
    "priority": 1
  }'

# Tasks processed in priority order (10 before 1)
```

## Exchange & Queue Setup

### Exchanges
| Name | Type | Purpose |
|------|------|---------|
| `direct_exchange` | direct | Order routing |
| `topic_exchange` | topic | Notification routing |
| `fanout_exchange` | fanout | Broadcast logs |

### Queues
| Name | Bound To | Routing Key |
|------|----------|-------------|
| `orders` | direct_exchange | `order` |
| `emails` | topic_exchange | `notify.email.*` |
| `sms` | topic_exchange | `notify.sms.*` |
| `logs` | fanout_exchange | (none) |
| `priority_tasks` | (direct) | (none) |

## Expected Behavior

### Topic Exchange Example
```
Message: notify.email.urgent
Matches: notify.email.*
         notify.#
Queues:  emails queue receives it
```

### Fanout Example
```
Message: (any)
Broadcast to: logs queue (and any other bound queues)
```

### Priority Example
```
Queue: [Task(p=10), Task(p=5), Task(p=1)]
Processed: p=10 first, then p=5, then p=1
```

## Message Acknowledgment

Manual acknowledgment ensures messages aren't lost:
```java
consumer.handler(message -> {
    try {
        processMessage(message);
        // Acknowledge success
        client.basicAck(message.envelope().getDeliveryTag(), false);
    } catch (Exception e) {
        // Reject and requeue
        client.basicNack(message.envelope().getDeliveryTag(), false, true);
    }
});
```

## Advanced Features

### 1. Dead Letter Exchange (DLX)
```java
Map<String, Object> args = new HashMap<>();
args.put("x-dead-letter-exchange", "dlx_exchange");
args.put("x-message-ttl", 60000); // 60 seconds
client.queueDeclare("my_queue", true, false, false, args);
```

### 2. Message TTL
```java
AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
    .expiration("60000") // 60 seconds
    .build();
```

### 3. Consumer Prefetch
```java
client.basicQos(10); // Limit unacknowledged messages
```

### 4. Publisher Confirms
```java
client.confirmSelect();
client.basicPublish(...);
client.waitForConfirms();
```

## Monitoring

### RabbitMQ Management UI
- Queues and message rates
- Exchange bindings
- Consumer details
- Message stats

### Key Metrics
- Messages published/consumed
- Queue depth
- Consumer count
- Acknowledgment rates

## Production Considerations

✅ **High Availability**
- Cluster multiple nodes
- Queue mirroring
- Disk persistence

✅ **Performance**
- Publisher confirms
- Consumer prefetch
- Connection pooling

✅ **Reliability**
- Manual acknowledgments
- Dead letter exchanges
- Message persistence

✅ **Security**
- TLS/SSL encryption
- User permissions
- Virtual hosts

## Common Patterns

### Work Queue Pattern
Multiple workers process tasks in parallel
```
Producer → Queue → [Worker1, Worker2, Worker3]
```

### Pub/Sub Pattern
Broadcast to multiple consumers
```
Publisher → Fanout Exchange → [Queue1, Queue2, Queue3]
```

### RPC Pattern
Request-reply with correlation ID and reply-to
```
Client → Request Queue → Server → Reply Queue → Client
```

### Routing Pattern
Route by criteria using topic exchange
```
Publisher → Topic Exchange → [Email Queue, SMS Queue, Push Queue]
```

## Learning Objectives

After this module, you understand:
✅ RabbitMQ exchange types
✅ Queue declaration and binding
✅ Routing keys and patterns
✅ Message acknowledgments
✅ Priority queues
✅ Publisher/consumer patterns
✅ Error handling and reliability

## Troubleshooting

**Issue**: Consumer not receiving messages
- Check queue binding
- Verify routing key matches
- Check RabbitMQ UI for messages

**Issue**: Messages lost
- Use manual acknowledgments
- Enable publisher confirms
- Make queues durable

**Issue**: Slow processing
- Increase prefetch count
- Add more consumers
- Use priority queues

## Resources

- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Vert.x RabbitMQ Client](https://vertx.io/docs/vertx-rabbitmq-client/java/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
