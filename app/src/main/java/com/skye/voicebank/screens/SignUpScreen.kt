package com.skye.voicebank.screens

import android.annotation.SuppressLint
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.skye.voicebank.data.UserDetails
import com.skye.voicebank.utils.AudioProcessor
import com.skye.voicebank.utils.FRILLModel
import com.skye.voicebank.utils.VoiceToTextParser
import com.skye.voicebank.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import com.airbnb.lottie.compose.*

@SuppressLint(
    "MissingPermission"
)
@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    frillModel: FRILLModel,
    voiceToTextParser: VoiceToTextParser,
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val state by voiceToTextParser.state.collectAsState()
    val audioProcessor = remember { AudioProcessor() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isListeningForName by remember { mutableStateOf(false) }
    var isListeningForEmail by remember { mutableStateOf(false) }
    var isListeningForPassword by remember { mutableStateOf(false) }
    var isListeningForAuth by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }
    var registeredEmbeddings by remember { mutableStateOf<List<Float>?>(null) }
    var confirmationText by remember { mutableStateOf("") }
    var isConfirmingName by remember { mutableStateOf(false) }
    var isConfirmingEmail by remember { mutableStateOf(false) }
    var isConfirmingPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.speak("Please speak your name", TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                Toast.makeText(context, "Text to Speech not supported", Toast.LENGTH_SHORT).show()
            }
        }
        delay(3000)
        isListeningForName = true
        voiceToTextParser.startListening("en-IN")
    }

    LaunchedEffect(state.spokenText, state.error) {

        if (isListeningForName) {
            if (state.spokenText.isNotBlank()) {
                name =
                    state.spokenText.lowercase(
                        Locale.ROOT
                    )
                        .replace(
                            " ",
                            ""
                        )
                isListeningForName =
                    false
                textToSpeech?.speak(
                    "You spoke $name. Is this the correct?",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
                delay(
                    4500
                )
                isConfirmingName =
                    true
                voiceToTextParser.startListening(
                    "en-IN"
                )
            } else {
                delay(
                    6000
                )
                textToSpeech?.speak(
                    "I didn't catch that. Please speak your name",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
                delay(
                    3500
                )
                voiceToTextParser.startListening(
                    "en-IN"
                )
            }
        } else if (isConfirmingName) {
            if (state.spokenText.isNotBlank()) {
                confirmationText =
                    state.spokenText.lowercase(
                        Locale.ROOT
                    )
                isConfirmingName =
                    false

                if (confirmationText.contains(
                        "yes"
                    )
                ) {
                    textToSpeech?.speak(
                        "Please speak your Email",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                    delay(
                        2000
                    )
                    isListeningForEmail =
                        true
                    voiceToTextParser.startListening(
                        "en-IN"
                    )
                } else {
                    textToSpeech?.speak(
                        "Please speak your name again.",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                    delay(
                        2500
                    )
                    isListeningForName =
                        true
                    voiceToTextParser.startListening(
                        "en-IN"
                    )
                }
            } else {
                delay(
                    6000
                )
                textToSpeech?.speak(
                    "Is this the correct name? Please say yes or no.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
                delay(
                    3500
                )
                voiceToTextParser.startListening(
                    "en-IN"
                )
            }
        } else if (isListeningForEmail) {
            if (state.spokenText.isNotBlank()) {
                email =
                    state.spokenText.lowercase(
                        Locale.ROOT
                    )
                        .replace(
                            " ",
                            ""
                        )
                isListeningForEmail =
                    false
                textToSpeech?.speak(
                    "You spoke $email. Is this the correct email?",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
                delay(
                    4500
                )
                isConfirmingEmail =
                    true
                voiceToTextParser.startListening(
                    "en-IN"
                )
            } else {
                delay(
                    6000
                )
                textToSpeech?.speak(
                    "I didn't catch that. Please speak your email ID",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
                delay(
                    3500
                )
                voiceToTextParser.startListening(
                    "en-IN"
                )
            }
        } else if (isConfirmingEmail) {
            if (state.spokenText.isNotBlank()) {
                confirmationText =
                    state.spokenText.lowercase(
                        Locale.ROOT
                    )
                isConfirmingEmail =
                    false

                if (confirmationText.contains(
                        "yes"
                    )
                ) {
                    textToSpeech?.speak(
                        "Please speak your password",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                    delay(
                        2000
                    )
                    isListeningForPassword =
                        true
                    voiceToTextParser.startListening(
                        "en-IN"
                    )
                } else {
                    textToSpeech?.speak(
                        "Please speak your email again.",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                    delay(
                        2500
                    )
                    isListeningForEmail =
                        true
                    voiceToTextParser.startListening(
                        "en-IN"
                    )
                }
            } else {
                delay(
                    6000
                )
                textToSpeech?.speak(
                    "Is this the correct email? Please say yes or no.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
                delay(
                    3500
                )
                voiceToTextParser.startListening(
                    "en-IN"
                )
            }
        } else if (isListeningForPassword) {
            if (state.spokenText.isNotBlank()) {
                password =
                    state.spokenText.trim()
                        .replace(
                            "\\s+".toRegex(),
                            ""
                        )
                isListeningForPassword =
                    false

                textToSpeech?.speak(
                    "You spoke $password. Is this correct?",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
                delay(
                    6000
                )
                isConfirmingPassword =
                    true
                voiceToTextParser.startListening(
                    "en-IN"
                )
            } else {
                delay(
                    6000
                )
                textToSpeech?.speak(
                    "I didn’t hear you. Please speak your password.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
                delay(
                    3500
                )
                voiceToTextParser.startListening(
                    "en-IN"
                )
            }
        } else if (isConfirmingPassword) {
            if (state.spokenText.isNotBlank()) {
                confirmationText =
                    state.spokenText.lowercase(
                        Locale.ROOT
                    )
                isConfirmingPassword =
                    false

                if (confirmationText.contains(
                        "yes"
                    )
                ) {
                    textToSpeech?.speak("Now recording your voice. Please speak for 5 seconds.", TextToSpeech.QUEUE_FLUSH, null, null)
                    delay(7000)
                    registeredEmbeddings = audioProcessor.recordAndProcessAudio(frillModel)?.toList()
                    delay(3000)
                    if (registeredEmbeddings != null) {
                        val userDetails = UserDetails(name = name)
                        authViewModel.signUp(email.trim(), password.trim(), userDetails)
                        textToSpeech?.speak("Signup successful, please log in", TextToSpeech.QUEUE_FLUSH, null, null)
                        Toast.makeText(context, "Signup complete! Redirecting...", Toast.LENGTH_SHORT).show()
                        delay(3000)
                        onSignUpSuccess()
                    } else {
                        Toast.makeText(context, "Voice registration failed", Toast.LENGTH_SHORT).show()
                    }
                    isListeningForAuth =
                        true
                } else {
                    textToSpeech?.speak(
                        "Please speak your password again.",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                    delay(
                        1500
                    )
                    isListeningForPassword =
                        true
                    voiceToTextParser.startListening(
                        "en-IN"
                    )
                }
            } else {
                delay(
                    6000
                )
                textToSpeech?.speak(
                    "Please confirm. Say yes or no",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
                delay(
                    3000
                )
                voiceToTextParser.startListening(
                    "en-IN"
                )
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
    ) { padding ->

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
                    .padding(
                        padding
                    )
                    .padding(
                        16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Voice Sign Up",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 32.dp),
                    fontWeight = FontWeight.W600
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name =
                            it
                    },
                    label = {
                        Text(
                            "Name"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            8.dp
                        )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email =
                            it
                    },
                    label = {
                        Text(
                            "Email"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            8.dp
                        )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password =
                            it
                    },
                    label = {
                        Text(
                            "Password"
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                passwordVisible =
                                    !passwordVisible
                            }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Add else Icons.Filled.AddCircle,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            8.dp
                        )
                )

                Spacer(
                    modifier = Modifier.height(
                        16.dp
                    )
                )

                Text(
                    text = "Already registered? Log in here.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.W400,
                    modifier = Modifier
                        .clickable {
                            onNavigateToLogin()
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
}




