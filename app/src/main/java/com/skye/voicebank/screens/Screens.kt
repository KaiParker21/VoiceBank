package com.skye.voicebank.screens

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PermIdentity
import androidx.compose.ui.graphics.vector.ImageVector
import com.skye.voicebank.R

sealed class Screens(val route: String) {
    object LoginScreen : Screens("login screen")
    object SignUpScreen : Screens("sign up screen")
    object HomeScreen : Screens("home screen")
    object SplashScreen: Screens("splash screen")
    object SignupOrLoginScreen: Screens("signup or login screen")
    object CommandsScreen: Screens("commands screen")
//    object BankingCommandsScreen: Screens("banking commands screen")

    sealed class DrawerScreen(
        val dTitle: String, val dRoute: String, @SuppressLint(
            "SupportAnnotationUsage"
        ) @DrawableRes val icon: ImageVector
    ) : Screens(dRoute) {
        object BankingCommandsScreen: DrawerScreen("Commands Screen", "banking commands screen",
            Icons.Rounded.Home
        )
        object TransactionHistoryScreen: DrawerScreen("Transaction History", "transaction history screen",
            Icons.Rounded.History
        )
        object ProfileScreen: DrawerScreen("Profile", "profile screen", Icons.Rounded.PermIdentity)
    }

}

val screensInDrawer = listOf(
    Screens.DrawerScreen.BankingCommandsScreen,
    Screens.DrawerScreen.TransactionHistoryScreen,
    Screens.DrawerScreen.ProfileScreen,
)