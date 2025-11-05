package com.vertx.testing;

import io.vertx.core.json.JsonObject;
import java.util.*;

/**
 * User Service - Business Logic Layer
 * Implements domain logic for user management
 */
public class UserService {
    private final Map<String, User> users = new HashMap<>();

    /**
     * Create a new user
     * @param user the user to create
     * @throws IllegalArgumentException if user already exists
     */
    public void createUser(User user) {
        if (users.containsKey(user.getId())) {
            throw new IllegalArgumentException("User with ID " + user.getId() + " already exists");
        }
        users.put(user.getId(), user);
    }

    /**
     * Get user by ID
     * @param id the user ID
     * @return the user or null if not found
     */
    public User getUserById(String id) {
        return users.get(id);
    }

    /**
     * Get all users
     * @return list of all users
     */
    public List<JsonObject> getAllUsers() {
        return users.values().stream()
            .map(User::toJson)
            .toList();
    }

    /**
     * Update user
     * @param id the user ID
     * @param updates the updates to apply
     * @return the updated user
     * @throws IllegalArgumentException if user not found
     */
    public User updateUser(String id, JsonObject updates) {
        User existing = users.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("User not found");
        }

        String name = updates.getString("name", existing.getName());
        String email = updates.getString("email", existing.getEmail());
        Integer age = updates.getInteger("age", existing.getAge());

        User updated = new User(id, name, email, age);
        users.put(id, updated);
        return updated;
    }

    /**
     * Delete user
     * @param id the user ID
     * @return true if deleted, false if not found
     */
    public boolean deleteUser(String id) {
        return users.remove(id) != null;
    }

    /**
     * Get user count
     * @return number of users
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * Clear all users
     */
    public void clearAllUsers() {
        users.clear();
    }

    /**
     * Check if user exists
     * @param id the user ID
     * @return true if user exists
     */
    public boolean userExists(String id) {
        return users.containsKey(id);
    }

    /**
     * Find users by name (case-insensitive)
     * @param name the name to search for
     * @return list of matching users
     */
    public List<User> findUsersByName(String name) {
        return users.values().stream()
            .filter(u -> u.getName().toLowerCase().contains(name.toLowerCase()))
            .toList();
    }

    /**
     * Find users by age range
     * @param minAge minimum age
     * @param maxAge maximum age
     * @return list of users in age range
     */
    public List<User> findUsersByAgeRange(int minAge, int maxAge) {
        return users.values().stream()
            .filter(u -> u.getAge() >= minAge && u.getAge() <= maxAge)
            .toList();
    }
}