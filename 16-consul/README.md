# Module 16: Service Discovery with Consul

## Overview
This module demonstrates service registration, discovery, and health checking using HashiCorp Consul with Eclipse Vert.x.

## Key Concepts Covered

### 1. **Service Registration**
- Registering services with Consul
- Health checks
- Service metadata
- Deregistration

### 2. **Service Discovery**
- Discovering available services
- Load balancing
- Failover handling
- DNS interface

### 3. **Advanced Features**
- Key-Value store
- Distributed configuration
- Watches and notifications
- Cluster management

### 4. **Best Practices**
- Health check strategies
- Service naming conventions
- Graceful shutdown
- Monitoring

## Project Structure
```
16-consul/
├── src/main/java/com/vertx/consul/
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
cd 16-consul
docker-compose up --build

# Access Consul UI
# http://localhost:8500
```

## Learning Objectives
- ✅ Service registration with Consul
- ✅ Service discovery patterns
- ✅ Health checking
- ✅ Configuration management
- ✅ Distributed systems patterns

## References
- [Consul Documentation](https://www.consul.io/docs)
- [Vert.x Service Discovery](https://vertx.io/docs/vertx-service-discovery/java/)