package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER
}

enum class AccountType {
    CASH, BANK, BKASH, NAGAD, ROCKET, CARD, OTHER
}

enum class DebtType {
    LENT,    // পাওনা (I gave money to someone)
    BORROWED // দেনা (I took money from someone)
}

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val accountNumber: String = "",
    val colorHex: String = "#00897B",
    val iconName: String = "wallet"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameEn: String,
    val nameBn: String,
    val type: TransactionType,
    val monthlyBudget: Double = 0.0,
    val colorHex: String = "#1E88E5",
    val iconName: String = "category"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val accountId: Long,
    val targetAccountId: Long? = null, // Used for internal transfer
    val categoryId: Long? = null,
    val categoryName: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = ""
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val phoneNumber: String = "",
    val amount: Double,
    val repaidAmount: Double = 0.0,
    val type: DebtType,
    val dueDate: String = "",
    val note: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
