package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.CurrencyManager
import com.example.data.localization.LanguageManager

@Composable
fun BudgetAlertModal(
    is100Percent: Boolean,
    currentExpense: Double,
    monthlyBudget: Double,
    currencySymbol: String,
    currentLang: String,
    onDismiss: () -> Unit
) {
    val title = if (is100Percent) {
        if (currentLang == "bn") "⚠️ ১০০% বাজেট সীমার সতর্কতা!" else "⚠️ 100% Budget Limit Exceeded!"
    } else {
        if (currentLang == "bn") "⚡ ৮০% বাজেট সতর্কতা!" else "⚡ 80% Budget Warning!"
    }

    val description = if (is100Percent) {
        if (currentLang == "bn")
            "আপনি চলতি মাসে ${CurrencyManager.format(monthlyBudget, currencySymbol)} বাজেটের সম্পূর্ণ টাকা খরচ করে ফেলেছেন! আপনার বর্তমান খরচ: ${CurrencyManager.format(currentExpense, currencySymbol)}।"
        else
            "You have spent 100% of your monthly budget (${CurrencyManager.format(monthlyBudget, currencySymbol)})! Current spending: ${CurrencyManager.format(currentExpense, currencySymbol)}."
    } else {
        if (currentLang == "bn")
            "সাবধান! আপনার চলতি মাসের খরচ নির্ধারিত বাজেটের (${CurrencyManager.format(monthlyBudget, currencySymbol)}) ৮০% অতিক্রম করেছে। আপনার বর্তমান খরচ: ${CurrencyManager.format(currentExpense, currencySymbol)}।"
        else
            "Caution! Your monthly spending has passed 80% of your budget limit (${CurrencyManager.format(monthlyBudget, currencySymbol)}). Current spending: ${CurrencyManager.format(currentExpense, currencySymbol)}."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = if (is100Percent) Color.Red else Color(0xFFFF9800),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (is100Percent) Color.Red else Color(0xFFFF9800)
                )
            ) {
                Text(if (currentLang == "bn") "ঠিক আছে" else "Got it", color = Color.White)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
