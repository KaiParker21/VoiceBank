package com.skye.voicebank.screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.skye.voicebank.utils.AudioProcessor
import com.skye.voicebank.utils.FRILLModel
import com.skye.voicebank.utils.TextToSpeechHelper
import com.skye.voicebank.utils.VoiceToTextParser
import com.skye.voicebank.viewmodels.AuthViewModel

@SuppressLint(
    "MissingPermission"
)
@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun BankingCommandsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    voiceToTextParser: VoiceToTextParser,
    ttsHelper: TextToSpeechHelper,
    frillModel: FRILLModel
) {
    var canRecord by remember { mutableStateOf(false) }
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> canRecord = isGranted }
    )

    var registeredEmbeddings by remember { mutableStateOf<List<Float>?>(null) }
    val userId = authViewModel.getCurrentUserId()
    val audioProcessor = AudioProcessor()

    LaunchedEffect(userId) {
        if (userId != null) {
            authViewModel.fetchEmbeddings { fetchedEmbeddings ->
                registeredEmbeddings = fetchedEmbeddings
            }
        }
    }

    LaunchedEffect(Unit) {
        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val state by voiceToTextParser.state.collectAsState()
    var spokenCommandResult by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val animatedColor1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF4facfe),
        targetValue = Color(0xFF00f2fe),
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color1"
    )

    val animatedColor2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF00f2fe),
        targetValue = Color(0xFF4facfe),
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color2"
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .background(
                        if (state.isSpeaking)
                            Brush.verticalGradient(
                                colors = listOf(animatedColor1, animatedColor2)
                            )
                        else
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
                            )
                    )
            ) {
                TopAppBar(
                    title = { Text("Home", modifier = Modifier.padding(top = 4.dp)) },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (state.isSpeaking) {
                        voiceToTextParser.stopListening()
                    } else {
                        voiceToTextParser.startListening("en-IN")
                    }
                }
            ) {
                AnimatedContent(targetState = state.isSpeaking) { isSpeaking ->
                    if (isSpeaking) {
                        Icon(Icons.Rounded.Stop, contentDescription = "Stop Icon")
                    } else {
                        Icon(Icons.Rounded.Mic, contentDescription = "Mic Icon")
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(
                    if (state.isSpeaking)
                        Brush.verticalGradient(
                            colors = listOf(animatedColor1, animatedColor2)
                        )
                    else
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
                        )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Hi, Vidya ", fontSize = 30.sp)
                Text("Greetings of the day!", fontSize = 15.sp)
                Spacer(modifier = Modifier.height(70.dp))
                Text("Perform your Bank Transactions! 🎯", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    if (canRecord) {
                        AnimatedContent(targetState = state.isSpeaking) { isSpeaking ->
                            if (isSpeaking) {
                                Text("Listening...", modifier = Modifier.padding(16.dp))
                            } else {
                                val spokenText = state.spokenText
                                if (spokenText.isNotEmpty()) {
                                    val otp = remember { mutableStateOf("") }
                                    val awaitingOtp = remember { mutableStateOf(false) }

                                    LaunchedEffect(spokenText) {
                                        if (!awaitingOtp.value) {

                                            val generatedOtp = (1000..9999).random().toString()
                                            otp.value = generatedOtp
                                            awaitingOtp.value = true


                                            ttsHelper.speak("Your OTP is $generatedOtp. Please repeat it.") {

                                                voiceToTextParser.startListening("en-IN")
                                            }
                                        } else {

                                            val cleanSpokenOtp = spokenText.filter { it.isDigit() }
                                            if (cleanSpokenOtp == otp.value) {
                                                awaitingOtp.value = false
                                                ttsHelper.speak("OTP matched. Please speak to verify your voice.") {
                                                    val newEmbedding = audioProcessor.recordAndProcessAudio(frillModel)?.toList()
                                                    if (registeredEmbeddings != null && newEmbedding != null) {
                                                        val similarity = audioProcessor.cosineSimilarity(registeredEmbeddings!!, newEmbedding)
                                                        Log.d("FRILL", "Similarity Score: $similarity")
                                                        if (similarity > 0.3) {
                                                            Log.d("FRILL", "Voice Matched!")
                                                            checkCommands(spokenText, authViewModel, ttsHelper) { result ->
                                                                spokenCommandResult = result
                                                            }
                                                        } else {
                                                            Log.d("FRILL", "Voice Not Matched!")
                                                            ttsHelper.speak("Authentication failed. Voice not matched.")
                                                        }
                                                    } else {
                                                        Log.d("FRILL", "No embeddings available!")
                                                    }
                                                }
                                            } else {
                                                ttsHelper.speak("Incorrect OTP. Please try again.") {
                                                    awaitingOtp.value = false
                                                    voiceToTextParser.startListening("en-IN")
                                                }
                                            }
                                        }
                                    }


                                    Text(
                                        "Your OTP: ${otp.value}",
                                        fontSize = 24.sp,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                            .wrapContentWidth()
                                    )
                                }
                                spokenCommandResult?.let {
                                    Text(it, modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

fun checkCommands(
    spokenText: String,
    authViewModel: AuthViewModel,
    ttsHelper: TextToSpeechHelper,
    onResult: (String) -> Unit
) {
    val lowerText = spokenText.lowercase()

    Log.d("COMMANDS", "User said: \"$spokenText\"")

    val sendKeywords = listOf("send", "pay", "transfer", "debit")
    val creditKeywords = listOf("credit", "deposit", "add")
    val balanceKeywords = listOf("balance", "check", "show", "fetch")

    val containsSend = sendKeywords.any { lowerText.contains(it) }
    val containsCredit = creditKeywords.any { lowerText.contains(it) }
    val containsBalance = balanceKeywords.any { lowerText.contains(it) }

    when {
        containsBalance -> {
            val balance = authViewModel.getBalance()
            val result = "Current Balance: ₹$balance"
            Log.d("COMMANDS", "Balance command recognized. Responding with: $result")
            ttsHelper.speak(result)
            onResult(result)
        }

        containsSend -> {
            val amount = extractAmount(spokenText)
            val recipient = extractRecipient(spokenText)

            Log.d("COMMANDS", "Send command recognized. Extracted amount: $amount, recipient: $recipient")

            if (amount != null && recipient != null) {
                authViewModel.sendMoney(toEmail = recipient, amount = amount) { result ->
                    val message = if (result.isSuccess) {
                        Log.d("COMMANDS", "Transaction successful.")
                        "Successfully sent ₹$amount to $recipient"
                    } else {
                        Log.e("COMMANDS", "Transaction failed: ${result.exceptionOrNull()?.localizedMessage}")
                        "Failed to send money: ${result.exceptionOrNull()?.localizedMessage}"
                    }
                    ttsHelper.speak(message)
                    onResult(message)
                }
            } else {
                val msg = "Sorry, I couldn't understand the amount or recipient."
                Log.e("COMMANDS", "Parsing failed. Amount: $amount, Recipient: $recipient")
                ttsHelper.speak(msg)
                onResult(msg)
            }
        }

        containsCredit -> {
            val amount = extractAmount(spokenText) ?: 1000.0
            authViewModel.creditAmount(amount)
            val newBalance = authViewModel.getBalance()
            val result = "The Credit transaction of ₹$amount has been processed.\nCurrent Balance: ₹$newBalance"
            Log.d("COMMANDS", "Credit command. Added ₹$amount. New Balance: ₹$newBalance")
            ttsHelper.speak(result)
            onResult(result)
        }

        else -> {
            val msg = "Sorry, I didn't understand that command."
            Log.w("COMMANDS", "Unknown command: \"$spokenText\"")
            ttsHelper.speak(msg)
            onResult(msg)
        }
    }
}


fun extractAmount(text: String): Double? {
    val amountRegex = Regex("""(?:₹|rs\.?|rupees?)\s?(\d+(\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    val plainNumberRegex = Regex("""\b\d{1,6}(\.\d{1,2})?\b""")

    val match = amountRegex.find(text) ?: plainNumberRegex.find(text)
    return match?.groupValues?.firstOrNull()?.toDoubleOrNull()
}

fun extractRecipient(text: String): String? {
    val emailRegex = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""")
    val match = emailRegex.find(text)
    if (match != null) return match.value


    val cleaned = text.lowercase()
        .replace(" at ", "@")
        .replace(" dot ", ".")
        .replace(" underscore ", "_")
        .replace("\\s+".toRegex(), "")

    val cleanedMatch = emailRegex.find(cleaned)
    if (cleanedMatch != null) return cleanedMatch.value


    val words = text.split(" ")
    val toIndex = words.indexOfLast { it == "to" }
    return if (toIndex != -1 && toIndex + 1 < words.size) {
        words[toIndex + 1]
    } else {
        null
    }
}





