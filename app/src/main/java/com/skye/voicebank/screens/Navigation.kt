package com.skye.voicebank.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.skye.voicebank.BiometricPromptManager
import com.skye.voicebank.utils.FRILLModel
import com.skye.voicebank.utils.TextToSpeechHelper
import com.skye.voicebank.utils.VoiceToTextParser
import com.skye.voicebank.viewmodels.AuthViewModel

@RequiresApi(
    Build.VERSION_CODES.R
)
@Composable
fun NavigationGraph(
    navcontroller: NavHostController,
    authViewModel: AuthViewModel,
    frillModel: FRILLModel,
    voiceToTextParser: VoiceToTextParser,
    ttsHelper: TextToSpeechHelper,
    promptManager: BiometricPromptManager
) {
    NavHost(
        navController = navcontroller,
        startDestination = Screens.SplashScreen.route
    ) {
        composable (Screens.SignupOrLoginScreen.route) {
            SignUpOrLoginScreen(
                navController = navcontroller,
                voiceToTextParser = voiceToTextParser
            )
        }

        composable(Screens.SplashScreen.route) {
            SplashScreen(
                navcontroller = navcontroller,
                authViewModel = authViewModel
            )
        }
        composable(Screens.LoginScreen.route) {
            LoginScreen(
                onLoginSuccess = { navcontroller.navigate(Screens.BankingCommandsScreen.route) },
                onNavigateToSignup = { navcontroller.navigate(Screens.SignUpScreen.route) },
                authViewModel = authViewModel,
                voiceToTextParser = voiceToTextParser,
                frillModel = frillModel
            )
        }

        composable(Screens.SignUpScreen.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                frillModel = frillModel,
                voiceToTextParser = voiceToTextParser,
                onSignUpSuccess = { navcontroller.navigate(Screens.BankingCommandsScreen.route) },
                onNavigateToLogin = { navcontroller.navigate(Screens.LoginScreen.route) }
            )
        }

        composable(Screens.HomeScreen.route) {
            HomeScreen(
                authViewModel = authViewModel,
                onSignOut = {
                    navcontroller.navigate(
                        Screens.SplashScreen.route
                    )
                },
                frillModel = frillModel
            )
        }

//        composable(Screens.CommandsScreen.route) {
//            BankingCommandsScreen(
//                navController = navcontroller,
//                authViewModel = authViewModel,
//                voiceToTextParser = voiceToTextParser,
//                frillModel = frillModel,
//                ttsHelper = ttsHelper
//            )
//        }

        composable(Screens.BankingCommandsScreen.route) {
            BankingCommandsScreen(
                authViewModel = authViewModel,
                voiceToTextParser = voiceToTextParser,
                ttsHelper = ttsHelper,
                frillModel = frillModel,
                promptManager = promptManager
            )
        }
    }
}