package com.example.ui.screens

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.ParsedAiResult
import com.example.data.local.entities.AccountEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.TransactionType
import com.example.data.export.PdfExportHelper
import com.example.data.localization.CurrencyManager
import com.example.data.localization.LanguageManager
import com.example.data.util.CategorySuggestor
import com.example.ui.components.PieChart
import com.example.ui.viewmodel.MainViewModel
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAi: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToAnalytics: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentLang by viewModel.languageCode.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsState()

    val totalNetWorth by viewModel.totalNetWorth.collectAsState()
    val monthExpense by viewModel.currentMonthExpenses.collectAsState()
    val monthIncome by viewModel.currentMonthIncome.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val monthlyBudget by viewModel.totalMonthlyBudget.collectAsState()
    val loggedInUserId by viewModel.loggedInUserId.collectAsState()

    val isAiParsing by viewModel.isAiParsing.collectAsState()
    val pendingAiResult by viewModel.pendingAiResult.collectAsState()

    var aiInputText by remember { mutableStateOf("") }
    var isListeningVoice by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var initialAddType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var isFabExpanded by remember { mutableStateOf(false) }

    // Speech Recognizer setup
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (currentLang == "bn") "bn-BD" else "en-US")
        }
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListeningVoice = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListeningVoice = false }
            override fun onError(error: Int) { isListeningVoice = false }
            override fun onResults(results: Bundle?) {
                isListeningVoice = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    aiInputText = matches[0]
                    viewModel.parseInputWithAi(matches[0])
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer?.startListening(intent)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Net Worth Banner Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LanguageManager.getString("total_net_worth", currentLang),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleBalanceVisibility() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Balance",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBalanceHidden) "••••••••" else CurrencyManager.format(totalNetWorth, currencySymbol),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Income
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Income",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LanguageManager.getString("this_month_income", currentLang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isBalanceHidden) "••••" else CurrencyManager.formatCompact(monthIncome, currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        // Vertical Divider
                        Divider(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )

                        // Expense
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFC62828).copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Expense",
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LanguageManager.getString("this_month_expense", currentLang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isBalanceHidden) "••••" else CurrencyManager.formatCompact(monthExpense, currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }

            // AI Smart Input Bar Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LanguageManager.getString("ai_input_title", currentLang),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        TextButton(onClick = onNavigateToAi) {
                            Text(
                                text = if (currentLang == "bn") "ফুল ভিউ →" else "Full AI View →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = aiInputText,
                            onValueChange = { aiInputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_input_text_field"),
                            placeholder = {
                                Text(
                                    text = if (currentLang == "bn") "যেমন: ৫০০ টাকা গ্রোসারি বিকাশ..." else "e.g., 500 grocery with Bkash...",
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            trailingIcon = {
                                if (isAiParsing) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else if (aiInputText.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.parseInputWithAi(aiInputText) }) {
                                        Icon(Icons.Default.Send, contentDescription = "Parse AI", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Microphone Button
                        FilledIconButton(
                            onClick = { startListening() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isListeningVoice) Color.Red else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("voice_mic_button")
                        ) {
                            Icon(
                                imageVector = if (isListeningVoice) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = Color.White
                            )
                        }
                    }

                    // Quick Suggestion Chips
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val suggestions = if (currentLang == "bn") listOf(
                            "আজকে ৫০০ টাকা গ্রোসারি বিকাশ",
                            "১২০ টাকা রিকশা ভাড়া ক্যাশ",
                            "২০০০ টাকা কারেন্ট বিল ব্যাংক"
                        ) else listOf(
                            "500 grocery via Bkash",
                            "120 rickshaw fare Cash",
                            "2000 electricity bill Bank"
                        )

                        items(suggestions) { text ->
                            SuggestionChip(
                                onClick = {
                                    aiInputText = text
                                    viewModel.parseInputWithAi(text)
                                },
                                label = { Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }

            // Multi-Asset Accounts Row Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageManager.getString("accounts", currentLang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToAccounts) {
                    Text(if (currentLang == "bn") "সব দেখুন" else "View All", fontSize = 12.sp)
                }
            }

            // Accounts Horizontal Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(accounts) { account ->
                    AccountCardItem(
                        account = account,
                        currencySymbol = currencySymbol,
                        isBalanceHidden = isBalanceHidden
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Monthly Spending Breakdown (Pie Chart) Dashboard Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = "Spending Pie Chart",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLang == "bn") "মাসিক খরচের পাই চার্ট" else "Monthly Spending Breakdown",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(onClick = onNavigateToAnalytics) {
                            Text(
                                text = if (currentLang == "bn") "চার্ট ভিউ →" else "Charts →",
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    PieChart(
                        transactions = transactions,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.testTag("home_dashboard_pie_chart")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Transactions Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentLang == "bn") "সাম্প্রতিক লেনদেন" else "Recent Transactions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            PdfExportHelper.generateAndOpenTransactionPdf(
                                context = context,
                                transactions = transactions,
                                currencySymbol = currencySymbol,
                                userName = loggedInUserId.ifEmpty { "SmartHishab User" }
                            )
                        },
                        modifier = Modifier.testTag("export_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF Statement",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "${transactions.size} entries",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLang == "bn") "এখনো কোন হিসাব যুক্ত করা হয়নি।\nভয়েস বা + বাটনে ট্যাপ করে শুরু করুন!" else "No transactions added yet.\nTap Voice or + button to begin!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionRowItem(
                            transaction = tx,
                            currencySymbol = currencySymbol,
                            isBalanceHidden = isBalanceHidden,
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }

        // Expandable Quick-Add Speed Dial Floating Action Button
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Expense Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            initialAddType = TransactionType.EXPENSE
                            showAddDialog = true
                            isFabExpanded = false
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 3.dp
                        ) {
                            Text(
                                text = if (currentLang == "bn") "💸 দ্রুত খরচ" else "💸 Quick Expense",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                initialAddType = TransactionType.EXPENSE
                                showAddDialog = true
                                isFabExpanded = false
                            },
                            containerColor = Color(0xFFFFEBEE),
                            contentColor = Color(0xFFD32F2F),
                            modifier = Modifier.testTag("quick_add_expense_fab")
                        ) {
                            Icon(Icons.Default.TrendingDown, contentDescription = "Quick Expense")
                        }
                    }

                    // Quick Income Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            initialAddType = TransactionType.INCOME
                            showAddDialog = true
                            isFabExpanded = false
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 3.dp
                        ) {
                            Text(
                                text = if (currentLang == "bn") "💰 দ্রুত আয়" else "💰 Quick Income",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                initialAddType = TransactionType.INCOME
                                showAddDialog = true
                                isFabExpanded = false
                            },
                            containerColor = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF2E7D32),
                            modifier = Modifier.testTag("quick_add_income_fab")
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = "Quick Income")
                        }
                    }

                    // Quick Transfer Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            initialAddType = TransactionType.TRANSFER
                            showAddDialog = true
                            isFabExpanded = false
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 3.dp
                        ) {
                            Text(
                                text = if (currentLang == "bn") "🔁 ট্রান্সফার" else "🔁 Quick Transfer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00796B),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                initialAddType = TransactionType.TRANSFER
                                showAddDialog = true
                                isFabExpanded = false
                            },
                            containerColor = Color(0xE0E0F2F1),
                            contentColor = Color(0xFF00796B),
                            modifier = Modifier.testTag("quick_add_transfer_fab")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Quick Transfer")
                        }
                    }
                }
            }

            // Main Speed Dial FAB
            ExtendedFloatingActionButton(
                onClick = { isFabExpanded = !isFabExpanded },
                icon = {
                    Icon(
                        imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.FlashOn,
                        contentDescription = "Expand Quick Add"
                    )
                },
                text = {
                    Text(
                        text = if (isFabExpanded) {
                            if (currentLang == "bn") "বন্ধ করুন" else "Close"
                        } else {
                            if (currentLang == "bn") "দ্রুত যোগ করুন" else "Quick Add"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = if (isFabExpanded) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                contentColor = if (isFabExpanded) MaterialTheme.colorScheme.onSecondaryContainer else Color.White,
                modifier = Modifier.testTag("add_transaction_fab")
            )
        }

        // Pending AI Extracted Result Confirmation Sheet
        if (pendingAiResult != null) {
            AiConfirmationModal(
                result = pendingAiResult!!,
                currencySymbol = currencySymbol,
                currentLang = currentLang,
                accounts = accounts,
                onConfirm = { amt, type, acc, targetAcc, cat, note ->
                    viewModel.confirmPendingAiTransaction(amt, type, acc, targetAcc, cat, note)
                },
                onDismiss = { viewModel.clearPendingAi() }
            )
        }

        // Add Manual Transaction Dialog
        if (showAddDialog) {
            AddTransactionModal(
                currencySymbol = currencySymbol,
                currentLang = currentLang,
                accounts = accounts,
                initialType = initialAddType,
                onSave = { amount, type, accountId, targetId, categoryName, note ->
                    viewModel.addManualTransaction(amount, type, accountId, targetId, categoryName, note)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

@Composable
fun AccountCardItem(
    account: AccountEntity,
    currencySymbol: String,
    isBalanceHidden: Boolean
) {
    val cardColor = remember(account.colorHex) {
        try { Color(android.graphics.Color.parseColor(account.colorHex)) }
        catch (e: Exception) { Color(0xFF00897B) }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        modifier = Modifier
            .width(140.dp)
            .height(90.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = when (account.type) {
                        com.example.data.local.entities.AccountType.CASH -> Icons.Default.Payments
                        com.example.data.local.entities.AccountType.BANK -> Icons.Default.AccountBalance
                        com.example.data.local.entities.AccountType.BKASH -> Icons.Default.PhoneAndroid
                        com.example.data.local.entities.AccountType.NAGAD -> Icons.Default.PhonelinkRing
                        com.example.data.local.entities.AccountType.ROCKET -> Icons.Default.Rocket
                        else -> Icons.Default.CreditCard
                    },
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = if (isBalanceHidden) "••••" else CurrencyManager.formatCompact(account.balance, currencySymbol),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    currencySymbol: String,
    isBalanceHidden: Boolean,
    onDelete: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val isIncome = transaction.type == TransactionType.INCOME

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = when (transaction.type) {
                        TransactionType.EXPENSE -> Color(0xFFFFEBEE)
                        TransactionType.INCOME -> Color(0xE8F5E9)
                        TransactionType.TRANSFER -> Color(0xFFE3F2FD)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (transaction.type) {
                                TransactionType.EXPENSE -> Icons.Default.ArrowUpward
                                TransactionType.INCOME -> Icons.Default.ArrowDownward
                                TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                            },
                            contentDescription = null,
                            tint = when (transaction.type) {
                                TransactionType.EXPENSE -> Color.Red
                                TransactionType.INCOME -> Color(0xFF2E7D32)
                                TransactionType.TRANSFER -> Color(0xFF1976D2)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.categoryName.ifBlank { transaction.note.ifBlank { "Transaction" } },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = transaction.note.ifBlank { "No description" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (isExpense) "-" else if (isIncome) "+" else ""}${if (isBalanceHidden) "••••" else CurrencyManager.format(transaction.amount, currencySymbol)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = when (transaction.type) {
                        TransactionType.EXPENSE -> Color.Red
                        TransactionType.INCOME -> Color(0xFF2E7D32)
                        TransactionType.TRANSFER -> MaterialTheme.colorScheme.primary
                    }
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AiConfirmationModal(
    result: ParsedAiResult,
    currencySymbol: String,
    currentLang: String,
    accounts: List<AccountEntity>,
    onConfirm: (amount: Double, type: TransactionType, accountName: String, targetAccountName: String?, categoryName: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableDoubleStateOf(result.amount) }
    var type by remember { mutableStateOf(result.type) }
    var accountName by remember { mutableStateOf(result.accountName) }
    var categoryName by remember { mutableStateOf(result.categoryName) }
    var note by remember { mutableStateOf(result.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentLang == "bn") "এআই চিহ্নিত এন্ট্রি কনফার্ম করুন" else "Confirm AI Extracted Entry",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (result.aiAdvice.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "💡 ${result.aiAdvice}",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = amount.toString(),
                    onValueChange = { amount = it.toDoubleOrNull() ?: 0.0 },
                    label = { Text("Amount ($currencySymbol)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account (Cash, Bkash, Bank...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(amount, type, accountName, result.targetAccountName, categoryName, note)
                }
            ) {
                Text(if (currentLang == "bn") "সেভ করুন" else "Save Transaction")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLang == "bn") "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
fun AddTransactionModal(
    currencySymbol: String,
    currentLang: String,
    accounts: List<AccountEntity>,
    initialType: TransactionType = TransactionType.EXPENSE,
    onSave: (amount: Double, type: TransactionType, accountId: Long, targetId: Long?, categoryName: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    var selectedAccountId by remember { mutableLongStateOf(accounts.firstOrNull()?.id ?: 1) }
    var selectedTargetId by remember { mutableStateOf<Long?>(null) }
    var categoryName by remember { mutableStateOf("Food & Grocery") }
    var note by remember { mutableStateOf("") }
    var isCategoryUserEdited by remember { mutableStateOf(false) }

    val suggestedCategory = remember(note) { CategorySuggestor.suggestCategory(note) }

    LaunchedEffect(suggestedCategory) {
        if (suggestedCategory != null && !isCategoryUserEdited) {
            categoryName = suggestedCategory
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (currentLang == "bn") "নতুন লেনদেন যোগ করুন" else "Add New Transaction", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Switcher Tabs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TransactionType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_transaction_amount_input")
                )

                // Quick Amount Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(50, 100, 500, 1000)
                    presets.forEach { preset ->
                        AssistChip(
                            onClick = {
                                val currentVal = amountText.toDoubleOrNull() ?: 0.0
                                val newVal = currentVal + preset
                                amountText = if (newVal % 1.0 == 0.0) newVal.toLong().toString() else newVal.toString()
                            },
                            label = { Text("+$preset", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (currentLang == "bn") "বিবরণ / নোট (যেমন: বাস ভাড়া, বাজার...)" else "Note / Description (e.g. Bus fare, Grocery...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        isCategoryUserEdited = true
                    },
                    label = { Text("Category (Grocery, Transport, Bills...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                suggestedCategory?.let { suggestion ->
                    AssistChip(
                        onClick = {
                            categoryName = suggestion
                            isCategoryUserEdited = true
                        },
                        label = {
                            Text(
                                text = "💡 ${if (currentLang == "bn") "পরামর্শকৃত ক্যাটাগরি" else "Suggested"}: $suggestion",
                                fontSize = 12.sp
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("suggested_category_chip")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(amt, selectedType, selectedAccountId, selectedTargetId, categoryName, note)
                    }
                }
            ) {
                Text(if (currentLang == "bn") "যোগ করুন" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLang == "bn") "বাতিল" else "Cancel")
            }
        }
    )
}
