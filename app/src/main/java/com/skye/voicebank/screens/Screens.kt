package com.skye.voicebank.screens

sealed class Screens(val route: String) {
    object LoginScreen : Screens("login screen")
    object SignUpScreen : Screens("sign up screen")
    object HomeScreen : Screens("home screen")
    object SplashScreen: Screens("splash screen")
    object SignupOrLoginScreen: Screens("signup or login screen")
    object CommandsScreen: Screens("commands screen")
}