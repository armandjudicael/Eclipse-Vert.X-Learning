package com.vertx.testing;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests for UserService (TDD - Test-Driven Development)
 * Tests focus on business logic and service behavior
 */
@DisplayName("User Service Tests")
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void testCreateUserSuccessfully() {
            User user = new User("1", "John Doe", "john@example.com", 30);
            userService.createUser(user);

            assertThat(userService.getUserCount()).isEqualTo(1);
            assertThat(userService.getUserById("1")).isEqualTo(user);
        }

        @Test
        @DisplayName("Should throw exception when creating duplicate user")
        void testCreateDuplicateUserThrowsException() {
            User user = new User("1", "John Doe", "john@example.com", 30);
            userService.createUser(user);

            User duplicate = new User("1", "Jane Doe", "jane@example.com", 25);
            assertThatThrownBy(() -> userService.createUser(duplicate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with ID 1 already exists");
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should retrieve user by ID")
        void testGetUserById() {
            User user = new User("1", "John Doe", "john@example.com", 30);
            userService.createUser(user);

            User retrieved = userService.getUserById("1");
            assertThat(retrieved).isEqualTo(user);
        }

        @Test
        @DisplayName("Should return null for non-existent user")
        void testGetNonExistentUser() {
            User retrieved = userService.getUserById("999");
            assertThat(retrieved).isNull();
        }

        @Test
        @DisplayName("Should get all users")
        void testGetAllUsers() {
            userService.createUser(new User("1", "John", "john@example.com", 30));
            userService.createUser(new User("2", "Jane", "jane@example.com", 25));

            List<JsonObject> users = userService.getAllUsers();
            assertThat(users).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void testUpdateUserSuccessfully() {
            User original = new User("1", "John Doe", "john@example.com", 30);
            userService.createUser(original);

            JsonObject updates = new JsonObject()
                .put("name", "John Updated")
                .put("age", 31);

            User updated = userService.updateUser("1", updates);

            assertThat(updated.getName()).isEqualTo("John Updated");
            assertThat(updated.getAge()).isEqualTo(31);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent user")
        void testUpdateNonExistentUserThrowsException() {
            JsonObject updates = new JsonObject().put("name", "Updated");

            assertThatThrownBy(() -> userService.updateUser("999", updates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void testDeleteUserSuccessfully() {
            User user = new User("1", "John Doe", "john@example.com", 30);
            userService.createUser(user);

            boolean deleted = userService.deleteUser("1");

            assertThat(deleted).isTrue();
            assertThat(userService.getUserById("1")).isNull();
        }

        @Test
        @DisplayName("Should return false when deleting non-existent user")
        void testDeleteNonExistentUserReturnsFalse() {
            boolean deleted = userService.deleteUser("999");
            assertThat(deleted).isFalse();
        }
    }

    @Nested
    @DisplayName("Search User Tests")
    class SearchUserTests {

        @BeforeEach
        void setUpUsers() {
            userService.createUser(new User("1", "John Doe", "john@example.com", 30));
            userService.createUser(new User("2", "Jane Doe", "jane@example.com", 25));
            userService.createUser(new User("3", "Bob Smith", "bob@example.com", 35));
        }

        @Test
        @DisplayName("Should find users by name")
        void testFindUsersByName() {
            List<User> results = userService.findUsersByName("Doe");

            assertThat(results)
                .hasSize(2)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Doe");
        }

        @Test
        @DisplayName("Should find users by age range")
        void testFindUsersByAgeRange() {
            List<User> results = userService.findUsersByAgeRange(25, 30);

            assertThat(results)
                .hasSize(2)
                .extracting(User::getAge)
                .containsExactlyInAnyOrder(25, 30);
        }

        @Test
        @DisplayName("Should return empty list when no users match search")
        void testSearchReturnsEmptyList() {
            List<User> results = userService.findUsersByName("NonExistent");
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Utility Tests")
    class UtilityTests {

        @Test
        @DisplayName("Should check if user exists")
        void testUserExists() {
            User user = new User("1", "John Doe", "john@example.com", 30);
            userService.createUser(user);

            assertThat(userService.userExists("1")).isTrue();
            assertThat(userService.userExists("999")).isFalse();
        }

        @Test
        @DisplayName("Should clear all users")
        void testClearAllUsers() {
            userService.createUser(new User("1", "John", "john@example.com", 30));
            userService.createUser(new User("2", "Jane", "jane@example.com", 25));

            userService.clearAllUsers();

            assertThat(userService.getUserCount()).isZero();
        }
    }
}