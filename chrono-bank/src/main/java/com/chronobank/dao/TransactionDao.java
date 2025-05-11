package com.chronobank.dao;

import com.chronobank.model.transaction.Transaction;
import com.chronobank.model.transaction.TransactionRecord; // Using TransactionRecord for ledger queries
import java.sql.SQLException;

import java.util.List;
import java.util.Optional;

public interface TransactionDao {
    /**
     * @param transaction The transaction object to save.
     */
    void saveTransaction(Transaction transaction) throws SQLException;

    /**
     * @param transactionId The ID of the transaction to find.
     * @return An Optional containing the transaction if found, or an empty Optional otherwise.
     */
    Optional<Transaction> findById(String transactionId) throws SQLException;

    /**
     * @param accountId The ID of the account.
     * @return A list of transactions associated with the account.
     */
    List<TransactionRecord> findByAccountId(String accountId) throws SQLException;

    /**
     * @return A list of all transactions (as TransactionRecords for overview).
     */
    List<TransactionRecord> findAll() throws SQLException;

    /**
     * @param transaction The transaction object with updated information.
     */
    void updateTransaction(Transaction transaction);

    /**
     * @param record The TransactionRecord to save to the audit log.
     */
    void saveTransactionRecordToLog(TransactionRecord record);

    /**
     * @param transactionId The ID of the transaction.
     * @return A list of TransactionRecords from the log.
     */
    List<TransactionRecord> findTransactionRecordsFromLog(String transactionId);
}

