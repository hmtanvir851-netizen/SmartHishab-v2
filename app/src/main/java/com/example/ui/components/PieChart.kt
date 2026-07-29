package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.TransactionEntity
import com.example.data.localization.CurrencyManager

data class CategoryPieData(
    val categoryName: String,
    val totalAmount: Double,
    val color: Color
)

@Composable
fun PieChart(
    transactions: List<TransactionEntity>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val expenseTransactions = transactions.filter { it.type == com.example.data.local.entities.TransactionType.EXPENSE }
    val grouped = expenseTransactions.groupBy { it.categoryName.ifBlank { "Other" } }

    val chartColor = Color(0xFF00897B) // Premium Soft Teal / Emerald Green

    val pieDataList = remember(grouped) {
        grouped.entries
            .map { entry ->
                Pair(entry.key, entry.value.sumOf { it.amount })
            }
            .sortedByDescending { it.second }
            .map { (categoryName, totalAmount) ->
                CategoryPieData(
                    categoryName = categoryName,
                    totalAmount = totalAmount,
                    color = chartColor
                )
            }
    }

    val totalExpense = pieDataList.sumOf { it.totalAmount }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(pieDataList) {
        progress.animateTo(1f, animationSpec = tween(1000))
    }

    if (pieDataList.isEmpty() || totalExpense == 0.0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expense data to display chart",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                val strokeWidth = 32.dp.toPx()
                val gapAngle = if (pieDataList.size > 1) 2.5f else 0f

                // Draw background track ring
                drawArc(
                    color = Color(0xFFE0F2F1).copy(alpha = 0.5f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                pieDataList.forEach { data ->
                    val rawSweep = ((data.totalAmount / totalExpense) * 360f * progress.value).toFloat()
                    val sweepAngle = (rawSweep - gapAngle).coerceAtLeast(0f)
                    if (rawSweep > 0f) {
                        drawArc(
                            color = data.color,
                            startAngle = startAngle + gapAngle / 2f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += rawSweep
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Expense",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyManager.formatCompact(totalExpense, currencySymbol),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pieDataList.take(5).forEach { data ->
                val percentage = (data.totalAmount / totalExpense * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = CircleShape,
                            color = data.color
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = data.categoryName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${CurrencyManager.format(data.totalAmount, currencySymbol)} ($percentage%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
