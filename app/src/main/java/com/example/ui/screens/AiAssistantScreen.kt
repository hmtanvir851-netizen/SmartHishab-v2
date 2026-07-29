package com.example.ui.screens

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.WifiOff
import com.example.data.ai.ParsedAiResult
import com.example.data.localization.CurrencyManager
import com.example.data.localization.LanguageManager
import com.example.data.util.NetworkUtils
import com.example.ui.viewmodel.MainViewModel

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val parsedResult: ParsedAiResult? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val currentLang by viewModel.languageCode.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val isAiParsing by viewModel.isAiParsing.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            AiChatMessage(
                sender = "AI",
                text = if (currentLang == "bn")
                    "হ্যালো! আমি স্মার্ট-হিসাব এআই অ্যাসিস্ট্যান্ট।\nআপনি বলুন বা লিখুন: 'আজকে ৫০০ টাকার গ্রোসারি কিনলাম বিকাশ থেকে' বা আমাকে যেকোনো ফাইন্যান্সিয়াল প্রশ্ন করতে পারেন!"
                else
                    "Hello! I am SmartHishab AI Assistant.\nSpeak or write: 'Spent 500 on groceries with Bkash' or ask me any financial question!"
            )
        )
    }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (currentLang == "bn") "bn-BD" else "en-US")
        }
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) { isListening = false }
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    inputText = matches[0]
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer?.startListening(intent)
    }

    fun sendMessage() {
        val userQuery = inputText.trim()
        if (userQuery.isNotEmpty()) {
            chatMessages.add(AiChatMessage(sender = "USER", text = userQuery))
            inputText = ""

            viewModel.parseInputWithAi(userQuery)
        }
    }

    val pendingAiResult by viewModel.pendingAiResult.collectAsState()

    LaunchedEffect(pendingAiResult) {
        pendingAiResult?.let { result ->
            chatMessages.add(
                AiChatMessage(
                    sender = "AI",
                    text = result.aiAdvice.ifBlank { "আপনার দেয়া তথ্য চিহ্নিত করা হয়েছে:" },
                    parsedResult = result
                )
            )
        }
    }

    val isNetworkAvailable = remember(context) { NetworkUtils.isNetworkAvailable(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // AI Banner Header
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = LanguageManager.getString("ai_input_title", currentLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (currentLang == "bn") "ন্যাচারাল ল্যাঙ্গুয়েজ ট্র্যাকিং ও পরামর্শ" else "Natural Language Tracking & Financial Advice",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (!isNetworkAvailable) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Offline Warning",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLang == "bn")
                            "⚠️ অফলাইন মোড: কোনো ইন্টারনেট সংযোগ নেই! ক্লাউড জেমিনাই এআই এর পরিবর্তে অফলাইন লোকাল রুলস ইঞ্জিন ব্যবহার করা হবে।"
                        else
                            "⚠️ Offline Mode: No internet connection! Cloud Gemini AI unavailable; using local smart parsing rules.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Log List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.text,
                                fontSize = 14.sp,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (msg.parsedResult != null) {
                                val res = msg.parsedResult
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "অ্যামাউন্ট: ${CurrencyManager.format(res.amount, currencySymbol)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("টাইপ: ${res.type.name}", fontSize = 12.sp)
                                        Text("ক্যাটাগরি: ${res.categoryName}", fontSize = 12.sp)
                                        Text("অ্যাকাউন্ট: ${res.accountName}", fontSize = 12.sp)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = {
                                                viewModel.confirmPendingAiTransaction(
                                                    res.amount, res.type, res.accountName, res.targetAccountName, res.categoryName, res.note
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (currentLang == "bn") "হিসাবে সেভ করুন" else "Save to Ledger", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = if (currentLang == "bn") "বলুন বা লিখুন..." else "Speak or type...",
                        fontSize = 13.sp
                    )
                },
                shape = RoundedCornerShape(20.dp),
                trailingIcon = {
                    if (isAiParsing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        IconButton(onClick = { sendMessage() }) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = { startListening() },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isListening) Color.Red else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = Color.White
                )
            }
        }
    }
}
