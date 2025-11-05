# Module 07: Microservices and Circuit Breaker

## Overview
Learn microservices patterns with Vert.x including API Gateway, Circuit Breaker for resilience, and service-to-service communication.

## Key Concepts
- **API Gateway Pattern** - Single entry point for clients
- **Circuit Breaker** - Prevent cascading failures
- **Service Discovery** - Locate services dynamically
- **Resilience Patterns** - Timeouts, retries, fallbacks
- **Service Communication** - HTTP-based inter-service calls

## Architecture
```
Client → API Gateway (8080) → Service A (8081)
                            → Service B (8082)
```

## Circuit Breaker States
- **CLOSED** - Normal operation, requests pass through
- **OPEN** - Too many failures, requests fail fast
- **HALF_OPEN** - Testing if service recovered

## Features Demonstrated
- ✅ API Gateway routing requests
- ✅ Circuit breaker for each service
- ✅ Timeout handling
- ✅ Fallback responses
- ✅ Service aggregation pattern
- ✅ Simulated failures and delays

## Running
```bash
mvn clean package && java -jar target/microservices-fat.jar

# Or with Docker
docker-compose up --build
```

## API Endpoints

### Gateway (Port 8080)
- `GET /api/service-a/:id` - Call Service A
- `GET /api/service-b/:id` - Call Service B
- `GET /api/aggregate/:id` - Aggregate both services
- `GET /health` - Health check with circuit breaker status

### Service A (Port 8081)
- `GET /data/:id` - Get data (20% failure rate)
- `GET /health` - Service health

### Service B (Port 8082)
- `GET /process/:id` - Process data (15% slow responses)
- `GET /health` - Service health

## Testing
```bash
# Test Service A through gateway
curl http://localhost:8080/api/service-a/123

# Test Service B through gateway
curl http://localhost:8080/api/service-b/456

# Test aggregation
curl http://localhost:8080/api/aggregate/789

# Check health and circuit breaker status
curl http://localhost:8080/health

# Trigger circuit breaker (call multiple times rapidly)
for i in {1..10}; do curl http://localhost:8080/api/service-a/test; done
```

## Circuit Breaker Configuration
- **Max Failures**: 3 consecutive failures
- **Timeout**: 2000ms per request
- **Reset Timeout**: 5000ms before trying again

## Learning Objectives
✅ API Gateway pattern
✅ Circuit Breaker for resilience
✅ Timeout and failure handling
✅ Service aggregation
✅ Fallback mechanisms
✅ Microservices communication patterns
