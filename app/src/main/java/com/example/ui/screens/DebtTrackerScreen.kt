package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.DebtEntity
import com.example.data.local.entities.DebtType
import com.example.data.localization.CurrencyManager
import com.example.data.localization.LanguageManager
import com.example.data.util.PaymentReminderHelper
import com.example.data.util.ReminderLanguageOption
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DebtTrackerScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val currentLang by viewModel.languageCode.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val debts by viewModel.debts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Lent (পাওনা), 1: Borrowed (দেনা)
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDebtForRepayment by remember { mutableStateOf<DebtEntity?>(null) }
    var selectedDebtForReminder by remember { mutableStateOf<DebtEntity?>(null) }

    val activeDebts = remember(debts, selectedTab) {
        val type = if (selectedTab == 0) DebtType.LENT else DebtType.BORROWED
        debts.filter { it.type == type }
    }

    val totalLent = remember(debts) { debts.filter { it.type == DebtType.LENT && !it.isSettled }.sumOf { it.amount } }
    val totalBorrowed = remember(debts) { debts.filter { it.type == DebtType.BORROWED && !it.isSettled }.sumOf { it.amount } }

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
                text = LanguageManager.getString("debt_tracker_title", currentLang),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Debt", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Row Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = LanguageManager.getString("i_lent", currentLang), fontSize = 11.sp, color = Color(0xFF2E7D32))
                    Text(text = CurrencyManager.format(totalLent, currencySymbol), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = LanguageManager.getString("i_borrowed", currentLang), fontSize = 11.sp, color = Color.Red)
                    Text(text = CurrencyManager.format(totalBorrowed, currencySymbol), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(LanguageManager.getString("i_lent", currentLang)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(LanguageManager.getString("i_borrowed", currentLang)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeDebts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentLang == "bn") "কোন দেনা-পাওনার খাতা নেই" else "No debt/loan entries",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeDebts) { item ->
                    val effectiveRepaid = if (item.isSettled) item.amount else item.repaidAmount.coerceIn(0.0, item.amount)
                    val progressRatio = if (item.amount > 0) (effectiveRepaid / item.amount).toFloat().coerceIn(0f, 1f) else 0f
                    val percentageInt = (progressRatio * 100).toInt()
                    val remainingAmount = (item.amount - effectiveRepaid).coerceAtLeast(0.0)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("debt_item_card_${item.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth()
                        ) {
                            // Top Row: Person Info, Amount & Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.toggleDebtSettled(item) }) {
                                        Icon(
                                            imageVector = if (item.isSettled) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                            contentDescription = "Settle",
                                            tint = if (item.isSettled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = item.personName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (item.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (item.dueDate.isNotBlank()) {
                                            Text(
                                                text = "${if (currentLang == "bn") "মেয়াদ:" else "Due:"} ${item.dueDate}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = CurrencyManager.format(item.amount, currencySymbol),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (item.type == DebtType.LENT) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )

                                    IconButton(
                                        onClick = { selectedDebtForReminder = item },
                                        modifier = Modifier.testTag("reminder_button_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Send Reminder",
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    if (item.phoneNumber.isNotBlank()) {
                                        IconButton(onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.phoneNumber}"))
                                            context.startActivity(intent)
                                        }) {
                                            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteDebt(item) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Visual Progress Bar Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentLang == "bn")
                                        "পরিশোধিত: ${CurrencyManager.format(effectiveRepaid, currencySymbol)} / ${CurrencyManager.format(item.amount, currencySymbol)}"
                                    else
                                        "Repaid: ${CurrencyManager.format(effectiveRepaid, currencySymbol)} / ${CurrencyManager.format(item.amount, currencySymbol)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "$percentageInt% ${if (currentLang == "bn") "পরিশোধিত" else "Repaid"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isSettled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val barColor = if (item.isSettled) Color(0xFF4CAF50)
                            else if (item.type == DebtType.LENT) Color(0xFF2E7D32)
                            else Color(0xFFE65100)

                            val trackColor = if (item.type == DebtType.LENT) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("debt_progress_bar_${item.id}"),
                                color = barColor,
                                trackColor = trackColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Bottom Actions Row: Record Repayment or Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!item.isSettled) {
                                    Text(
                                        text = if (currentLang == "bn")
                                            "বাকি: ${CurrencyManager.format(remainingAmount, currencySymbol)}"
                                        else
                                            "Remaining: ${CurrencyManager.format(remainingAmount, currencySymbol)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    OutlinedButton(
                                        onClick = { selectedDebtForRepayment = item },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("record_repayment_button_${item.id}")
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (currentLang == "bn") "+ পরিশোধ যোগ করুন" else "+ Record Repayment",
                                            fontSize = 11.sp
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (currentLang == "bn") "সম্পূর্ণ পরিশোধিত" else "Fully Settled",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Record Repayment Dialog
    selectedDebtForRepayment?.let { targetDebt ->
        val effectiveRepaid = if (targetDebt.isSettled) targetDebt.amount else targetDebt.repaidAmount.coerceIn(0.0, targetDebt.amount)
        val remaining = (targetDebt.amount - effectiveRepaid).coerceAtLeast(0.0)
        var repaymentInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedDebtForRepayment = null },
            title = {
                Text(
                    text = if (currentLang == "bn") "পরিশোধ রসিদ (${targetDebt.personName})" else "Record Repayment (${targetDebt.personName})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (currentLang == "bn")
                            "মোট ঋণ: ${CurrencyManager.format(targetDebt.amount, currencySymbol)} | বাকি: ${CurrencyManager.format(remaining, currencySymbol)}"
                        else
                            "Total: ${CurrencyManager.format(targetDebt.amount, currencySymbol)} | Remaining: ${CurrencyManager.format(remaining, currencySymbol)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = repaymentInput,
                        onValueChange = { repaymentInput = it },
                        label = { Text(if (currentLang == "bn") "পরিশোধের পরিমাণ ($currencySymbol)" else "Repaid Amount ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("repayment_amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val addedAmt = repaymentInput.toDoubleOrNull() ?: 0.0
                        if (addedAmt > 0) {
                            viewModel.recordDebtRepayment(targetDebt, addedAmt)
                            selectedDebtForRepayment = null
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (currentLang == "bn") "পরিশোধ কনফার্ম করুন" else "Confirm Repayment")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDebtForRepayment = null }) {
                    Text(if (currentLang == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Payment Reminder Dialog
    selectedDebtForReminder?.let { debt ->
        val effectiveRepaid = if (debt.isSettled) debt.amount else debt.repaidAmount.coerceIn(0.0, debt.amount)
        val remaining = (debt.amount - effectiveRepaid).coerceAtLeast(0.0)
        val rawAmount = if (remaining > 0) remaining else debt.amount
        val amountNumStr = if (rawAmount % 1.0 == 0.0) rawAmount.toLong().toString() else String.format("%.2f", rawAmount)
        val amountDisplay = CurrencyManager.format(rawAmount, currencySymbol)

        val loggedInUserId by viewModel.loggedInUserId.collectAsState()
        val userName = loggedInUserId.ifEmpty { "Smart Hishab User" }

        var selectedOption by remember { mutableStateOf(ReminderLanguageOption.BANGLA) }
        var customMessageText by remember(selectedOption, debt, amountNumStr, userName) {
            mutableStateOf(
                PaymentReminderHelper.generateReminderText(
                    contactName = debt.personName,
                    dueDate = debt.dueDate,
                    amountText = amountNumStr,
                    userName = userName,
                    option = selectedOption
                )
            )
        }

        AlertDialog(
            onDismissRequest = { selectedDebtForReminder = null },
            title = {
                Text(
                    text = if (currentLang == "bn") "বকেয়া পাওনা রিমাইন্ডার" else "Payment Reminder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (currentLang == "bn")
                            "প্রাপক: ${debt.personName} | বকেয়া: $amountDisplay"
                        else
                            "Recipient: ${debt.personName} | Dues: $amountDisplay",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (currentLang == "bn") "টেমপ্লেট ভাষা নির্বাচন করুন:" else "Select Template Language:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedOption == ReminderLanguageOption.BANGLA,
                            onClick = { selectedOption = ReminderLanguageOption.BANGLA },
                            label = { Text("বাংলা", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("reminder_option_bangla")
                        )
                        FilterChip(
                            selected = selectedOption == ReminderLanguageOption.ENGLISH,
                            onClick = { selectedOption = ReminderLanguageOption.ENGLISH },
                            label = { Text("English", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("reminder_option_english")
                        )
                        FilterChip(
                            selected = selectedOption == ReminderLanguageOption.COMBINED,
                            onClick = { selectedOption = ReminderLanguageOption.COMBINED },
                            label = { Text("উভয় (Both)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("reminder_option_combined")
                        )
                    }

                    OutlinedTextField(
                        value = customMessageText,
                        onValueChange = { customMessageText = it },
                        label = { Text(if (currentLang == "bn") "বার্তা প্রিভিউ (এডিটেবল)" else "Message Preview (Editable)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .testTag("reminder_message_preview"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (debt.phoneNumber.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:${debt.phoneNumber}")
                                    putExtra("sms_body", customMessageText)
                                }
                                context.startActivity(smsIntent)
                                selectedDebtForReminder = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("send_sms_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentLang == "bn") "Direct SMS" else "Direct SMS")
                        }
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, customMessageText)
                            }
                            val chooserIntent = Intent.createChooser(
                                shareIntent,
                                if (currentLang == "bn") "রিমাইন্ডার পাঠান" else "Send Payment Reminder"
                            )
                            context.startActivity(chooserIntent)
                            selectedDebtForReminder = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("share_reminder_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLang == "bn") "রিমাইন্ডার পাঠান (Share)" else "Send Reminder (Share)")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDebtForReminder = null }) {
                    Text(if (currentLang == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }

    if (showAddDialog) {
        var personName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var dueDate by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (currentLang == "bn") "নতুন দেনা/পাওনা এন্ট্রি" else "Add New Debt / Loan Record") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text("Person Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (e.g., 2026-08-15)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (personName.isNotBlank() && amt > 0) {
                            viewModel.addDebt(
                                personName = personName,
                                phone = phone,
                                amount = amt,
                                type = if (selectedTab == 0) DebtType.LENT else DebtType.BORROWED,
                                dueDate = dueDate,
                                note = note
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(if (currentLang == "bn") "সেভ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (currentLang == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }
}
