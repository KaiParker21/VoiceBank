package com.skye.voicebank.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skye.voicebank.utils.AudioProcessor
import com.skye.voicebank.utils.FRILLModel
import com.skye.voicebank.viewmodels.AuthViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onSignOut: () -> Unit,
    frillModel: FRILLModel
) {

    var registeredEmbeddings by remember { mutableStateOf<List<Float>?>(null) }
    val userId = authViewModel.getCurrentUserId()
    val audioProcessor = AudioProcessor()

    LaunchedEffect(userId) {
        if (userId != null) {
            authViewModel.fetchEmbeddings { fetchedEmbeddings ->
                registeredEmbeddings = fetchedEmbeddings
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Home Screen")
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = @androidx.annotation.RequiresPermission(
                android.Manifest.permission.RECORD_AUDIO
            ) {
                val testEmbedding = audioProcessor.recordAndProcessAudio(frillModel)?.toList()
                if (registeredEmbeddings != null && testEmbedding != null) {
                    val similarity = audioProcessor.cosineSimilarity(registeredEmbeddings!!, testEmbedding)
                    Log.d("FRILL", "Similarity Score: $similarity")
                    if (similarity > 0.8) {
                        Log.d("FRILL", "Voice Matched!")
                    } else {
                        Log.d("FRILL", "Voice Not Matched!")
                    }
                } else {
                    Log.d("FRILL", "No registered voice or test voice found!")
                }
            }
        ) {
            Text("Test")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                authViewModel.signOut()
                onSignOut()
            }
        ) {
            Text("Sign Out")
        }
    }
}