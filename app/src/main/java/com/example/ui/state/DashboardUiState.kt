package com.example.ui.state

import com.example.data.ai.ParsedAiResult
import com.example.data.local.entities.AccountEntity
import com.example.data.local.entities.TransactionEntity

/**
 * Single source of truth for the Dashboard / Main Screen UI state.
 * Fully reactive via StateFlow in ViewModel.
 */
data class DashboardUiState(
    val totalNetWorth: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val accounts: List<AccountEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val isBalanceHidden: Boolean = false,
    val isAiParsing: Boolean = false,
    val pendingAiResult: ParsedAiResult? = null,
    val currencySymbol: String = "৳",
    val languageCode: String = "en",
    val show80PercentAlert: Boolean = false,
    val show100PercentAlert: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
