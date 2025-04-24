package com.skye.voicebank.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.skye.voicebank.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun SplashScreen(
    navcontroller: NavController,
    authViewModel: AuthViewModel
) {
    var startFadeOut by remember { mutableStateOf(false) }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.speak("Welcome to Voice Bank", TextToSpeech.QUEUE_FLUSH, null, null)

            }
        }
        delay(1500)
    }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startFadeOut) 0f else 1f,
        animationSpec = tween(durationMillis = 1000),
        finishedListener = {
            navcontroller.navigate(Screens.SignupOrLoginScreen.route) {
                popUpTo("splash") { inclusive = true }
            }
        }
    )

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("bank.json"))
    val progress by animateLottieCompositionAsState(composition)

    LaunchedEffect(true) {
        delay(3000)
        startFadeOut = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
                )
            )
            .alpha(alphaAnim),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to VoiceBank",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

