package com.h1194.keuanganku.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insertTransaction(Transaction transaction);

    @Delete
    void deleteTransaction(Transaction transaction);

    @Query("SELECT * FROM transaction_table")
    List<Transaction> getAllTransactions();

    @Query("SELECT SUM(amount) FROM transaction_table WHERE type = :type")
    Float getTotalByType(String type);

    @Query("SELECT SUM(amount) FROM transaction_table WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    Float getTotalByTypeAndDateRange(String type, Date startDate, Date endDate);
}
