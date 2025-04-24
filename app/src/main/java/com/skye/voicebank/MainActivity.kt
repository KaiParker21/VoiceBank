package com.skye.voicebank

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.skye.voicebank.screens.NavigationGraph
import com.skye.voicebank.ui.theme.VoiceBankTheme
import com.skye.voicebank.utils.FRILLModel
import com.skye.voicebank.utils.TextToSpeechHelper
import com.skye.voicebank.utils.VoiceToTextParser
import com.skye.voicebank.viewmodels.AuthRepository
import com.skye.voicebank.viewmodels.AuthViewModel
import com.skye.voicebank.viewmodels.AuthViewModelFactory
import com.skye.voicebank.viewmodels.FirebaseAuthRepository

class MainActivity :
    ComponentActivity() {

    val voiceToTextParser by lazy {
        VoiceToTextParser(application)
    }

    val ttsHelper by lazy {
        TextToSpeechHelper(applicationContext)
    }

    private lateinit var frillModel: FRILLModel

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        frillModel = FRILLModel(this)
        Log.d("FRILL", "Model loaded successfully!")

        val authRepository: AuthRepository = FirebaseAuthRepository(
            frillModel,
            context = applicationContext
        )
        val authViewModelFactory = AuthViewModelFactory(
            authRepository,
            context = applicationContext
        )
        val authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    Log.e("Permission", "Microphone permission denied!")
                }
            }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO)
        }

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            VoiceBankTheme {
                NavigationGraph(
                    navcontroller = navController,
                    authViewModel = authViewModel,
                    frillModel = frillModel,
                    voiceToTextParser = voiceToTextParser,
                    ttsHelper = ttsHelper
                )
            }
        }
    }
}