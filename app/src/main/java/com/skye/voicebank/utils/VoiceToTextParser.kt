package com.skye.voicebank.utils

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VoiceToTextParser(
    private val app: Application
) : RecognitionListener {

    private val _state = MutableStateFlow(VoiceToTextParserState())
    val state = _state.asStateFlow()

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(app)

    private var listeningJob: Job? = null

    private val TAG = "VoiceToTextParser"

    fun startListening(languageCode: String) {
        Log.d(TAG, "Starting to listen for speech...")

        _state.update { VoiceToTextParserState() }

        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            Log.e(TAG, "Speech recognition is not available.")
            _state.update {
                it.copy(
                    error = "Recognition is not available."
                )
            }
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        }

        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)

        _state.update {
            it.copy(
                isSpeaking = true
            )
        }

        Log.d(TAG, "Listening started, waiting for speech input...")

        listeningJob?.cancel()
    }

    fun stopListening() {
        Log.d(TAG, "Stopping listening...")
        listeningJob?.cancel()
        recognizer.stopListening()
        _state.update {
            it.copy(isSpeaking = false)
        }
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        Log.d(TAG, "Ready for speech input.")
        _state.update {
            it.copy(
                error = null
            )
        }
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Speech has started.")
    }

    override fun onRmsChanged(p0: Float) {

    }

    override fun onBufferReceived(p0: ByteArray?) {
        Log.d(TAG, "Buffer received.")
    }

    override fun onEndOfSpeech() {
        Log.d(TAG, "Speech has ended.")
    }

    override fun onError(p0: Int) {
        if (p0 == SpeechRecognizer.ERROR_CLIENT) return

        Log.e(TAG, "Error occurred: $p0")
        listeningJob?.cancel()


        _state.update {
            it.copy(
                isSpeaking = false,
                error = "I'm sorry, I didn't hear that. Please speak again."
            )
        }
    }

    override fun onResults(p0: Bundle?) {
        listeningJob?.cancel()

        val text = p0
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?: ""

        Log.d(TAG, "Speech recognition results: $text")

        _state.update {
            it.copy(
                isSpeaking = false,
                spokenText = text
            )
        }
    }

    override fun onPartialResults(p0: Bundle?) {
        Log.d(TAG, "Partial results received.")
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        Log.d(TAG, "Event received: $p0")
    }

    fun resetSpokenText() {
        Log.d(TAG, "Resetting spoken text.")
        _state.value = VoiceToTextParserState()
    }
}

data class VoiceToTextParserState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null
)
