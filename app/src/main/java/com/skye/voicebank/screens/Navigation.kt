package com.skye.voicebank.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.skye.voicebank.utils.FRILLModel
import com.skye.voicebank.utils.TextToSpeechHelper
import com.skye.voicebank.utils.VoiceToTextParser
import com.skye.voicebank.viewmodels.AuthViewModel

@Composable
fun NavigationGraph(
    navcontroller: NavHostController,
    authViewModel: AuthViewModel,
    frillModel: FRILLModel,
    voiceToTextParser: VoiceToTextParser,
    ttsHelper: TextToSpeechHelper
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
                onLoginSuccess = { navcontroller.navigate(Screens.CommandsScreen.route) },
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
                onSignUpSuccess = { navcontroller.navigate(Screens.CommandsScreen.route) },
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

        composable(Screens.CommandsScreen.route) {
            BankingCommandsScreen(
                navController = navcontroller,
                authViewModel = authViewModel,
                voiceToTextParser = voiceToTextParser,
                frillModel = frillModel,
                ttsHelper = ttsHelper
            )
        }
    }
}