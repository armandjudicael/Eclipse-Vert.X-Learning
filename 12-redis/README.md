# Module 12: Redis Caching

## Overview
Comprehensive Redis integration demonstrating caching patterns, session storage, distributed locks, rate limiting, pub/sub, and leaderboards.

## Key Concepts

### 1. **Caching Patterns**
- Cache-aside (lazy loading)
- Write-through caching
- Cache invalidation strategies
- TTL (Time To Live)

### 2. **Session Storage**
- Distributed session management
- Automatic expiration
- Fast lookups

### 3. **Distributed Locks**
- Resource locking across instances
- Lock expiration for safety
- Prevent race conditions

### 4. **Rate Limiting**
- Sliding window algorithm
- Per-IP tracking
- Redis sorted sets

### 5. **Pub/Sub**
- Real-time messaging
- Channel subscriptions
- Broadcast notifications

### 6. **Leaderboards**
- Sorted sets (ZSET)
- Score-based ranking
- Top N queries

## Running

### Docker Compose (Recommended)
```bash
cd 12-redis
docker-compose up --build
```

Starts:
- Redis (port 6379)
- Vert.x app (port 8080)

### Maven
```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
cd 12-redis
mvn clean package
java -jar target/redis-caching-fat.jar
```

## API Endpoints

- `GET /health` - Health check
- `GET /api/cache/:key` - Get from cache (cache-aside)
- `PUT /api/cache/:key` - Put to cache (write-through)
- `DELETE /api/cache/:key` - Invalidate cache
- `POST /api/session` - Create session
- `GET /api/session/:id` - Get session
- `GET /api/rate-limit` - Check rate limit
- `POST /api/lock/:resource` - Acquire distributed lock
- `GET /api/leaderboard` - Get top 10
- `POST /api/leaderboard` - Update score
- `POST /api/publish` - Publish message
- `GET /api/stats` - Redis statistics

## Testing Examples

### 1. Cache-Aside Pattern
```bash
# First request (cache miss)
curl http://localhost:8080/api/cache/user:123
# Response header: X-Cache: MISS
# Value fetched from database and cached

# Second request (cache hit)
curl http://localhost:8080/api/cache/user:123
# Response header: X-Cache: HIT
# Value from cache
```

### 2. Write-Through Caching
```bash
curl -X PUT http://localhost:8080/api/cache/product:456 \
  -H "Content-Type: application/json" \
  -d '{
    "value": "Laptop 15 inch",
    "ttl": 120
  }'
# Writes to cache AND database
```

### 3. Cache Invalidation
```bash
curl -X DELETE http://localhost:8080/api/cache/user:123
# Removes from cache
```

### 4. Session Management
```bash
# Create session
curl -X POST http://localhost:8080/api/session \
  -H "Content-Type: application/json" \
  -d '{"userId": "user-789"}'

# Response: {"sessionId":"uuid","expiresIn":3600}

# Get session
curl http://localhost:8080/api/session/uuid
```

### 5. Rate Limiting
```bash
# Call multiple times
for i in {1..12}; do
  curl http://localhost:8080/api/rate-limit
done

# After 10 requests: 429 Too Many Requests
# Headers show: X-RateLimit-Limit, X-RateLimit-Remaining
```

### 6. Distributed Lock
```bash
curl -X POST http://localhost:8080/api/lock/resource-1 \
  -H "Content-Type: application/json"

# Response: {"locked":true,"lockId":"uuid"}

# Try again immediately:
# Response: {"locked":false,"message":"Resource is locked"}
```

### 7. Leaderboard
```bash
# Update scores
curl -X POST http://localhost:8080/api/leaderboard \
  -H "Content-Type: application/json" \
  -d '{"player":"Alice","score":1500}'

curl -X POST http://localhost:8080/api/leaderboard \
  -H "Content-Type: application/json" \
  -d '{"player":"Bob","score":1200}'

# Get leaderboard
curl http://localhost:8080/api/leaderboard
# Returns top 10 players sorted by score
```

### 8. Pub/Sub
```bash
# Publish message
curl -X POST http://localhost:8080/api/publish \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "notifications",
    "message": "System maintenance at 2am"
  }'

# Subscribers will receive the message
```

## Expected Behavior

### Cache-Aside Flow
```
Request → Check Redis → HIT → Return cached data
                      → MISS → Fetch from DB → Cache → Return
```

### Rate Limiting Example
```
Request 1-10: Allowed (200 OK)
Request 11+: Blocked (429 Too Many Requests)
After 60s: Window resets
```

### Distributed Lock
```
Instance A: Acquires lock → Success
Instance B: Tries to acquire → Fails (locked)
After 10s: Lock expires
Instance B: Tries again → Success
```

## Redis Commands Used

| Command | Purpose |
|---------|---------|
| GET/SET | Basic caching |
| SETEX | Set with expiration |
| DEL | Delete key |
| ZADD | Add to sorted set |
| ZREVRANGE | Get top scores |
| PUBLISH/SUBSCRIBE | Messaging |
| ZCARD | Count sorted set |
| EXPIRE | Set TTL |

## Caching Strategies

### Cache-Aside (Lazy Loading)
```java
1. Check cache
2. If miss, fetch from DB
3. Store in cache
4. Return data
```

### Write-Through
```java
1. Write to cache
2. Write to database
3. Return success
```

### Write-Behind (Async)
```java
1. Write to cache
2. Queue database write
3. Return immediately
4. Async write to DB
```

## Production Considerations

✅ **High Availability**
- Redis Sentinel for failover
- Redis Cluster for sharding
- Replication for redundancy

✅ **Performance**
- Connection pooling
- Pipeline commands
- Optimize key design

✅ **Monitoring**
- Memory usage
- Hit rate
- Slow queries
- Eviction policy

✅ **Security**
- Enable AUTH
- Use TLS
- Network isolation

## Learning Objectives

After this module, you understand:
✅ Cache-aside and write-through patterns
✅ Session management with Redis
✅ Distributed locking
✅ Rate limiting algorithms
✅ Pub/Sub messaging
✅ Leaderboards with sorted sets
✅ TTL and expiration
✅ Production best practices

## Common Patterns

### Session Store
```
session:uuid → {userId, data, timestamp}
TTL: 1 hour
```

### Rate Limit
```
rate:ip → Sorted set of timestamps
Window: 60 seconds
Max: 10 requests
```

### Cache Keys
```
user:123 → User data
product:456 → Product data
config:global → Configuration
```

## Troubleshooting

**Issue**: High memory usage
- Check key expiration
- Review eviction policy
- Use memory-efficient data structures

**Issue**: Cache misses
- Increase TTL
- Pre-warm cache
- Check key patterns

**Issue**: Slow responses
- Use pipelining
- Optimize data structures
- Check network latency

## Resources

- [Redis Documentation](https://redis.io/documentation)
- [Vert.x Redis Client](https://vertx.io/docs/vertx-redis-client/java/)
- [Redis Best Practices](https://redis.io/topics/best-practices)
