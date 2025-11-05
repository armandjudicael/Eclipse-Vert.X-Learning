# Module 28: Advanced Software Testing

## Overview
This module provides a comprehensive guide to advanced software testing methodologies including TDD (Test-Driven Development), DDD (Domain-Driven Design), BDD (Behavior-Driven Development), and other testing strategies for building robust, maintainable applications with Eclipse Vert.x.

## Key Concepts Covered

### 1. **Test-Driven Development (TDD)**
- Red-Green-Refactor cycle
- Writing tests before implementation
- Unit test best practices
- Test organization and structure

### 2. **Domain-Driven Design (DDD)**
- Domain models and entities
- Value objects
- Aggregates and repositories
- Ubiquitous language
- Domain validation

### 3. **Behavior-Driven Development (BDD)**
- Gherkin syntax
- Cucumber integration
- Scenario-based testing
- Acceptance criteria

### 4. **Testing Strategies**
- Unit testing
- Integration testing
- API testing
- Architecture testing
- Mutation testing
- Code coverage analysis

### 5. **Testing Frameworks & Tools**
- JUnit 5 with parameterized tests
- Mockito for mocking
- AssertJ for fluent assertions
- REST Assured for API testing
- TestContainers for integration tests
- Cucumber for BDD
- ArchUnit for architecture testing
- PIT for mutation testing
- JaCoCo for code coverage

## Project Structure
```
28-advanced-testing/
├── src/main/java/com/vertx/testing/
│   ├── MainVerticle.java          # HTTP API server
│   ├── User.java                  # Domain model (DDD)
│   └── UserService.java           # Business logic
├── src/test/java/com/vertx/testing/
│   ├── UserTest.java              # Unit tests (TDD)
│   ├── UserServiceTest.java       # Service tests
│   └── MainVerticleTest.java      # Integration tests (API)
├── src/main/resources/
│   └── logback.xml                # Logging configuration
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Running the Application

### Option 1: Using Docker Compose (Recommended)
```bash
cd 28-advanced-testing
docker-compose up --build

# Access the API
curl http://localhost:8080/health
```

### Option 2: Using Maven
```bash
mvn clean package
java -jar target/vertx-advanced-testing-fat.jar
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=UserTest
```

### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
# Report generated in: target/site/jacoco/index.html
```

### Run Mutation Tests
```bash
mvn org.pitest:pitest-maven:mutationCoverage
# Report generated in: target/pit-reports/
```

## API Endpoints

### Health Check
```bash
GET /health
```

### Create User
```bash
POST /api/users
Content-Type: application/json

{
  "id": "1",
  "name": "John Doe",
  "email": "john@example.com",
  "age": 30
}
```

### Get User by ID
```bash
GET /api/users/{id}
```

### Get All Users
```bash
GET /api/users
```

### Update User
```bash
PUT /api/users/{id}
Content-Type: application/json

{
  "name": "John Updated",
  "age": 31
}
```

### Delete User
```bash
DELETE /api/users/{id}
```

## Testing Methodologies Demonstrated

### 1. Test-Driven Development (TDD)

#### Red-Green-Refactor Cycle
```
1. RED: Write failing test
2. GREEN: Write minimal code to pass test
3. REFACTOR: Improve code while keeping tests passing
```

#### Example: UserTest.java
```java
@Test
@DisplayName("Should create valid user with all required fields")
void testCreateValidUser() {
    // Arrange & Act
    User user = new User("1", "John Doe", "john@example.com", 30);

    // Assert
    assertThat(user)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", "1")
        .hasFieldOrPropertyWithValue("name", "John Doe");
}
```

### 2. Domain-Driven Design (DDD)

#### Domain Model with Validation
```java
public class User {
    private final String id;
    private final String name;
    private final String email;
    private final Integer age;

    public User(String id, String name, String email, Integer age) {
        // Validation logic
        validateUser();
    }

    private void validateUser() {
        // Business rules enforcement
    }
}
```

#### Key DDD Principles
- **Entities**: Objects with identity (User)
- **Value Objects**: Immutable objects (Email, Age)
- **Aggregates**: Clusters of entities (User aggregate)
- **Repositories**: Collection-like interface (UserService)
- **Domain Events**: Significant occurrences
- **Ubiquitous Language**: Shared terminology

### 3. Behavior-Driven Development (BDD)

#### Gherkin Syntax Example
```gherkin
Feature: User Management
  Scenario: Create a new user
    Given I have a user service
    When I create a user with valid data
    Then the user should be created successfully
```

### 4. Unit Testing Best Practices

#### Parameterized Tests
```java
@ParameterizedTest
@CsvSource({
    "john@example.com, true",
    "invalid.email, false"
})
void testEmailValidation(String email, boolean shouldBeValid) {
    // Test implementation
}
```

#### Nested Test Classes
```java
@Nested
@DisplayName("Create User Tests")
class CreateUserTests {
    // Related tests grouped together
}
```

#### Fluent Assertions
```java
assertThat(user)
    .isNotNull()
    .hasFieldOrPropertyWithValue("name", "John")
    .extracting(User::getAge)
    .isEqualTo(30);
```

### 5. Integration Testing

#### API Testing with REST Assured
```java
given()
    .contentType("application/json")
    .body(user.toString())
    .when()
    .post("http://localhost:8080/api/users")
    .then()
    .statusCode(201)
    .body("id", equalTo("1"));
```

### 6. Mocking with Mockito

#### Mock Example
```java
@Mock
private UserRepository userRepository;

@Test
void testServiceWithMock() {
    when(userRepository.findById("1"))
        .thenReturn(Optional.of(user));
    
    // Test implementation
}
```

### 7. Code Coverage Analysis

#### JaCoCo Configuration
```bash
# Generate coverage report
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

#### Coverage Metrics
- Line coverage
- Branch coverage
- Method coverage
- Class coverage

### 8. Mutation Testing

#### PIT Configuration
```bash
# Run mutation tests
mvn org.pitest:pitest-maven:mutationCoverage

# View mutation report
open target/pit-reports/index.html
```

#### Mutation Testing Benefits
- Validates test quality
- Identifies weak tests
- Ensures meaningful assertions

## Learning Objectives

After completing this module, you should understand:
- ✅ Test-Driven Development (TDD) principles and practices
- ✅ Domain-Driven Design (DDD) concepts and implementation
- ✅ Behavior-Driven Development (BDD) with Cucumber
- ✅ Unit testing best practices and patterns
- ✅ Integration testing strategies
- ✅ API testing with REST Assured
- ✅ Mocking and stubbing with Mockito
- ✅ Fluent assertions with AssertJ
- ✅ Code coverage analysis with JaCoCo
- ✅ Mutation testing with PIT
- ✅ Architecture testing with ArchUnit
- ✅ Test organization and structure

## Important Best Practices

### 1. **Test Organization**
- One test class per production class
- Use nested classes for related tests
- Clear, descriptive test names
- Arrange-Act-Assert pattern

### 2. **Test Coverage**
- Aim for >80% code coverage
- Focus on critical paths
- Test edge cases and error conditions
- Avoid testing trivial code

### 3. **Test Maintenance**
- Keep tests simple and focused
- Avoid test interdependencies
- Use setup/teardown methods
- Refactor tests like production code

### 4. **Mocking Strategy**
- Mock external dependencies
- Don't mock the system under test
- Use real objects when possible
- Verify behavior, not implementation

### 5. **Assertion Quality**
- Use specific assertions
- Provide meaningful error messages
- Test one thing per test
- Use fluent assertions for readability

### 6. **Performance**
- Keep unit tests fast
- Use in-memory databases for tests
- Parallelize test execution
- Profile slow tests

## Advanced Topics

### Architecture Testing with ArchUnit
```java
@ArchTest
static final ArchRule services_should_be_in_service_package =
    classes()
        .that().haveSimpleNameEndingWith("Service")
        .should().resideInAPackage("..service..");
```

### Parameterized Tests
```java
@ParameterizedTest
@MethodSource("provideUsers")
void testWithMultipleUsers(User user) {
    // Test implementation
}

static Stream<User> provideUsers() {
    return Stream.of(
        new User("1", "John", "john@example.com", 30),
        new User("2", "Jane", "jane@example.com", 25)
    );
}
```

### Custom Assertions
```java
public class UserAssertions extends AbstractAssert<UserAssertions, User> {
    public UserAssertions hasValidEmail() {
        // Custom assertion logic
        return this;
    }
}
```

## Troubleshooting

### Tests Not Running
- Check test class naming (ends with Test)
- Verify test methods are public
- Ensure @Test annotation is present
- Check for compilation errors

### Mocking Issues
- Verify mock setup before test
- Check mock verification syntax
- Ensure mocked methods are called
- Review mock configuration

### Coverage Issues
- Run with coverage plugin
- Check excluded packages
- Review coverage reports
- Identify untested code paths

### Performance Issues
- Profile slow tests
- Use parallel test execution
- Optimize test setup/teardown
- Consider test categorization

## Next Steps

1. **Implement TDD**: Write tests before code
2. **Apply DDD**: Model your domain properly
3. **Use BDD**: Describe behavior in Gherkin
4. **Measure Coverage**: Track code coverage
5. **Improve Quality**: Use mutation testing
6. **Maintain Tests**: Refactor tests regularly

## References

### Testing Frameworks
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/assertj-core-features-highlight.html)
- [REST Assured](https://rest-assured.io/)

### Testing Methodologies
- [Test-Driven Development](https://en.wikipedia.org/wiki/Test-driven_development)
- [Domain-Driven Design](https://en.wikipedia.org/wiki/Domain-driven_design)
- [Behavior-Driven Development](https://en.wikipedia.org/wiki/Behavior-driven_development)

### Tools & Plugins
- [JaCoCo Code Coverage](https://www.jacoco.org/)
- [PIT Mutation Testing](https://pitest.org/)
- [ArchUnit](https://www.archunit.org/)
- [Cucumber](https://cucumber.io/)

### Books & Articles
- "Test Driven Development: By Example" - Kent Beck
- "Domain-Driven Design" - Eric Evans
- "The Pragmatic Programmer" - Hunt & Thomas
- "Growing Object-Oriented Software, Guided by Tests" - Freeman & Pryce

---

**Happy Testing! 🚀**

*Master advanced testing methodologies with Eclipse Vert.x*