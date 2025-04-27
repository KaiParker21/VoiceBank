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
    private val utteranceCallbacks = mutableMapOf<String, () -> Unit>()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("en", "US"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                } else {
                    tts?.setSpeechRate(1.0f)
                }
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                utteranceId?.let {
                    Handler(Looper.getMainLooper()).post {
                        utteranceCallbacks.remove(it)?.invoke()
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                utteranceId?.let {
                    Handler(Looper.getMainLooper()).post {
                        utteranceCallbacks.remove(it)?.invoke()
                    }
                }
            }
        })
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        val utteranceId = UUID.randomUUID().toString()
        onDone?.let { utteranceCallbacks[utteranceId] = it }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }


    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
