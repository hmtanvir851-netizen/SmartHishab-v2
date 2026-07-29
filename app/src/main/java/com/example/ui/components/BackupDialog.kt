package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.backup.BackupHelper
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.languageCode.collectAsState()
    val isExporting by viewModel.isExportingBackup.collectAsState()
    val isRestoring by viewModel.isRestoringBackup.collectAsState()
    val backupStatus by viewModel.backupOperationStatus.collectAsState()
    val pendingRestore by viewModel.pendingRestoreResult.collectAsState()

    var showPasteInputDialog by remember { mutableStateOf(false) }
    var pastedContent by remember { mutableStateOf("") }
    var pendingExportPayload by remember { mutableStateOf<String?>(null) }

    // Launcher to save file to local/cloud storage
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { targetUri ->
            pendingExportPayload?.let { content ->
                val success = BackupHelper.writeContentToUri(context, targetUri, content)
                if (success) {
                    Toast.makeText(
                        context,
                        if (currentLang == "bn") "ব্যাকআপ ফাইল সফলভাবে সেভ হয়েছে!" else "Backup file saved successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        if (currentLang == "bn") "ব্যাকআপ ফাইল সেভ করতে ব্যর্থ।" else "Failed to save backup file.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Launcher to pick backup file for import
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            try {
                val content = BackupHelper.readContentFromUri(context, fileUri)
                viewModel.parseBackupForRestore(content)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    if (currentLang == "bn") "ফাইল পড়তে ব্যর্থ: ${e.message}" else "Failed to read file: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.clearBackupStatus()
            onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Cloud & Local Backup",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLang == "bn") "ম্যানুয়াল ক্লাউড ও লোকাল ব্যাকআপ" else "Manual Cloud & Local Backup",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = {
                    viewModel.clearBackupStatus()
                    onDismiss()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                // Info Header
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Secure Format",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == "bn")
                                "আপনার হিসাবের সকল তথ্য এনক্রিপ্টেড JSON ব্যাকআপ ফাইলের মাধ্যমে ক্লাউড ড্রাইভ বা লোকাল স্টোরেজে এক্সপোর্ট ও ইমপোর্ট করতে পারবেন।"
                            else
                                "Export or import your complete financial history safely to Google Drive, Cloud, or local storage using secure backup format.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Operation Status Message Banner
                backupStatus?.let { status ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = status,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Section 1: EXPORT BACKUP
                Text(
                    text = if (currentLang == "bn") "১. ব্যাকআপ এক্সপোর্ট (সঞ্চয় করুন)" else "1. Export Backup",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.exportBackup { payload ->
                            pendingExportPayload = payload
                            createDocumentLauncher.launch(BackupHelper.getDefaultBackupFileName())
                        }
                    },
                    enabled = !isExporting,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_backup_file_button")
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == "bn") "ব্যাকআপ ফাইল এক্সপোর্ট করুন (.json)" else "Export Backup File (.json)",
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.exportBackup { payload ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("SmartHishab Backup Payload", payload)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(
                                context,
                                if (currentLang == "bn") "ব্যাকআপ কোড ক্লিপবোর্ডে কপি হয়েছে!" else "Backup code copied to clipboard!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = !isExporting,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLang == "bn") "ব্যাকআপ কোড কপি করুন" else "Copy Backup Code to Clipboard",
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: IMPORT BACKUP
                Text(
                    text = if (currentLang == "bn") "২. ব্যাকআপ ইমপোর্ট (পুনরুদ্ধার করুন)" else "2. Import & Restore Backup",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { getContentLauncher.launch("*/*") },
                        enabled = !isRestoring,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_backup_file_button")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLang == "bn") "ফাইল বেছে নিন" else "Pick File",
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showPasteInputDialog = true },
                        enabled = !isRestoring,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLang == "bn") "কোড পেস্ট করুন" else "Paste Code",
                            fontSize = 12.sp
                        )
                    }
                }

                // Restore Preview Dialog / Confirmation Box if pendingRestore != null
                pendingRestore?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLang == "bn") "ব্যাকআপ ফাইল যাচাইকরণ সফল!" else "Backup Verified Successfully!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(result.timestamp))
                            Text(
                                text = "${if (currentLang == "bn") "তারিখ:" else "Date:"} $dateStr",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• ${result.accountsCount} ${if (currentLang == "bn") "টি অ্যাকাউন্ট" else "Accounts"}\n" +
                                        "• ${result.categoriesCount} ${if (currentLang == "bn") "টি ক্যাটাগরি" else "Categories"}\n" +
                                        "• ${result.transactionsCount} ${if (currentLang == "bn") "টি লেনদেন" else "Transactions"}\n" +
                                        "• ${result.debtsCount} ${if (currentLang == "bn") "টি দেনা-পাওনা" else "Debts"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (currentLang == "bn")
                                    "⚠️ রিস্টোর করলে বর্তমানের ডেটা পরিবর্তন হয়ে এই ব্যাকআপ ফাইলের ডেটা দিয়ে প্রতিস্থাপিত হবে।"
                                else
                                    "⚠️ Restoring will overwrite current app data with the accounts & transactions from this backup file.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { viewModel.clearBackupStatus() }) {
                                    Text(if (currentLang == "bn") "বাতিল" else "Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.confirmRestoreBackup() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("confirm_restore_backup_button")
                                ) {
                                    if (isRestoring) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text(if (currentLang == "bn") "রিস্টোর কনফার্ম করুন" else "Confirm Restore")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = {
                viewModel.clearBackupStatus()
                onDismiss()
            }) {
                Text(if (currentLang == "bn") "বন্ধ করুন" else "Close")
            }
        }
    )

    // Paste Backup Code Dialog
    if (showPasteInputDialog) {
        AlertDialog(
            onDismissRequest = { showPasteInputDialog = false },
            title = { Text(if (currentLang == "bn") "ব্যাকআপ কোড পেস্ট করুন" else "Paste Backup Code") },
            text = {
                OutlinedTextField(
                    value = pastedContent,
                    onValueChange = { pastedContent = it },
                    placeholder = { Text(if (currentLang == "bn") "এখানে ব্যাকআপ কোড বা JSON টেক্সট পেস্ট করুন..." else "Paste backup JSON or code here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("paste_backup_text_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.parseBackupForRestore(pastedContent)
                        showPasteInputDialog = false
                    }
                ) {
                    Text(if (currentLang == "bn") "যাচাই করুন" else "Verify Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteInputDialog = false }) {
                    Text(if (currentLang == "bn") "বাতিল" else "Cancel")
                }
            }
        )
    }
}
