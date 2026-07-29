package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MoneyDao {

    // Accounts
    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    // Categories
    @Query("SELECT * FROM categories ORDER BY type, nameEn ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR targetAccountId = :accountId ORDER BY timestamp DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // Debts
    @Query("SELECT * FROM debts ORDER BY isSettled ASC, createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    // Backup & Restore Bulk Operations
    @Query("SELECT * FROM accounts")
    suspend fun getAllAccountsList(): List<AccountEntity>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesList(): List<CategoryEntity>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Query("SELECT * FROM debts")
    suspend fun getAllDebtsList(): List<DebtEntity>

    @Query("DELETE FROM accounts")
    suspend fun clearAccounts()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM debts")
    suspend fun clearDebts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountsList(accounts: List<AccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoriesList(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionsList(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtsList(debts: List<DebtEntity>)
}
