# Module 23: Server-Sent Events (SSE)

## Overview
This module demonstrates implementing Server-Sent Events for real-time, unidirectional communication from server to client.

## Key Concepts Covered

### 1. **SSE Fundamentals**
- Event streaming
- Connection management
- Reconnection handling
- Event types

### 2. **Implementation Patterns**
- Event broadcasting
- Per-client events
- Event filtering
- Backpressure handling

### 3. **Advanced Features**
- Event IDs and retry
- Custom event types
- Compression
- Load balancing

### 4. **Best Practices**
- Connection pooling
- Resource cleanup
- Error handling
- Monitoring

## Project Structure
```
23-sse/
├── src/main/java/com/vertx/sse/
│   └── MainVerticle.java
├── src/main/resources/
│   ├── logback.xml
│   └── webroot/
│       └── index.html
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Running the Application

### Using Docker Compose
```bash
cd 23-sse
docker-compose up --build

# Open browser
# http://localhost:8080
```

## SSE Client Example

```javascript
const eventSource = new EventSource('/api/events');

eventSource.addEventListener('message', (event) => {
    console.log('Received:', event.data);
});

eventSource.addEventListener('error', () => {
    console.error('Connection error');
});
```

## Learning Objectives
- ✅ SSE protocol and standards
- ✅ Connection lifecycle management
- ✅ Event broadcasting patterns
- ✅ Client-side handling
- ✅ Performance optimization

## References
- [Server-Sent Events Specification](https://html.spec.whatwg.org/multipage/server-sent-events.html)
- [MDN SSE Documentation](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [Vert.x SSE Support](https://vertx.io/docs/vertx-web/java/#_server_sent_events)