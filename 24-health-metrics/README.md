# Module 24: Health Checks and Metrics

## Overview
This module demonstrates implementing health checks and metrics collection with Prometheus integration for observability.

## Key Concepts Covered

### 1. **Health Checks**
- Liveness probes
- Readiness probes
- Startup probes
- Custom health indicators

### 2. **Metrics Collection**
- Counter metrics
- Gauge metrics
- Histogram metrics
- Timer metrics

### 3. **Prometheus Integration**
- Metrics exposition
- Scrape configuration
- Alerting rules
- Grafana dashboards

### 4. **Best Practices**
- Metric naming conventions
- Cardinality management
- Performance impact
- Monitoring strategy

## Project Structure
```
24-health-metrics/
├── src/main/java/com/vertx/metrics/
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
cd 24-health-metrics
docker-compose up --build

# Health check
curl http://localhost:8080/health

# Metrics
curl http://localhost:8080/metrics

# Prometheus UI
# http://localhost:9090
```

## Health Check Endpoints

```
GET /health              # Overall health
GET /health/live         # Liveness probe
GET /health/ready        # Readiness probe
```

## Learning Objectives
- ✅ Health check implementation
- ✅ Metrics collection and exposition
- ✅ Prometheus integration
- ✅ Alerting configuration
- ✅ Dashboard creation

## References
- [Kubernetes Health Checks](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Micrometer Metrics](https://micrometer.io/)
- [Vert.x Health Checks](https://vertx.io/docs/vertx-health-check/java/)