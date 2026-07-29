package com.example.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExportHelper {

    fun generateAndOpenTransactionPdf(
        context: Context,
        transactions: List<TransactionEntity>,
        currencySymbol: String = "৳",
        userName: String = "User"
    ) {
        if (transactions.isEmpty()) {
            Toast.makeText(context, "No transactions available to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points (8.27 in)
            val pageHeight = 842 // A4 height in points (11.69 in)
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val titlePaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var y = 40f

            // --- HEADER ---
            // Header Bar
            paint.color = Color.parseColor("#1C2434")
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

            // Header Title
            titlePaint.color = Color.WHITE
            titlePaint.textSize = 22f
            canvas.drawText("SMART HISHAB", 30f, 45f, titlePaint)

            titlePaint.textSize = 12f
            titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Transaction History Statement", 30f, 68f, titlePaint)

            // Date & User
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val currentDate = sdf.format(Date())
            titlePaint.textSize = 10f
            val dateText = "Generated: $currentDate"
            val userText = "User: $userName"
            canvas.drawText(dateText, pageWidth - 180f, 45f, titlePaint)
            canvas.drawText(userText, pageWidth - 180f, 68f, titlePaint)

            y = 110f

            // --- SUMMARY CALCULATIONS ---
            var totalIncome = 0.0
            var totalExpense = 0.0
            transactions.forEach {
                when (it.type) {
                    TransactionType.INCOME -> totalIncome += it.amount
                    TransactionType.EXPENSE -> totalExpense += it.amount
                    else -> {}
                }
            }
            val netBalance = totalIncome - totalExpense

            // Summary Box
            paint.color = Color.parseColor("#F4F6F9")
            canvas.drawRoundRect(30f, y, pageWidth - 30f, y + 60f, 12f, 12f, paint)

            paint.color = Color.parseColor("#2E7D32") // Income Green
            titlePaint.color = Color.parseColor("#1B5E20")
            titlePaint.textSize = 11f
            titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Total Income", 50f, y + 25f, titlePaint)
            titlePaint.textSize = 13f
            canvas.drawText("$currencySymbol${String.format(Locale.US, "%.2f", totalIncome)}", 50f, y + 48f, titlePaint)

            titlePaint.color = Color.parseColor("#C62828") // Expense Red
            titlePaint.textSize = 11f
            canvas.drawText("Total Expense", 230f, y + 25f, titlePaint)
            titlePaint.textSize = 13f
            canvas.drawText("$currencySymbol${String.format(Locale.US, "%.2f", totalExpense)}", 230f, y + 48f, titlePaint)

            titlePaint.color = Color.parseColor("#1565C0") // Net Blue
            titlePaint.textSize = 11f
            canvas.drawText("Net Flow", 410f, y + 25f, titlePaint)
            titlePaint.textSize = 13f
            canvas.drawText("$currencySymbol${String.format(Locale.US, "%.2f", netBalance)}", 410f, y + 48f, titlePaint)

            y += 85f

            // --- TABLE HEADER ---
            paint.color = Color.parseColor("#2B364B")
            canvas.drawRect(30f, y, pageWidth - 30f, y + 28f, paint)

            titlePaint.color = Color.WHITE
            titlePaint.textSize = 10f
            titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText("Date", 40f, y + 18f, titlePaint)
            canvas.drawText("Category / Note", 140f, y + 18f, titlePaint)
            canvas.drawText("Type", 360f, y + 18f, titlePaint)
            canvas.drawText("Amount", 470f, y + 18f, titlePaint)

            y += 28f

            // --- TABLE ROWS ---
            val rowPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val dateSdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

            transactions.forEachIndexed { index, tx ->
                // Check page height limit
                if (y > pageHeight - 60f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    // Draw Table Header on new page
                    y = 40f
                    paint.color = Color.parseColor("#2B364B")
                    canvas.drawRect(30f, y, pageWidth - 30f, y + 28f, paint)

                    titlePaint.color = Color.WHITE
                    titlePaint.textSize = 10f
                    titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

                    canvas.drawText("Date", 40f, y + 18f, titlePaint)
                    canvas.drawText("Category / Note", 140f, y + 18f, titlePaint)
                    canvas.drawText("Type", 360f, y + 18f, titlePaint)
                    canvas.drawText("Amount", 470f, y + 18f, titlePaint)

                    y += 28f
                }

                // Alternate background color
                if (index % 2 == 0) {
                    paint.color = Color.parseColor("#F9FAFC")
                    canvas.drawRect(30f, y, pageWidth - 30f, y + 24f, paint)
                }

                rowPaint.color = Color.parseColor("#212121")
                val formattedDate = if (tx.dateString.isNotEmpty()) tx.dateString else dateSdf.format(Date(tx.timestamp))
                canvas.drawText(formattedDate, 40f, y + 16f, rowPaint)

                val displayCategory = tx.categoryName.ifBlank { "General" }
                val displayNote = if (tx.note.isNotBlank()) " (${tx.note.take(20)})" else ""
                val categoryText = (displayCategory + displayNote).take(32)
                canvas.drawText(categoryText, 140f, y + 16f, rowPaint)

                when (tx.type) {
                    TransactionType.INCOME -> {
                        rowPaint.color = Color.parseColor("#2E7D32")
                        canvas.drawText("INCOME", 360f, y + 16f, rowPaint)
                    }
                    TransactionType.EXPENSE -> {
                        rowPaint.color = Color.parseColor("#C62828")
                        canvas.drawText("EXPENSE", 360f, y + 16f, rowPaint)
                    }
                    TransactionType.TRANSFER -> {
                        rowPaint.color = Color.parseColor("#1565C0")
                        canvas.drawText("TRANSFER", 360f, y + 16f, rowPaint)
                    }
                }

                rowPaint.color = Color.parseColor("#212121")
                val amtStr = "$currencySymbol${String.format(Locale.US, "%.2f", tx.amount)}"
                canvas.drawText(amtStr, 470f, y + 16f, rowPaint)

                // Divider line
                paint.color = Color.parseColor("#E0E0E0")
                canvas.drawLine(30f, y + 24f, pageWidth - 30f, y + 24f, paint)

                y += 24f
            }

            // Footer
            rowPaint.textSize = 8.5f
            rowPaint.color = Color.GRAY
            canvas.drawText("SmartHishab - Page $pageNumber", 30f, pageHeight - 20f, rowPaint)

            pdfDocument.finishPage(page)

            // Save PDF File
            val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            val fileName = "SmartHishab_Statement_${System.currentTimeMillis()}.pdf"
            val file = File(exportDir, fileName)

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Toast.makeText(context, "PDF Exported: $fileName", Toast.LENGTH_LONG).show()

            // Open or Share PDF
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "SmartHishab Transaction History")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share or View PDF Statement"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
