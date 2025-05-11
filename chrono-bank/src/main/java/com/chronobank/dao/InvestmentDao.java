package com.chronobank.dao;

import com.chronobank.model.common.Investment;
import java.sql.SQLException;
import java.util.List;

public interface InvestmentDao {
    void saveInvestment(Investment investment) throws SQLException;
    List<Investment> findByAccountId(String accountId) throws SQLException;
    Investment findById(String investmentId) throws SQLException;
    void updateInvestment(Investment investment) throws SQLException;
    void deleteInvestment(String investmentId) throws SQLException;
} 