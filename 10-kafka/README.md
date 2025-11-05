# Module 10: Apache Kafka Integration

## Overview
Complete Kafka integration with Vert.x, demonstrating producer/consumer patterns, consumer groups, partitions, and event-driven architecture.

## Key Concepts

### 1. **Kafka Producer**
- Send messages to topics
- Key-based partitioning
- Acknowledgment strategies
- Retry configuration

### 2. **Kafka Consumer**
- Subscribe to topics
- Consumer groups for load balancing
- Offset management
- Auto-commit vs manual commit

### 3. **Topics**
- `orders` - Order processing
- `notifications` - Email/SMS notifications
- `events` - System events

### 4. **Partitions**
- Parallel processing
- Ordering guarantees within partition
- Key-based routing

### 5. **Consumer Groups**
- Load balancing across instances
- Each message consumed by one instance in group
- Automatic rebalancing

## Architecture

```
HTTP API → Kafka Producer → Topics (orders, notifications, events)
                                 ↓
                            Kafka Consumers → Processing Logic
```

## Running the Module

### Using Docker Compose (Recommended)
```bash
cd 10-kafka
docker-compose up --build
```

This starts:
- Apache Kafka (port 9092)
- Vert.x application (port 8080)

### Using Maven (Requires local Kafka)
```bash
# Start Kafka first
docker run -d --name kafka -p 9092:9092 apache/kafka:latest

# Build and run
cd 10-kafka
mvn clean package
java -jar target/kafka-integration-fat.jar
```

## API Endpoints

- `GET /` - API information
- `GET /health` - Health check
- `POST /api/orders` - Send order to Kafka
- `POST /api/notifications` - Send notification
- `POST /api/events` - Publish event

## Testing Examples

### 1. Send Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "cust-123",
    "product": "Laptop",
    "amount": 1299.99
  }'

# Response
{
  "message": "Order accepted",
  "orderId": "uuid",
  "partition": 0,
  "offset": 123
}
```

### 2. Send Notification
```bash
curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "message": "Your order has shipped!"
  }'
```

### 3. Publish Event
```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "type": "user.login",
    "data": {
      "userId": "user-456",
      "timestamp": 1234567890
    }
  }'
```

### 4. Watch Logs (Consumer Output)
```bash
# In Docker
docker logs -f vertx-kafka-app

# You'll see:
📦 Order received:
   Topic: orders, Partition: 1, Offset: 5
   Key: uuid, Value: {...}
🔄 Processing order: uuid for product: Laptop
✅ Order processed successfully: uuid
📧 Sending notification to cust-123: Your order uuid has been processed
```

## Expected Behavior

### Order Flow
1. Client sends POST to `/api/orders`
2. Producer sends to Kafka `orders` topic
3. Consumer receives and processes order
4. Consumer sends notification to `notifications` topic
5. Notification consumer sends email

### Logs Example
```
✅ Kafka Producer initialized
✅ Orders Consumer initialized (group: orders-consumer-group)
✅ Notifications Consumer initialized (group: notifications-consumer-group)
✅ Order published to Kafka:
   Topic: orders, Partition: 2, Offset: 10
📦 Order received:
   Topic: orders, Partition: 2, Offset: 10
   Key: order-abc, Value: {"orderId":"order-abc",...}
🔄 Processing order: order-abc for product: Laptop
✅ Order processed successfully: order-abc
📧 Notification sent to cust-123
```

## Kafka Configuration

### Producer Config
```java
bootstrap.servers=localhost:9092
key.serializer=StringSerializer
value.serializer=StringSerializer
acks=all                  // Wait for all replicas
retries=3                 // Retry on failure
linger.ms=10             // Batch delay
batch.size=16384         // Batch size
```

### Consumer Config
```java
bootstrap.servers=localhost:9092
key.deserializer=StringDeserializer
value.deserializer=StringDeserializer
group.id=orders-consumer-group
auto.offset.reset=earliest  // Start from beginning
enable.auto.commit=true     // Auto-commit offsets
```

## Consumer Groups

- **orders-consumer-group**: Processes orders
  - Multiple instances share load
  - Each partition assigned to one consumer

- **notifications-consumer-group**: Sends notifications
  - Independent from orders group
  - Can scale separately

## Partition Strategy

Orders are partitioned by `orderId` (key):
- Same orderId always goes to same partition
- Maintains ordering for same customer
- Enables parallel processing across partitions

## Error Handling

### Producer Retries
- Automatic retries (configured: 3)
- Exponential backoff
- Returns error after retries exhausted

### Consumer Error Handling
- Logged but not retried (at-least-once delivery)
- Dead letter queue pattern can be added
- Manual offset management for exactly-once

## Advanced Features

### 1. Offset Management
```java
// Manual commit
consumer.commit()

// Seek to specific offset
consumer.seek(new TopicPartition("orders", 0), 100)
```

### 2. Batch Processing
```java
consumer.batchHandler(records -> {
    records.forEach(record -> process(record));
});
```

### 3. Transaction Support
```java
producer.initTransactions()
producer.beginTransaction()
// ... send messages
producer.commitTransaction()
```

## Monitoring

### Key Metrics
- Messages produced/consumed
- Consumer lag
- Partition distribution
- Error rates

### Check Consumer Group
```bash
docker exec -it vertx-kafka \
  kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group orders-consumer-group
```

## Production Considerations

✅ **High Availability**
- Multiple Kafka brokers
- Replication factor > 1
- Min in-sync replicas

✅ **Performance**
- Tune batch size and linger.ms
- Compression (gzip, snappy, lz4)
- Partitioning strategy

✅ **Reliability**
- Producer: acks=all
- Consumer: Manual offset management
- Idempotent producers

✅ **Monitoring**
- JMX metrics
- Kafka Manager/UI
- Consumer lag monitoring

## Learning Objectives

After this module, you understand:
✅ Kafka producer and consumer APIs
✅ Topics, partitions, and offsets
✅ Consumer groups and load balancing
✅ Message ordering guarantees
✅ At-least-once delivery
✅ Event-driven architecture
✅ Async message processing with Vert.x

## Common Patterns

### Event Sourcing
Store all events in Kafka, rebuild state by replaying

### CQRS
Separate read/write models, Kafka for synchronization

### Change Data Capture (CDC)
Capture database changes to Kafka

### Saga Pattern
Distributed transactions across microservices

## Troubleshooting

**Issue**: Consumer not receiving messages
- Check consumer is subscribed
- Verify topic exists
- Check consumer group offset

**Issue**: Messages out of order
- Check partitioning key
- Single partition for ordering

**Issue**: Slow processing
- Increase partitions
- Scale consumers (add instances)
- Optimize processing logic

## Resources

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Vert.x Kafka Client](https://vertx.io/docs/vertx-kafka-client/java/)
- [Kafka: The Definitive Guide](https://www.confluent.io/resources/kafka-the-definitive-guide/)
