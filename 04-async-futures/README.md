# Module 04: Async Programming and Futures

## Overview
Master asynchronous programming in Vert.x using Futures and Promises. Learn composition, error handling, and coordinating multiple async operations.

## Key Concepts
- **Future & Promise API** - Core async primitives
- **Future Composition** - Chain operations with `compose()`, `map()`, `flatMap()`
- **Multiple Futures** - Coordinate with `all()`, `any()`, `join()`
- **Error Handling** - `recover()`, `otherwise()`, error propagation
- **Timeout Management** - Handle long-running operations

## Running
```bash
mvn clean package && java -jar target/async-futures-fat.jar
# or
docker-compose up --build
```

## Learning Objectives
✅ Understand Future and Promise
✅ Compose async operations
✅ Handle multiple concurrent operations
✅ Implement error recovery
✅ Build complex async workflows
