# Module 26: Clustering and High Availability

## Overview
This module demonstrates building clustered Vert.x applications with Hazelcast for distributed data structures and high availability.

## Key Concepts Covered

### 1. **Clustering Fundamentals**
- Cluster formation
- Node discovery
- Distributed event bus
- Cluster-wide state

### 2. **Hazelcast Integration**
- Cluster manager
- Distributed maps
- Distributed locks
- Cluster events

### 3. **Advanced Features**
- Leader election
- Split-brain handling
- Failover mechanisms
- Load balancing

### 4. **Best Practices**
- Cluster configuration
- Monitoring
- Graceful shutdown
- Data consistency

## Project Structure
```
26-clustering/
├── src/main/java/com/vertx/cluster/
│   └── MainVerticle.java
├── src/main/resources/
│   ├── logback.xml
│   └── hazelcast.xml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Running the Application

### Using Docker Compose
```bash
cd 26-clustering
docker-compose up --build

# Scale to multiple instances
docker-compose up --scale app=3
```

## Cluster Configuration

```xml
<hazelcast>
    <network>
        <join>
            <multicast enabled="false"/>
            <tcp-ip enabled="true">
                <member>localhost:5701</member>
                <member>localhost:5702</member>
            </tcp-ip>
        </join>
    </network>
</hazelcast>
```

## Learning Objectives
- ✅ Cluster formation and discovery
- ✅ Distributed data structures
- ✅ Cluster-wide event bus
- ✅ Failover and recovery
- ✅ Monitoring and management

## References
- [Hazelcast Documentation](https://docs.hazelcast.com/)
- [Vert.x Clustering](https://vertx.io/docs/vertx-core/java/#_clustering)
- [Distributed Systems Patterns](https://martinfowler.com/articles/patterns-of-distributed-systems/)