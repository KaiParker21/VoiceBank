package com.skye.voicebank.screens

import android.annotation.SuppressLint
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.skye.voicebank.utils.AudioProcessor
import com.skye.voicebank.utils.FRILLModel
import com.skye.voicebank.utils.VoiceToTextParser
import com.skye.voicebank.viewmodels.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@SuppressLint(
    "MissingPermission"
)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    voiceToTextParser: VoiceToTextParser,
    frillModel: FRILLModel
) {

    BackHandler(enabled = true) { }

    val context = LocalContext.current
    val state by voiceToTextParser.state.collectAsState()
    val audioProcessor = AudioProcessor()
    val authResult by authViewModel.authResult.observeAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedField by remember { mutableStateOf("") }
    var isListeningForEmail by remember { mutableStateOf(false) }
    var isListeningForPassword by remember { mutableStateOf(false) }
    var isListeningForAuth by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isVoiceAuthenticated by remember { mutableStateOf(false) }

    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }
    var registeredEmbeddings by remember { mutableStateOf<List<Float>?>(null) }

    var confirmationText by remember { mutableStateOf("") }
    var isConfirmingEmail by remember { mutableStateOf(false) }
    var isConfirmingPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setSpeechRate(1.0f)
                textToSpeech?.speak("Please speak your email ID", TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                Toast.makeText(context, "Text to Speech not supported", Toast.LENGTH_SHORT).show()
            }
        }
        delay(3000)
        isListeningForEmail = true
        voiceToTextParser.startListening("en-IN")
    }

    LaunchedEffect(state.spokenText, state.error) {

        if (isListeningForEmail) {
            if (state.spokenText.isNotBlank()) {
                email = state.spokenText.lowercase(Locale.ROOT).replace(" ", "")
                isListeningForEmail = false
                textToSpeech?.speak("You spoke $email. Is this the correct email?", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(4500)
                isConfirmingEmail = true
                voiceToTextParser.startListening("en-IN")
            } else {
                delay(6000)
                textToSpeech?.speak("I didn't catch that. Please speak your email ID", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3500)
                voiceToTextParser.startListening("en-IN")
            }
        } else if (isConfirmingEmail) {
            if (state.spokenText.isNotBlank()) {
                confirmationText = state.spokenText.lowercase(Locale.ROOT)
                isConfirmingEmail = false

                if (confirmationText.contains("yes")) {
                    textToSpeech?.speak("Please speak your password", TextToSpeech.QUEUE_FLUSH, null, null)
                    delay(2000)
                    isListeningForPassword = true
                    voiceToTextParser.startListening("en-IN")
                } else {
                    textToSpeech?.speak("Please speak your email again.", TextToSpeech.QUEUE_FLUSH, null, null)
                    delay(2500)
                    isListeningForEmail = true
                    voiceToTextParser.startListening("en-IN")
                }
            } else {
                delay(6000)
                textToSpeech?.speak("Is this the correct email? Please say yes or no.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3500)
                voiceToTextParser.startListening("en-IN")
            }

        } else if (isListeningForPassword) {
            if (state.spokenText.isNotBlank()) {
                password = state.spokenText.trim().replace("\\s+".toRegex(), "")
                isListeningForPassword = false

                textToSpeech?.speak("You spoke $password. Is this correct?", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(6000)
                isConfirmingPassword = true
                voiceToTextParser.startListening("en-IN")
            } else {
                delay(6000)
                textToSpeech?.speak("I didn’t hear you. Please speak your password.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3500)
                voiceToTextParser.startListening("en-IN")
            }

        } else if (isConfirmingPassword) {
            if (state.spokenText.isNotBlank()) {
                confirmationText = state.spokenText.lowercase(Locale.ROOT)
                isConfirmingPassword = false

                if (confirmationText.contains("yes")) {
                    textToSpeech?.speak("Please speak now to verify your voice", TextToSpeech.QUEUE_FLUSH, null, null)
                    delay(1500)
                    isListeningForAuth = true
                } else {
                    textToSpeech?.speak("Please speak your password again.", TextToSpeech.QUEUE_FLUSH, null, null)
                    delay(1500)
                    isListeningForPassword = true
                    voiceToTextParser.startListening("en-IN")
                }
            } else {
                delay(6000)
                textToSpeech?.speak("Please confirm", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3000)
                voiceToTextParser.startListening("en-IN")
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(email, password, isListeningForAuth) {
        if (email.isNotBlank() && password.isNotBlank() && isListeningForAuth) {
            authViewModel.fetchUidByEmail(email) { uid ->
                if (uid != null) {
                    authViewModel.fetchEmbeddingsForLogin(uid) { fetchedEmbeddings ->
                        coroutineScope.launch {
                            registeredEmbeddings = fetchedEmbeddings
                            val testEmbedding = audioProcessor.recordAndProcessAudio(frillModel)?.toList()
                            if (registeredEmbeddings != null && testEmbedding != null) {
                                val similarity = audioProcessor.cosineSimilarity(
                                    registeredEmbeddings!!,
                                    testEmbedding
                                )
                                Log.d("FRILL", "Similarity Score: $similarity")
                                if (similarity > 0.6) {
                                    isVoiceAuthenticated = true
                                    textToSpeech?.speak(
                                        "Voice authentication successful, logging in", TextToSpeech.QUEUE_FLUSH, null, null
                                    )
                                    delay(1500)
                                    authViewModel.signIn(email, password)
                                    delay(1500)
                                    onLoginSuccess()
                                    isListeningForAuth = false
                                    isVoiceAuthenticated = false
                                    isListeningForEmail = false
                                    isListeningForPassword = false
                                    email = ""
                                    password = ""
                                    selectedField = ""
                                    registeredEmbeddings = null
                                    textToSpeech?.stop()
                                } else {
                                    textToSpeech?.speak(
                                        "Voice did not match. Try again.",
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        null
                                    )

                                    coroutineScope.launch {
                                        delay(2500)
                                        isListeningForAuth = false
                                        delay(500)

                                        textToSpeech?.speak(
                                            "Please speak now to verify your voice",
                                            TextToSpeech.QUEUE_FLUSH,
                                            null,
                                            null
                                        )

                                        delay(2000)
                                        isListeningForAuth = true
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Could not process voice authentication",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    coroutineScope.launch {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
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
                if (state.isSpeaking) {
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
                }

                FloatingActionButton(
                    onClick = {
                        if (state.isSpeaking) {
                            voiceToTextParser.stopListening()
                        } else {
                            voiceToTextParser.startListening("en-US")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(200.dp))

                Text(
                    text = "Login",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 32.dp),
                    fontWeight = FontWeight.W600
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .onFocusChanged { if (it.isFocused) selectedField = "email" },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .onFocusChanged { if (it.isFocused) selectedField = "password" },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Password else Icons.Filled.RemoveRedEye,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (!isVoiceAuthenticated) {
                            Toast.makeText(context, "Please complete voice authentication first", Toast.LENGTH_SHORT).show()
                        } else {
                            authViewModel.signIn(email, password)
                        }
                    },
                    enabled = isVoiceAuthenticated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Don't have an account? Sign up.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.W400,
                    modifier = Modifier
                        .clickable {
                            onNavigateToSignup()
                        }
                        .padding(8.dp)
                )

                if (state.isSpeaking) {
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

    LaunchedEffect(authResult) {
        authResult?.let { result ->
            result.onSuccess {
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }.onFailure {
                Toast.makeText(context, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

