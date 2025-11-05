# Module 17: Distributed Tracing

## Overview
This module demonstrates distributed tracing with OpenTelemetry and Jaeger for observability across microservices.

## Key Concepts Covered

### 1. **Tracing Fundamentals**
- Spans and traces
- Context propagation
- Trace sampling
- Span attributes

### 2. **OpenTelemetry Integration**
- Instrumentation
- Exporters
- Collectors
- Jaeger backend

### 3. **Advanced Features**
- Custom spans
- Baggage propagation
- Performance analysis
- Error tracking

### 4. **Best Practices**
- Sampling strategies
- Span naming conventions
- Attribute selection
- Performance impact

## Project Structure
```
17-tracing/
├── src/main/java/com/vertx/tracing/
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
cd 17-tracing
docker-compose up --build

# Access Jaeger UI
# http://localhost:16686
```

## Learning Objectives
- ✅ Distributed tracing concepts
- ✅ OpenTelemetry instrumentation
- ✅ Jaeger deployment and usage
- ✅ Trace analysis
- ✅ Performance monitoring

## References
- [OpenTelemetry Documentation](https://opentelemetry.io/)
- [Jaeger Documentation](https://www.jaegertracing.io/)
- [Vert.x Tracing](https://vertx.io/docs/vertx-opentelemetry/java/)