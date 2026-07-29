package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.PdfExportHelper
import com.example.data.localization.CurrencyManager
import com.example.data.localization.LanguageManager
import com.example.ui.components.PieChart
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel
) {
    val currentLang by viewModel.languageCode.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val monthExpense by viewModel.currentMonthExpenses.collectAsState()
    val monthlyBudget by viewModel.totalMonthlyBudget.collectAsState()
    val aiSpendingInsight by viewModel.aiSpendingInsight.collectAsState()
    val isAnalyzingHabits by viewModel.isAnalyzingHabits.collectAsState()

    val context = LocalContext.current
    val loggedInUserId by viewModel.loggedInUserId.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf(monthlyBudget.toInt().toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = LanguageManager.getString("analytics", currentLang),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Gemini AI Spending Habits Analysis Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini AI Insights",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLang == "bn") "জেমিলাই এআই খরচের অভ্যাস বিশ্লেষণ" else "Gemini AI Spending Insights",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Button(
                            onClick = { viewModel.analyzeSpendingHabitsWithGemini() },
                            enabled = !isAnalyzingHabits,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("analyze_spending_habits_button")
                        ) {
                            if (isAnalyzingHabits) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (currentLang == "bn") "বিশ্লেষণ করুন" else "Analyze",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (aiSpendingInsight != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = aiSpendingInsight!!,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentLang == "bn")
                                "জেমিলাই এআই আপনার ক্যাটাগরি অনুযায়ী খরচের প্যাটার্ন বিশ্লেষণ করে সেভিংস টিপস প্রদান করবে।"
                            else
                                "Gemini AI analyzes your spending habits across categories to provide personalized tips.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // PDF Statement Export Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Report",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (currentLang == "bn") "পিডিএফ স্টেটমেন্ট ডাউনলোড" else "Download PDF Statement",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = if (currentLang == "bn") "সমস্ত লেনদেনের বিস্তারিত রিপোর্ট" else "Full transaction history report",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            PdfExportHelper.generateAndOpenTransactionPdf(
                                context = context,
                                transactions = transactions,
                                currencySymbol = currencySymbol,
                                userName = loggedInUserId.ifEmpty { "SmartHishab User" }
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("download_pdf_statement_button")
                    ) {
                        Text(if (currentLang == "bn") "ডাউনলোড" else "Export")
                    }
                }
            }
        }

        // Pie Chart Section Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (currentLang == "bn") "ক্যাটাগরিভিত্তিক খরচের পাই-চার্ট" else "Category Expense Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PieChart(
                        transactions = transactions,
                        currencySymbol = currencySymbol
                    )
                }
            }
        }

        // Budget Limit & Alert Status Card
        item {
            val budgetRatio = if (monthlyBudget > 0) (monthExpense / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
            val isOver80 = budgetRatio >= 0.8f
            val isOver100 = budgetRatio >= 1.0f

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isOver100) Color(0xFFFFEBEE) else if (isOver80) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LanguageManager.getString("budget_warnings", currentLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isOver100) Color.Red else if (isOver80) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        IconButton(onClick = { showBudgetDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Budget")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { budgetRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = if (isOver100) Color.Red else if (isOver80) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${if (currentLang == "bn") "খরচ" else "Spent"}: ${CurrencyManager.format(monthExpense, currencySymbol)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (currentLang == "bn") "বাজেট" else "Limit"}: ${CurrencyManager.format(monthlyBudget, currencySymbol)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isOver100) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentLang == "bn") "⚠️ ১০০% বাজেট সীমা পার হয়ে গেছে!" else "⚠️ 100% Budget limit exceeded!",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (isOver80) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentLang == "bn") "⚡ ৮০% বাজেট সতর্কতা পার হয়েছে!" else "⚡ Passed 80% budget alert limit!",
                            color = Color(0xFFE65100),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(if (currentLang == "bn") "মাসিক বাজেট নির্ধারণ করুন" else "Set Monthly Budget Limit") },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Budget Amount ($currencySymbol)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newB = budgetInput.toDoubleOrNull() ?: 30000.0
                        viewModel.setMonthlyBudget(newB)
                        showBudgetDialog = false
                    }
                ) {
                    Text(if (currentLang == "bn") "সেভ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text(if (currentLang == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }
}
