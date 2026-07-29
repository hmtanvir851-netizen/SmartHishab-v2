package com.example.data.repository

import com.example.data.local.UserPreferences
import com.example.data.local.dao.MoneyDao
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

class MoneyRepository(
    private val dao: MoneyDao,
    val preferences: UserPreferences
) {
    val allAccounts: Flow<List<AccountEntity>> = dao.getAllAccounts()
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allDebts: Flow<List<DebtEntity>> = dao.getAllDebts()

    suspend fun addAccount(account: AccountEntity) = dao.insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = dao.updateAccount(account)
    suspend fun deleteAccount(account: AccountEntity) = dao.deleteAccount(account)

    suspend fun addCategory(category: CategoryEntity) = dao.insertCategory(category)

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        val id = dao.insertTransaction(transaction)

        // Auto balance adjust
        val account = dao.getAccountById(transaction.accountId)
        if (account != null) {
            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    dao.updateAccount(account.copy(balance = account.balance - transaction.amount))
                }
                TransactionType.INCOME -> {
                    dao.updateAccount(account.copy(balance = account.balance + transaction.amount))
                }
                TransactionType.TRANSFER -> {
                    dao.updateAccount(account.copy(balance = account.balance - transaction.amount))
                    transaction.targetAccountId?.let { targetId ->
                        val targetAcc = dao.getAccountById(targetId)
                        if (targetAcc != null) {
                            dao.updateAccount(targetAcc.copy(balance = targetAcc.balance + transaction.amount))
                        }
                    }
                }
            }
        }
        return id
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
        // Reverse balance update
        val account = dao.getAccountById(transaction.accountId)
        if (account != null) {
            when (transaction.type) {
                TransactionType.EXPENSE -> dao.updateAccount(account.copy(balance = account.balance + transaction.amount))
                TransactionType.INCOME -> dao.updateAccount(account.copy(balance = account.balance - transaction.amount))
                TransactionType.TRANSFER -> {
                    dao.updateAccount(account.copy(balance = account.balance + transaction.amount))
                    transaction.targetAccountId?.let { targetId ->
                        val targetAcc = dao.getAccountById(targetId)
                        if (targetAcc != null) {
                            dao.updateAccount(targetAcc.copy(balance = targetAcc.balance - transaction.amount))
                        }
                    }
                }
            }
        }
    }

    suspend fun addDebt(debt: DebtEntity) = dao.insertDebt(debt)
    suspend fun updateDebt(debt: DebtEntity) = dao.updateDebt(debt)
    suspend fun deleteDebt(debt: DebtEntity) = dao.deleteDebt(debt)

    suspend fun exportBackupData(): String {
        val accounts = dao.getAllAccountsList()
        val categories = dao.getAllCategoriesList()
        val transactions = dao.getAllTransactionsList()
        val debts = dao.getAllDebtsList()

        return com.example.data.backup.BackupHelper.generateBackupString(
            accounts = accounts,
            categories = categories,
            transactions = transactions,
            debts = debts
        )
    }

    suspend fun restoreBackupData(backupResult: com.example.data.backup.BackupParseResult): Boolean {
        if (!backupResult.isValid) return false

        dao.clearAccounts()
        dao.clearCategories()
        dao.clearTransactions()
        dao.clearDebts()

        if (backupResult.accounts.isNotEmpty()) {
            dao.insertAccountsList(backupResult.accounts)
        }
        if (backupResult.categories.isNotEmpty()) {
            dao.insertCategoriesList(backupResult.categories)
        }
        if (backupResult.transactions.isNotEmpty()) {
            dao.insertTransactionsList(backupResult.transactions)
        }
        if (backupResult.debts.isNotEmpty()) {
            dao.insertDebtsList(backupResult.debts)
        }

        return true
    }
}
