# Module 25: Testing (Unit and Integration)

## Overview
This module demonstrates comprehensive testing strategies for Vert.x applications including unit tests, integration tests, and end-to-end tests.

## Key Concepts Covered

### 1. **Unit Testing**
- Verticle testing
- Mocking and stubbing
- Async testing
- Test fixtures

### 2. **Integration Testing**
- TestContainers
- Database testing
- Service integration
- API testing

### 3. **Testing Frameworks**
- Vert.x JUnit5
- REST Assured
- Testcontainers
- Mockito

### 4. **Best Practices**
- Test organization
- Test data management
- Async test handling
- Performance testing

## Project Structure
```
25-testing/
├── src/main/java/com/vertx/app/
│   └── MainVerticle.java
├── src/test/java/com/vertx/app/
│   ├── MainVerticleTest.java
│   └── IntegrationTest.java
├── src/main/resources/
│   └── logback.xml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Running Tests

### Unit Tests
```bash
cd 25-testing
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
```

## Test Example

```java
@ExtendWith(VertxExtension.class)
class MainVerticleTest {
    @Test
    void testVerticleDeployment(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {
            testContext.completeNow();
        }));
    }
}
```

## Learning Objectives
- ✅ Unit testing verticles
- ✅ Integration testing
- ✅ Async test handling
- ✅ Test containers
- ✅ Performance testing

## References
- [Vert.x Testing Guide](https://vertx.io/docs/vertx-junit5/java/)
- [TestContainers Documentation](https://www.testcontainers.org/)
- [REST Assured](https://rest-assured.io/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)