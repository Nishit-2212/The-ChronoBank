package com.chronobank.dao;

import com.chronobank.model.user.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    /**
     * @param user The user object to save.
     */
    void saveUser(User user);

    /**
     * @param userId The ID of the user to find.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findUserById(String userId);

    /**
     * @param username The username of the user to find.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findUserByUsername(String username);

    /**
     * @param email The email of the user to find.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findUserByEmail(String email);

    /**
     * @return A list of all users.
     */
    List<User> findAllUsers();

    /**
     * @param user The user object with updated information.
     */
    void updateUser(User user);

    /**
     * @param userId The ID of the user to delete.
     */
    void deleteUser(String userId);

    void save(User user) throws SQLException;
    Optional<User> findById(String userId) throws SQLException;
    Optional<User> findByUsername(String username) throws SQLException;
    Optional<User> findByEmail(String email) throws SQLException;
    List<User> findAll() throws SQLException;
    void updatePassword(String userId, String newPasswordHash) throws SQLException;
    void updateReputationScore(String userId, int newScore) throws SQLException;
}

