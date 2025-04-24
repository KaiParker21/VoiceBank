package com.skye.voicebank.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class TextToSpeechHelper(context: Context) {

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("en", "US"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {

            }

            override fun onDone(utteranceId: String?) {
                Handler(Looper.getMainLooper()).post {
                    onDone?.invoke()
                }
            }

            override fun onError(utteranceId: String?) {
                Handler(Looper.getMainLooper()).post {
                    onDone?.invoke()
                }
            }
        })
    }


    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
