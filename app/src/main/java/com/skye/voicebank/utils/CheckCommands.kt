package com.skye.voicebank.utils


import android.util.Log

fun checkCommands(
    spokenText: String,
    registeredEmbeddings: List<Float>?,
    frillModel: FRILLModel,
    audioProcessor: AudioProcessor,
    ttsHelper: TextToSpeechHelper,
    onVerified: () -> Unit,
    onRejected: () -> Unit
): String {

    if (registeredEmbeddings != null) {
        val testEmbedding = audioProcessor.recordAndProcessAudio(frillModel)?.toList()
        if (testEmbedding != null) {
            val similarity = audioProcessor.cosineSimilarity(registeredEmbeddings, testEmbedding)
            Log.d("FRILL", "Similarity: $similarity")


            if (similarity > 0.8f) {

                return when {
                    spokenText.contains("sign out", ignoreCase = true) -> {
                        onVerified()
                        "Signing out..."
                    }
                    spokenText.contains("balance", ignoreCase = true) -> {

                        "Checking your balance..."
                    }
                    spokenText.contains("transaction", ignoreCase = true) -> {

                        "Processing your transaction..."
                    }
                    else -> {

                        onRejected()
                        "Command not recognized. Please try again."
                    }
                }
            } else {

                ttsHelper.speak("Voice verification failed")
                return "Voice verification failed. Please speak again."
            }
        } else {

            ttsHelper.speak("Voice registration not found. Please try again later.")
            return "Voice registration not found. Please try again later."
        }
    } else {
        ttsHelper.speak("No voice embeddings found. Please register your voice first.")
        return "No voice embeddings found. Please register your voice first."
    }
}
