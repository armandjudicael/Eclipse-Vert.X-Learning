# Eclipse Vert.x Learning Repository

A comprehensive, hands-on learning repository for mastering **Eclipse Vert.x** - the reactive, polyglot toolkit for building modern applications on the JVM.

## 🎯 About This Repository

This repository provides a structured learning path through Vert.x's core concepts and features, with **tested, production-ready code examples** and **Dockerized environments** for each module.

## 📚 Module Overview

### [Module 01: Vert.x Basics and First Verticle](./01-vertx-basics/)
**Foundation concepts**
- Understanding Verticles (standard vs worker)
- Event loop model and threading
- Asynchronous operations with Promises
- Periodic tasks and timers
- Deployment options

**Key Learning**: Never block the event loop!

---

### [Module 02: Event Bus and Message Passing](./02-event-bus/)
**Inter-verticle communication**
- Publish-Subscribe pattern
- Point-to-point messaging (send)
- Request-Response pattern
- Message types and serialization
- Consumer lifecycle management

**Key Learning**: Event Bus is the nervous system of Vert.x apps

---

### [Module 03: HTTP Server and REST API](./03-http-server/)
**Building web services**
- HTTP server creation and configuration
- Vert.x Web Router
- RESTful API design (CRUD operations)
- Request/Response handling
- Middleware (CORS, body parsing, logging)

**Key Learning**: Reactive, non-blocking HTTP servers

---

### [Module 04: Async Programming and Futures](./04-async-futures/)
**Mastering asynchronous patterns**
- Future and Promise API
- Future composition (`compose`, `map`, `flatMap`)
- Coordinating multiple operations (`all`, `any`, `join`)
- Error handling and recovery
- Real-world async workflows

**Key Learning**: Compose async operations elegantly

---

### [Module 05: Database Integration](./05-database-integration/)
**Reactive database access**
- Vert.x PostgreSQL reactive client
- Connection pooling
- Prepared statements (SQL injection prevention)
- Transactions (ACID operations)
- Batch operations

**Key Learning**: Non-blocking database I/O with connection pools

**Requirements**: PostgreSQL (included in Docker Compose)

---

### [Module 06: WebSockets Real-time Communication](./06-websockets/)
**Bidirectional real-time apps**
- WebSocket server setup
- Connection lifecycle management
- Broadcasting to all clients
- Room-based messaging
- Ping/Pong keep-alive
- HTML/JavaScript client

**Key Learning**: Persistent connections for real-time features

---

### [Module 07: Microservices and Circuit Breaker](./07-microservices/)
**Building resilient systems**
- API Gateway pattern
- Circuit Breaker for fault tolerance
- Service-to-service communication
- Timeout and failure handling
- Fallback mechanisms
- Service aggregation

**Key Learning**: Resilience patterns prevent cascading failures

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** (JDK)
- **Maven 3.6+**
- **Docker & Docker Compose** (optional, for containerized deployment)

### Quick Start

#### Option 1: Run Individual Modules
```bash
# Navigate to any module
cd 01-vertx-basics

# Build
mvn clean package

# Run
java -jar target/vertx-basics-fat.jar
```

#### Option 2: Docker (Recommended)
```bash
# Navigate to any module
cd 01-vertx-basics

# Build and run
docker-compose up --build

# Stop
docker-compose down
```

## 📖 Learning Path

### Beginner Track
1. **Module 01** - Understand Vert.x basics
2. **Module 02** - Learn event-driven communication
3. **Module 03** - Build your first REST API

### Intermediate Track
4. **Module 04** - Master async programming
5. **Module 05** - Integrate databases reactively
6. **Module 06** - Add real-time features

### Advanced Track
7. **Module 07** - Build microservices with resilience

## 🎓 Key Vert.x Concepts

### 1. Verticles
The fundamental building blocks - think of them as lightweight actors.

```java
public class MyVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
        // Initialize your verticle
        startPromise.complete();
    }
}
```

### 2. Event Loop
Vert.x uses a multi-reactor pattern with event loops.

**Golden Rule**: **NEVER BLOCK THE EVENT LOOP**
- Use `executeBlocking()` for blocking operations
- Deploy worker verticles for CPU-intensive tasks

### 3. Async Programming
Everything is non-blocking with Futures and callbacks.

```java
Future<String> future = fetchData()
    .compose(data -> processData(data))
    .map(result -> transformResult(result));
```

### 4. Event Bus
The communication backbone for distributed systems.

```java
// Publish
vertx.eventBus().publish("news", "Breaking news!");

// Request-Reply
vertx.eventBus().request("service", data, reply -> {
    // Handle response
});
```

## 🏗️ System Design Principles

### Reactive Systems
Vert.x applications are:
- **Responsive**: Low latency
- **Resilient**: Fault-tolerant
- **Elastic**: Scalable
- **Message-Driven**: Async communication

### Best Practices

#### ✅ Do's
- Keep event loop handlers short and non-blocking
- Use Futures for async composition
- Leverage connection pooling for databases
- Implement circuit breakers for external services
- Use proper error handling

#### ❌ Don'ts
- Never block the event loop with synchronous I/O
- Avoid Thread.sleep() in standard verticles
- Don't share mutable state between verticles
- Never use ThreadLocal with event loop threads

## 🛠️ Technology Stack

- **Vert.x 4.5.10** - Reactive toolkit
- **Java 17** - Programming language
- **Maven** - Build tool
- **Logback** - Logging
- **PostgreSQL** - Database (Module 05)
- **Docker** - Containerization

## 📊 Module Comparison

| Module | Complexity | Duration | Prerequisites |
|--------|-----------|----------|---------------|
| 01 - Basics | ⭐ | 1-2 hours | Java basics |
| 02 - Event Bus | ⭐⭐ | 2-3 hours | Module 01 |
| 03 - HTTP Server | ⭐⭐ | 2-3 hours | Module 01, REST basics |
| 04 - Async/Futures | ⭐⭐⭐ | 3-4 hours | Modules 01-03 |
| 05 - Database | ⭐⭐⭐ | 3-4 hours | Module 04, SQL basics |
| 06 - WebSockets | ⭐⭐ | 2-3 hours | Module 03 |
| 07 - Microservices | ⭐⭐⭐⭐ | 4-5 hours | All previous |

## 🧪 Testing

Each module includes:
- Comprehensive code examples
- Logging for observability
- Test scripts where applicable
- Docker configurations

## 📖 Additional Resources

### Official Documentation
- [Vert.x Documentation](https://vertx.io/docs/)
- [Vert.x Examples](https://github.com/vert-x3/vertx-examples)

### Books & Guides
- [Vert.x in Action](https://www.manning.com/books/vertx-in-action)
- [Building Reactive Microservices in Java](https://developers.redhat.com/books/building-reactive-microservices-java/)

### Community
- [Vert.x Google Group](https://groups.google.com/forum/#!forum/vertx)
- [Vert.x Discord](https://discord.gg/vertx)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/vert.x)

## 🎯 Real-World Use Cases

Vert.x is perfect for:
- **Microservices** - Lightweight, fast startup
- **API Gateways** - High-performance routing
- **Real-time Applications** - WebSocket support
- **IoT Backends** - Handle thousands of connections
- **Reactive Systems** - Event-driven architecture

## 🤝 Contributing

This is a learning repository. If you find issues or have improvements:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is created for educational purposes.

## 👨‍💻 Author

Created as a comprehensive learning resource for Eclipse Vert.x.

## 🙏 Acknowledgments

- Eclipse Vert.x team for the amazing toolkit
- The reactive programming community
- All contributors to Vert.x documentation

---

## 🚦 Getting Help

- Read the module README files
- Check the code comments
- Review Vert.x official documentation
- Ask questions in the Vert.x community

---

<div align="center">

**Happy Learning! 🚀**

*Master reactive programming with Eclipse Vert.x*

[⭐ Star this repo](https://github.com/armandjudicael/Eclipse-Vert.X-Learning) if you find it helpful!

</div>
