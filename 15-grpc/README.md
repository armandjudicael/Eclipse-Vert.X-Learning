# Module 15: gRPC Services

## Overview
This module demonstrates building high-performance RPC services with gRPC and Protocol Buffers, integrated with Eclipse Vert.x for reactive, non-blocking communication.

## Key Concepts Covered

### 1. **Protocol Buffers**
- Message definition
- Service definition
- Code generation
- Serialization

### 2. **gRPC Communication Patterns**
- Unary RPC (request-response)
- Server streaming
- Client streaming
- Bidirectional streaming

### 3. **Advanced Features**
- gRPC interceptors
- Error handling
- Metadata and headers
- Load balancing

### 4. **Best Practices**
- Service design
- Error handling
- Performance optimization
- Security

## Project Structure
```
15-grpc/
├── src/main/java/com/vertx/grpc/
│   └── MainVerticle.java          # gRPC server
├── src/main/proto/
│   └── user.proto                 # Protocol Buffer definitions
├── src/main/resources/
│   └── logback.xml                # Logging configuration
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Code Examples

### MainVerticle.java
Demonstrates:
- gRPC server initialization
- Service registration
- Port configuration

### user.proto
Example Protocol Buffer definition:
```protobuf
syntax = "proto3";

package com.vertx.grpc;

service UserService {
  rpc GetUser (UserId) returns (User);
  rpc ListUsers (Empty) returns (stream User);
  rpc CreateUser (User) returns (User);
}

message User {
  string id = 1;
  string name = 2;
  string email = 3;
  int32 age = 4;
}

message UserId {
  string id = 1;
}

message Empty {}
```

## Running the Application

### Option 1: Using Docker Compose (Recommended)
```bash
cd 15-grpc
docker-compose up --build
```

### Option 2: Using Maven
```bash
mvn clean package
java -jar target/vertx-grpc-fat.jar
```

## gRPC Client Example

```bash
# Using grpcurl
grpcurl -plaintext localhost:50051 list

# Call GetUser
grpcurl -plaintext -d '{"id":"1"}' localhost:50051 com.vertx.grpc.UserService/GetUser
```

## Learning Objectives
After completing this module, you should understand:
- ✅ Protocol Buffer syntax and compilation
- ✅ gRPC service definition
- ✅ Unary and streaming RPC patterns
- ✅ Error handling in gRPC
- ✅ Integration with Vert.x
- ✅ Performance characteristics

## Important Best Practices
1. **Service Design** - Keep services focused and cohesive
2. **Error Handling** - Use appropriate gRPC status codes
3. **Versioning** - Plan for API evolution
4. **Performance** - Use streaming for large data
5. **Security** - Use TLS for production
6. **Monitoring** - Implement health checks

## Advanced Topics

### Server Streaming
```java
@Override
public void listUsers(Empty request, StreamObserver<User> responseObserver) {
    // Stream multiple responses
    for (User user : users) {
        responseObserver.onNext(user);
    }
    responseObserver.onCompleted();
}
```

### Client Streaming
```java
@Override
public StreamObserver<User> createUsers(StreamObserver<Summary> responseObserver) {
    return new StreamObserver<User>() {
        @Override
        public void onNext(User user) {
            // Process incoming user
        }

        @Override
        public void onCompleted() {
            responseObserver.onNext(summary);
            responseObserver.onCompleted();
        }
    };
}
```

### Bidirectional Streaming
```java
@Override
public StreamObserver<User> syncUsers(StreamObserver<User> responseObserver) {
    return new StreamObserver<User>() {
        @Override
        public void onNext(User user) {
            // Process and respond
            responseObserver.onNext(processedUser);
        }
    };
}
```

## Troubleshooting

### Compilation Issues
- Ensure protoc compiler is installed
- Check .proto file syntax
- Verify Maven protobuf plugin configuration

### Connection Issues
- Verify server is running on correct port
- Check firewall settings
- Use grpcurl for debugging

### Performance Issues
- Use streaming for large payloads
- Implement connection pooling
- Monitor resource usage

## Next Steps
Proceed to [Module 16: Service Discovery with Consul](../16-consul/) to learn about service registration and discovery.

## References
- [gRPC Official Documentation](https://grpc.io/)
- [Protocol Buffers Documentation](https://developers.google.com/protocol-buffers)
- [Vert.x gRPC Documentation](https://vertx.io/docs/vertx-grpc/java/)