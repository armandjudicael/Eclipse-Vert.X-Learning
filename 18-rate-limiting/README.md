# Module 18: API Rate Limiting

## Overview
This module demonstrates implementing rate limiting strategies for APIs using token bucket and sliding window algorithms.

## Key Concepts Covered

### 1. **Rate Limiting Algorithms**
- Token bucket algorithm
- Sliding window
- Leaky bucket
- Fixed window

### 2. **Implementation Strategies**
- In-memory rate limiting
- Redis-based rate limiting
- Per-user limits
- Per-IP limits

### 3. **Advanced Features**
- Distributed rate limiting
- Custom rate limit policies
- Rate limit headers
- Quota management

### 4. **Best Practices**
- Graceful degradation
- Clear error messages
- Monitoring and alerting
- Configuration management

## Project Structure
```
18-rate-limiting/
├── src/main/java/com/vertx/ratelimit/
│   └── MainVerticle.java
├── src/main/resources/
│   └── logback.xml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Running the Application

### Using Docker Compose
```bash
cd 18-rate-limiting
docker-compose up --build

# Test rate limiting
curl -X GET http://localhost:8080/api/data
```

## Learning Objectives
- ✅ Rate limiting algorithms
- ✅ Implementation patterns
- ✅ Distributed rate limiting
- ✅ Monitoring and metrics
- ✅ User experience optimization

## References
- [Rate Limiting Patterns](https://en.wikipedia.org/wiki/Rate_limiting)
- [Token Bucket Algorithm](https://en.wikipedia.org/wiki/Token_bucket)
- [Redis Rate Limiting](https://redis.io/commands/incr)