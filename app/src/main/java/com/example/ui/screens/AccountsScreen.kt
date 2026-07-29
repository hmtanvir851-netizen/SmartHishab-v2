package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AccountEntity
import com.example.data.local.entities.AccountType
import com.example.data.local.entities.TransactionType
import com.example.data.localization.CurrencyManager
import com.example.data.localization.LanguageManager
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AccountsScreen(
    viewModel: MainViewModel
) {
    val currentLang by viewModel.languageCode.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LanguageManager.getString("accounts", currentLang),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row {
                IconButton(onClick = { showTransferDialog = true }) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Internal Transfer", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showAddAccountDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Account", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Accounts list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(accounts) { acc ->
                val cardColor = remember(acc.colorHex) {
                    try { Color(android.graphics.Color.parseColor(acc.colorHex)) }
                    catch (e: Exception) { Color(0xFF00897B) }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = acc.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = acc.type.name,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Text(
                            text = if (isBalanceHidden) "••••" else CurrencyManager.format(acc.balance, currencySymbol),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showAddAccountDialog) {
        var name by remember { mutableStateOf("") }
        var initialBalance by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = { Text(if (currentLang == "bn") "নতুন অ্যাকাউন্ট/ওয়ালেট" else "Add New Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Account Name (Bank, Bkash, Card...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text("Initial Balance ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bal = initialBalance.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) {
                            viewModel.addAccount(name, AccountType.OTHER, bal, "#00897B")
                            showAddAccountDialog = false
                        }
                    }
                ) {
                    Text(if (currentLang == "bn") "যোগ করুন" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) {
                    Text(if (currentLang == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }

    if (showTransferDialog && accounts.size >= 2) {
        var transferAmount by remember { mutableStateOf("") }
        var fromAccId by remember { mutableLongStateOf(accounts[0].id) }
        var toAccId by remember { mutableLongStateOf(accounts[1].id) }

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text(LanguageManager.getString("internal_transfer", currentLang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it },
                        label = { Text("Amount ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("From Account:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        accounts.take(3).forEach { acc ->
                            FilterChip(
                                selected = fromAccId == acc.id,
                                onClick = { fromAccId = acc.id },
                                label = { Text(acc.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("To Account:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        accounts.take(3).forEach { acc ->
                            FilterChip(
                                selected = toAccId == acc.id,
                                onClick = { toAccId = acc.id },
                                label = { Text(acc.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = transferAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && fromAccId != toAccId) {
                            viewModel.addManualTransaction(
                                amount = amt,
                                type = TransactionType.TRANSFER,
                                accountId = fromAccId,
                                targetAccountId = toAccId,
                                categoryName = "Internal Transfer",
                                note = "Transfer between accounts"
                            )
                            showTransferDialog = false
                        }
                    }
                ) {
                    Text(if (currentLang == "bn") "ট্রান্সফার করুন" else "Transfer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text(if (currentLang == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }
}
