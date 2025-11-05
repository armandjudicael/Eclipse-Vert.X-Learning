package com.vertx.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests for User Domain Model (TDD - Test-Driven Development)
 * Tests focus on user validation and business rules
 */
@DisplayName("User Domain Model Tests")
class UserTest {

    @Test
    @DisplayName("Should create valid user with all required fields")
    void testCreateValidUser() {
        // Arrange & Act
        User user = new User("1", "John Doe", "john@example.com", 30);

        // Assert
        assertThat(user)
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", "1")
            .hasFieldOrPropertyWithValue("name", "John Doe")
            .hasFieldOrPropertyWithValue("email", "john@example.com")
            .hasFieldOrPropertyWithValue("age", 30);
    }

    @Test
    @DisplayName("Should throw exception when ID is null")
    void testCreateUserWithNullId() {
        assertThatThrownBy(() -> new User(null, "John", "john@example.com", 30))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("ID cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when ID is empty")
    void testCreateUserWithEmptyId() {
        assertThatThrownBy(() -> new User("", "John", "john@example.com", 30))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ID cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void testCreateUserWithNullName() {
        assertThatThrownBy(() -> new User("1", null, "john@example.com", 30))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Name cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when email is invalid")
    void testCreateUserWithInvalidEmail() {
        assertThatThrownBy(() -> new User("1", "John", "invalid-email", 30))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid email format");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 151, 200})
    @DisplayName("Should throw exception for invalid age values")
    void testCreateUserWithInvalidAge(int invalidAge) {
        assertThatThrownBy(() -> new User("1", "John", "john@example.com", invalidAge))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Age must be between 0 and 150");
    }

    @ParameterizedTest
    @CsvSource({
        "john@example.com, true",
        "john.doe@example.co.uk, true",
        "john+tag@example.com, true",
        "invalid.email, false",
        "@example.com, false",
        "john@, false"
    })
    @DisplayName("Should validate email format correctly")
    void testEmailValidation(String email, boolean shouldBeValid) {
        if (shouldBeValid) {
            assertThatCode(() -> new User("1", "John", email, 30))
                .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> new User("1", "John", email, 30))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("Should convert user to JSON correctly")
    void testUserToJson() {
        User user = new User("1", "John Doe", "john@example.com", 30);
        var json = user.toJson();

        assertThat(json)
            .containsEntry("id", "1")
            .containsEntry("name", "John Doe")
            .containsEntry("email", "john@example.com")
            .containsEntry("age", 30);
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void testUserEquality() {
        User user1 = new User("1", "John Doe", "john@example.com", 30);
        User user2 = new User("1", "John Doe", "john@example.com", 30);
        User user3 = new User("2", "Jane Doe", "jane@example.com", 25);

        assertThat(user1)
            .isEqualTo(user2)
            .isNotEqualTo(user3);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void testUserHashCode() {
        User user1 = new User("1", "John Doe", "john@example.com", 30);
        User user2 = new User("1", "John Doe", "john@example.com", 30);

        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }
}