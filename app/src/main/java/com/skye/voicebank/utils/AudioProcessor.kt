package com.skye.voicebank.utils

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlin.math.sqrt

class AudioProcessor {

    @RequiresPermission(
        Manifest.permission.RECORD_AUDIO
    )
    fun recordAndProcessAudio(frillModel: FRILLModel): FloatArray? {
        val sampleRate = 16000
        val audioBufferSize = sampleRate

        val audioBuffer = ShortArray(audioBufferSize)

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            audioBufferSize * 2
        )

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioRecord", "AudioRecord failed to initialize")
            return null
        }

        Log.d("AudioRecord", "Starting audio recording")
        try {
            recorder.startRecording()

            var totalRead = 0
            while (totalRead < audioBufferSize) {
                val readSize = recorder.read(audioBuffer, totalRead, audioBufferSize - totalRead)
                if (readSize > 0) {
                    totalRead += readSize
                }
            }

            recorder.stop()
            Log.d("AudioRecord", "Audio recording complete")

        } catch (e: Exception) {
            Log.e("AudioRecord", "Error recording audio", e)
            return null
        } finally {
            recorder.release()
        }

        val adjustedBuffer = if (audioBuffer.size < 16000) {
            audioBuffer + ShortArray(16000 - audioBuffer.size) { 0 }
        } else {
            audioBuffer.take(16000).toShortArray()
        }

        val floatArray = adjustedBuffer.map { it / 32768.0f }.toFloatArray()
        Log.d("FRILL", "Audio converted to float array: ${floatArray.take(10)}...")

        val embedding = frillModel.runInference(floatArray)
        Log.d("FRILL", "Generated Embedding: ${embedding.take(10)}...")

        return embedding
    }

    fun cosineSimilarity(vec1: List<Float>, vec2: List<Float>): Float {
        Log.d("FRILL", "Computing cosine similarity")

        if (vec1.isEmpty() || vec2.isEmpty() || vec1.size != vec2.size) {
            Log.w("FRILL", "Vectors are empty or different sizes, returning similarity 0.0")
            return 0.0f
        }

        val dotProduct = vec1.zip(vec2).sumOf { (a, b) -> (a * b).toDouble() }
        val magnitude1 = sqrt(vec1.sumOf { it.toDouble() * it.toDouble() })
        val magnitude2 = sqrt(vec2.sumOf { it.toDouble() * it.toDouble() })

        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            Log.w("FRILL", "Zero magnitude detected, returning similarity 0.0")
            return 0.0f
        }

        val similarity = (dotProduct / (magnitude1 * magnitude2)).toFloat()
        Log.d("FRILL", "Cosine Similarity Computed: $similarity")
        return similarity
    }

}