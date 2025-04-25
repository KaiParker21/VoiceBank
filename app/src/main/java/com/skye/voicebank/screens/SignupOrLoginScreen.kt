package com.skye.voicebank.screens

import android.Manifest
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.skye.voicebank.utils.VoiceToTextParser
import kotlinx.coroutines.delay
import java.util.Locale


@Composable
fun SignUpOrLoginScreen(
    navController: NavController,
    voiceToTextParser: VoiceToTextParser
) {
    val state by voiceToTextParser.state.collectAsState()
    val context = LocalContext.current
    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }
    var canRecord by remember { mutableStateOf(false) }

    var initialFlag by remember { mutableStateOf(false) }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> canRecord = isGranted }
    )

    LaunchedEffect(Unit) {
        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setSpeechRate(1.0f)
                textToSpeech?.speak(
                    "Please say Login or Sign Up",
                    TextToSpeech.QUEUE_FLUSH,
                    null, null
                )
            }
        }
        delay(2000)
        voiceToTextParser.startListening("en-US")
    }

    LaunchedEffect(state.spokenText, state.error) {
        if(initialFlag) {
            if (state.spokenText.lowercase(Locale.ROOT) == "login") {
                navController.navigate(Screens.LoginScreen.route)
            } else if (state.spokenText.lowercase(Locale.ROOT) == "sign up" || state.spokenText.lowercase(Locale.ROOT) == "signup") {
                navController.navigate(Screens.SignUpScreen.route)
            } else {
                delay(6000)
                textToSpeech?.speak(
                    "Please say Login or Sign Up",
                    TextToSpeech.QUEUE_FLUSH,
                    null, null
                )
                delay(2000)
                voiceToTextParser.startListening("en-US")
            }
        } else {
            initialFlag = true
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

    val textStyle = MaterialTheme.typography.headlineSmall.copy(
        color = Color.White,
        fontWeight = if (state.isSpeaking) FontWeight.Bold else FontWeight.Medium,
        fontSize = if (state.isSpeaking) 24.sp else 20.sp
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
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (canRecord) {

                    if (state.isSpeaking) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier
                                .size(250.dp)
                                .padding(16.dp)
                        )
                    }

                    AnimatedContent(targetState = state.isSpeaking) { isSpeaking ->
                        if (isSpeaking) {
                            Text(
                                text = "Listening...",
                                style = textStyle,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        } else {
                            Text(
                                text = state.spokenText.ifEmpty { "Say 'Login' or 'Sign Up'" },
                                style = textStyle,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
