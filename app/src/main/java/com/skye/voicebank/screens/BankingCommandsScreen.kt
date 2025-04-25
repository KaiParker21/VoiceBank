package com.skye.voicebank.screens

import android.annotation.SuppressLint
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.skye.voicebank.utils.AudioProcessor
import com.skye.voicebank.utils.TextToSpeechHelper
import com.skye.voicebank.utils.VoiceToTextParser
import com.skye.voicebank.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun BankingCommandsScreen(
    voiceToTextParser: VoiceToTextParser,
    authViewModel: AuthViewModel,
    ttsHelper: TextToSpeechHelper
) {

    val userId = authViewModel.getCurrentUserId()
    var command by remember { mutableStateOf("") }

    var registeredEmbeddings by remember { mutableStateOf<List<Float>?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    var currentOTP by remember { mutableStateOf<String?>(null) }
    var otpValidationInProgress by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val state by voiceToTextParser.state.collectAsState()
    val audioProcessor = AudioProcessor()

    LaunchedEffect(userId) {
        if (userId != null) {
            authViewModel.fetchEmbeddings { fetchedEmbeddings ->
                Log.d("VoiceToText", "Fetched embeddings: $fetchedEmbeddings")
                registeredEmbeddings = fetchedEmbeddings
            }
        } else {
            Log.d("VoiceToText", "User ID is null")
        }
    }

    LaunchedEffect(Unit) {
        voiceToTextParser.startContinuousListening("en-IN")
    }

    LaunchedEffect(state.error) {
        if(!state.isSpeaking && !isProcessing) {
            voiceToTextParser.startContinuousListening("en-IN")
        }
    }

    LaunchedEffect(state.spokenText) {
        Log.d("VoiceToText", state.spokenText)
        voiceToTextParser.stopListening()

        if (isValidCommand(state.spokenText) && !otpValidationInProgress) {
            command = state.spokenText
            otpValidationInProgress = true
            currentOTP = generateOTP()
            ttsHelper.speak("Your OTP is: $currentOTP. Please say the OTP to confirm.")
            delay(7000)
            voiceToTextParser.startContinuousListening("en-IN")
        } else if (otpValidationInProgress && state.spokenText.isNotEmpty()) {
            if (state.spokenText == currentOTP) {
                otpValidationInProgress = false
                isProcessing = true
                processSpokenText(
                    command,
                    authViewModel,
                    ttsHelper,
                    voiceToTextParser
                )
                isProcessing = false
                if (!state.isSpeaking) {
                    voiceToTextParser.startContinuousListening("en-IN")
                }
            } else {
                val msg = "The OTP you provided is incorrect. Please try again."
                ttsHelper.speak(msg)
                Log.w("OTP", "Incorrect OTP: \"$state.spokenText\"")
                otpValidationInProgress = false
                delay(5000)
                if (!state.isSpeaking) {
                    voiceToTextParser.startContinuousListening("en-IN")
                }
            }
        } else {
            if (state.spokenText.isNotEmpty()) {
                val msg = "Sorry, I didn't understand that command."
                ttsHelper.speak(msg)
                Log.w("COMMANDS", "Unknown command up $otpValidationInProgress: \"$state.spokenText\"")
                delay(5000)
            }
        }
        if (!state.isSpeaking) {
            voiceToTextParser.startContinuousListening("en-IN")
        }
    }

    LaunchedEffect(true) {
        while (true) {
            delay(5000)
            if (!state.isSpeaking) {
                voiceToTextParser.startContinuousListening("en-IN")
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

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
        floatingActionButton = {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                            alpha = 0.4f
                        }
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                )

                FloatingActionButton(
                    onClick = {
                        if (state.isSpeaking) {
                            voiceToTextParser.stopListening()
                        } else {
                            voiceToTextParser.startContinuousListening("en-US")
                        }
                    },
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(animatedColor1, animatedColor2)
                            ),
                            shape = CircleShape
                        )
                        .size(64.dp)
                ) {
                    AnimatedContent(targetState = state.isSpeaking) { isSpeaking ->
                        Icon(
                            imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = "Mic Icon",
                            tint = Color.White
                        )
                    }
                }

            }
        }
    ) { paddingValues->

        val composition by rememberLottieComposition(LottieCompositionSpec.Asset("mic.json"))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(animatedColor1, animatedColor2)
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .size(250.dp)
                        .padding(16.dp)
                )
            }
        }
    }
}

suspend fun processSpokenText(
    spokenText: String,
    authViewModel: AuthViewModel,
    ttsHelper: TextToSpeechHelper,
    voiceToTextParser: VoiceToTextParser
) {

    val lowerText = spokenText.lowercase()

    Log.d("COMMANDS", "User said: \"$spokenText\"")

    val sendKeywords = listOf("san", "send", "pay", "transfer", "debit")
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
            delay(5000)
        }

        containsSend -> {
            val amount = extractAmount(spokenText)
            val recipient = extractRecipient(spokenText)

            Log.d(
                "COMMANDS",
                "Send command recognized. Extracted amount: $amount, recipient: $recipient"
            )

            if (amount != null && recipient != null) {
                authViewModel.sendMoney(toEmail = recipient, amount = amount) { result ->
                    val message = if (result.isSuccess) {
                        Log.d("COMMANDS", "Transaction successful.")
                        "Successfully sent ₹$amount to $recipient"
                    } else {
                        Log.e(
                            "COMMANDS",
                            "Transaction failed: ${result.exceptionOrNull()?.localizedMessage}"
                        )
                        "Failed to send money: ${result.exceptionOrNull()?.localizedMessage}"
                    }
                    ttsHelper.speak(message)
                }
            } else {
                val msg = "Sorry, I couldn't understand the amount or recipient."
                Log.e("COMMANDS", "Parsing failed. Amount: $amount, Recipient: $recipient")
                ttsHelper.speak(msg)
            }
            delay(5000)
        }

        containsCredit -> {
            val amount = extractAmount(spokenText) ?: 1000.0
            authViewModel.creditAmount(amount)
            val newBalance = authViewModel.getBalance()
            val result = "The Credit transaction of ₹$amount has been processed.\nCurrent Balance: ₹$newBalance"
            Log.d("COMMANDS", "Credit command. Added ₹$amount. New Balance: ₹$newBalance")
            ttsHelper.speak(result)
            delay(5000)
        }

        else -> {
            if (spokenText.isNotEmpty()) {
                val msg = "Sorry, I didn't understand that command."
                ttsHelper.speak(msg)
                Log.w("COMMANDS", "Unknown command down: \"$spokenText\"")
                delay(5000)
            }
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

fun isValidCommand(spokenText: String): Boolean {
    val lowerText = spokenText.lowercase()

    val sendKeywords = listOf("san", "send", "pay", "transfer", "debit")
    val creditKeywords = listOf("credit", "deposit", "add")
    val balanceKeywords = listOf("balance", "check", "show", "fetch")

    val containsSend = sendKeywords.any { lowerText.contains(it) }
    val containsCredit = creditKeywords.any { lowerText.contains(it) }
    val containsBalance = balanceKeywords.any { lowerText.contains(it) }

    return containsSend || containsCredit || containsBalance
}

@SuppressLint(
    "DefaultLocale"
)
fun generateOTP(): String {
    return String.format("%03d", Random.nextInt(10)) // Generates a 6-digit OTP
}



