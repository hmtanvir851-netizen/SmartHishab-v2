package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.MoneyDao
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [AccountEntity::class, CategoryEntity::class, TransactionEntity::class, DebtEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moneyDao(): MoneyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smarthishab_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.moneyDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: MoneyDao) {
                // Default Accounts
                val initialAccounts = listOf(
                    AccountEntity(name = "Cash (ক্যাশ)", type = AccountType.CASH, balance = 5000.0, colorHex = "#4CAF50", iconName = "payments"),
                    AccountEntity(name = "Bank Account", type = AccountType.BANK, balance = 25000.0, colorHex = "#1E88E5", iconName = "account_balance"),
                    AccountEntity(name = "Bkash (বিকাশ)", type = AccountType.BKASH, balance = 3500.0, colorHex = "#E91E63", iconName = "phone_android"),
                    AccountEntity(name = "Nagad (নগদ)", type = AccountType.NAGAD, balance = 1200.0, colorHex = "#FF9800", iconName = "phonelink_ring"),
                    AccountEntity(name = "Rocket (রকেট)", type = AccountType.ROCKET, balance = 800.0, colorHex = "#9C27B0", iconName = "rocket")
                )
                initialAccounts.forEach { dao.insertAccount(it) }

                // Default Categories
                val initialCategories = listOf(
                    CategoryEntity(nameEn = "Food & Grocery", nameBn = "খাবার ও বাজার", type = TransactionType.EXPENSE, monthlyBudget = 10000.0, colorHex = "#FF5722", iconName = "shopping_cart"),
                    CategoryEntity(nameEn = "Transport", nameBn = "যাতায়াত", type = TransactionType.EXPENSE, monthlyBudget = 3000.0, colorHex = "#FFC107", iconName = "directions_bus"),
                    CategoryEntity(nameEn = "Bills & Utilities", nameBn = "বিল ও ইউটিলিটি", type = TransactionType.EXPENSE, monthlyBudget = 5000.0, colorHex = "#3F51B5", iconName = "receipt"),
                    CategoryEntity(nameEn = "Shopping", nameBn = "শপিং", type = TransactionType.EXPENSE, monthlyBudget = 4000.0, colorHex = "#E91E63", iconName = "shopping_bag"),
                    CategoryEntity(nameEn = "Healthcare", nameBn = "চিকিৎসা ও ওষুধ", type = TransactionType.EXPENSE, monthlyBudget = 2000.0, colorHex = "#F44336", iconName = "local_hospital"),
                    CategoryEntity(nameEn = "Education", nameBn = "শিক্ষা", type = TransactionType.EXPENSE, monthlyBudget = 5000.0, colorHex = "#00BCD4", iconName = "school"),
                    CategoryEntity(nameEn = "Entertainment", nameBn = "বিনোদন", type = TransactionType.EXPENSE, monthlyBudget = 2000.0, colorHex = "#9C27B0", iconName = "movie"),
                    CategoryEntity(nameEn = "Salary", nameBn = "বেতন", type = TransactionType.INCOME, colorHex = "#4CAF50", iconName = "attach_money"),
                    CategoryEntity(nameEn = "Business", nameBn = "ব্যবসা", type = TransactionType.INCOME, colorHex = "#009688", iconName = "store"),
                    CategoryEntity(nameEn = "Investment", nameBn = "বিনিয়োগ", type = TransactionType.INCOME, colorHex = "#2196F3", iconName = "trending_up"),
                    CategoryEntity(nameEn = "Other Income", nameBn = "অন্যান্য আয়", type = TransactionType.INCOME, colorHex = "#8BC34A", iconName = "account_balance_wallet")
                )
                initialCategories.forEach { dao.insertCategory(it) }

                // Initial Sample Transactions for instant visual delight
                val now = System.currentTimeMillis()
                dao.insertTransaction(
                    TransactionEntity(
                        amount = 50000.0,
                        type = TransactionType.INCOME,
                        accountId = 2, // Bank
                        categoryName = "বেতন (Salary)",
                        note = "Monthly Salary Deposit",
                        timestamp = now - 86400000 * 2,
                        dateString = "2026-07-25"
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        amount = 1200.0,
                        type = TransactionType.EXPENSE,
                        accountId = 3, // Bkash
                        categoryName = "খাবার ও বাজার (Food & Grocery)",
                        note = "Groceries from Swapno",
                        timestamp = now - 86400000,
                        dateString = "2026-07-26"
                    )
                )
            }
        }
    }
}
