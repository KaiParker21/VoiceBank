package com.skye.voicebank.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.skye.voicebank.utils.AudioProcessor
import com.skye.voicebank.BiometricPromptManager
import com.skye.voicebank.BiometricPromptManager.BiometricResult
import com.skye.voicebank.utils.FRILLModel
import com.skye.voicebank.utils.TextToSpeechHelper
import com.skye.voicebank.utils.VoiceToTextParser
import com.skye.voicebank.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(
    Build.VERSION_CODES.R
)
@SuppressLint(
    "MissingPermission"
)
@Composable
fun BankingCommandsScreen(
    navController: NavController,
    voiceToTextParser: VoiceToTextParser,
    authViewModel: AuthViewModel,
    ttsHelper: TextToSpeechHelper,
    frillModel: FRILLModel,
    promptManager: BiometricPromptManager
) {

    val biometricResult by promptManager.promptResults.collectAsState(initial = null)

    val enrollLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            println("Result: $it")
        }
    )

    LaunchedEffect(biometricResult) {
        if (biometricResult is BiometricResult.AuthenticationNotSet) {
            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG)
            }
            enrollLauncher.launch(enrollIntent)
        }
    }

    var uiMessage by remember { mutableStateOf("Waiting for command...") }
    var uiSubMessage by remember { mutableStateOf("") }


    val userId = authViewModel.getCurrentUserId()
    var command by remember { mutableStateOf("") }
    var registeredEmbeddings by remember { mutableStateOf<List<Float>?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var otpValidationInProgress by remember { mutableStateOf(false) }
    var voiceAuthInProgress by remember { mutableStateOf(false) }
    var faceAuthInProgress by remember { mutableStateOf(false) }

    var currentOTP by remember { mutableStateOf<String?>(null) }

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

//    LaunchedEffect(state.spokenText) {
//        if(!state.isSpeaking && !isProcessing) {
//            voiceToTextParser.startContinuousListening("en-IN")
//        } else if (isValidCommand(state.spokenText) && !otpValidationInProgress) {
//            Log.d("VoiceToText", "Valid command: ${state.spokenText}")
//            command = state.spokenText
//            otpValidationInProgress = true
//            currentOTP = generateOTP()
//            ttsHelper.speak("Your OTP is: $currentOTP. Please say the OTP to confirm.")
//            delay(7000)
//            voiceToTextParser.startContinuousListening("en-IN")
//        } else if (otpValidationInProgress && state.spokenText.isNotEmpty() && !voiceAuthInProgress) {
//            if(state.spokenText == currentOTP) {
//                otpValidationInProgress = false
//                voiceAuthInProgress = true
//            } else {
//                val msg = "The OTP is $currentOTP. Please try again."
//                ttsHelper.speak(msg)
//                Log.w("OTP", "Incorrect OTP: ${state.spokenText}")
//                delay(5000)
//            }
//        }
//    }
//
//    LaunchedEffect(voiceAuthInProgress) {
//        if(voiceAuthInProgress) {
//            ttsHelper.speak("Please speak now to verify your voice")
//            delay(6000)
//            var currentEmbeddings = audioProcessor.recordAndProcessAudio(frillModel)?.toList()
//            if (currentEmbeddings != null) {
//                val similarity = audioProcessor.cosineSimilarity(registeredEmbeddings!!, currentEmbeddings)
//                if (similarity > 0.3) {
//                    faceAuthInProgress = true
//                    voiceAuthInProgress = false
//                    ttsHelper.speak("Voice Authentication Successful")
//                    delay(3000)
//                    voiceToTextParser.startContinuousListening("en-IN")
//                } else {
//                    delay(1000)
//                    ttsHelper.speak("Voice not recognized. Please try again.")
//                    delay(3000)
//                    voiceAuthInProgress = true
//                    voiceToTextParser.startContinuousListening("en-IN")
//                }
//            }
//        }
//    }

    LaunchedEffect(state.error) {
        if(!state.isSpeaking && !isProcessing) {
            voiceToTextParser.startContinuousListening("en-IN")
        }
    }

    LaunchedEffect(state.spokenText) {
        Log.d("VoiceToText", state.spokenText)
        voiceToTextParser.stopListening()
        if (isValidCommand(state.spokenText) && !otpValidationInProgress && !faceAuthInProgress) {
            command = state.spokenText
            uiMessage = "Command detected: $command"
            uiSubMessage = "Generating OTP..."
            otpValidationInProgress = true
            currentOTP = generateOTP()
            ttsHelper.speak("Your OTP is: $currentOTP. Please say the OTP to confirm.")
            delay(7000)
            voiceToTextParser.startContinuousListening("en-IN")
        } else if (otpValidationInProgress && state.spokenText.isNotEmpty()) {
            if (state.spokenText == currentOTP) {
                uiMessage = "OTP verified ✅"
                uiSubMessage = "Processing command: $command"
                otpValidationInProgress = false
                isProcessing = true
                faceAuthInProgress = true
                promptManager.showBiometricPrompt(
                    title = "Authenticate",
                    description = "Please authenticate to continue"
                )
            } else {
                uiMessage = "Incorrect OTP ❌"
                uiSubMessage = "Please try again"
                val msg = "The OTP you provided is incorrect. Please try again."
                ttsHelper.speak(msg)
                Log.w("OTP", "Incorrect OTP: \"$state.spokenText\"")
                delay(5000)
                if (!state.isSpeaking) {
                    voiceToTextParser.startContinuousListening("en-IN")
                }
            }
        } else {
            if (state.spokenText.isNotEmpty() && !faceAuthInProgress) {
                uiMessage = "Unknown command ❓"
                uiSubMessage = "Please try again"
                val msg = "Sorry, I didn't understand that command."
                ttsHelper.speak(msg)
                Log.w("COMMANDS", "Unknown command up $otpValidationInProgress: \"$state.spokenText\"")
                delay(4000)
                voiceToTextParser.startContinuousListening("en-IN")
            }
        }
        if (!state.isSpeaking) {
            voiceToTextParser.startContinuousListening("en-IN")
        }
    }

    LaunchedEffect(true) {
        while (true) {
            delay(5000)
            if (!state.isSpeaking && !isProcessing && !otpValidationInProgress) {
                voiceToTextParser.startContinuousListening("en-IN")
            }
        }
    }

    biometricResult?.let { result ->
        if (result == BiometricResult.AuthenticationSuccess) {
            faceAuthInProgress = false
            LaunchedEffect(result) {
                processSpokenText(
                    command,
                    authViewModel,
                    ttsHelper,
                    voiceToTextParser
                ) { msg ->
                    uiMessage = msg
                }
                isProcessing = false
                if (!state.isSpeaking) {
                    voiceToTextParser.startContinuousListening("en-IN")
                }
            }
        } else {
            Log.w("Biometric", "Authentication failed: $result")
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                screensInDrawer.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.dTitle) },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate(screen.dRoute)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = screen.dTitle
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Banking Assistant") },
                    navigationIcon = {
                        IconButton(onClick = {
                            // Open the Drawer
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF00f2fe)
                    ),
                    modifier = Modifier.shadow(elevation = 15.dp)
                )
            },
            floatingActionButton = {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(
                                80.dp
                            )
                            .graphicsLayer {
                                scaleX =
                                    pulse
                                scaleY =
                                    pulse
                                alpha =
                                    0.4f
                            }
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
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
                                    colors = listOf(
                                        animatedColor1,
                                        animatedColor2
                                    )
                                ),
                                shape = CircleShape
                            )
                            .size(
                                64.dp
                            )
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
        ) { paddingValues ->

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
                            colors = listOf(
                                animatedColor1,
                                animatedColor2
                            )
                        )
                    )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            paddingValues
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .size(
                                250.dp
                            )
                            .padding(
                                16.dp
                            )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = uiMessage,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiSubMessage.isNotEmpty()) {
                        Text(
                            text = uiSubMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                }
            }
        }
    }
}

suspend fun processSpokenText(
    spokenText: String,
    authViewModel: AuthViewModel,
    ttsHelper: TextToSpeechHelper,
    voiceToTextParser: VoiceToTextParser,
    onUiUpdate: (String) -> Unit
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
            onUiUpdate("Balance: ₹$balance")
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
                    onUiUpdate("Transaction successful ✅ Sent ₹$amount to $recipient")
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
    Log.d("VoiceToText", "isValidCommand: $spokenText")
    val lowerText = spokenText.lowercase()

    val sendKeywords = listOf("san", "send", "pay", "transfer", "debit")
    val creditKeywords = listOf("credit", "deposit", "add")
    val balanceKeywords = listOf("balance", "check", "show", "fetch")

    val containsSend = sendKeywords.any { lowerText.contains(it) }
    val containsCredit = creditKeywords.any { lowerText.contains(it) }
    val containsBalance = balanceKeywords.any { lowerText.contains(it) }

    val isValid = containsSend || containsCredit || containsBalance

    Log.d("VoiceToText", "returned $isValid")
    return isValid
}

@SuppressLint(
    "DefaultLocale"
)
fun generateOTP(): String {
    return String.format("%03d", Random.nextInt(10)) // Generates a 6-digit OTP
}



