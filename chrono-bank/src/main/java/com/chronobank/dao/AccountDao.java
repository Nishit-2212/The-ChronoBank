package com.chronobank.dao;

import com.chronobank.model.account.TimeAccount;
import java.sql.SQLException; // Added import
import java.util.List;
import java.util.Optional;

public interface AccountDao {
    /**
     * @param account The account object to save.
     * @throws SQLException if a database access error occurs.
     */
    void saveAccount(TimeAccount account) throws SQLException; // Added throws SQLException

    /**
     * @param accountId The ID of the account to find.
     * @return An Optional containing the account if found, or an empty Optional otherwise.
     */
    Optional<TimeAccount> findAccountById(String accountId) throws SQLException; // Added throws SQLException

    /**
     * @param userId The ID of the user whose accounts are to be found.
     * @return A list of accounts belonging to the user.
     */
    List<TimeAccount> findAccountsByUserId(String userId) throws SQLException; // Added throws SQLException

    /**
     * @return A list of all accounts.
     */
    List<TimeAccount> findAll() throws SQLException; // Added throws SQLException

    /**
     * @param account The account object with updated information.
     * @throws SQLException if a database access error occurs.
     */
    void updateAccount(TimeAccount account) throws SQLException; // Added throws SQLException

    /**
     * @param accountId The ID of the account to delete.
     * @throws SQLException if a database access error occurs.
     */
    void deleteAccount(String accountId) throws SQLException;
}

