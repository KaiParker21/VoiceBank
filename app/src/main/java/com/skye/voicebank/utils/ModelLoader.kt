package com.skye.voicebank.utils

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class FRILLModel(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var isInputResized = false

    init {
        loadModel()
    }

    private fun loadModel() {
        val modelFile = loadModelFile()
        val options = Interpreter.Options()
        interpreter = Interpreter(modelFile, options)
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("frill.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun runInference(inputTensor: FloatArray): FloatArray {
        val interpreter = this.interpreter ?: throw IllegalStateException("Interpreter is not initialized")

        require(inputTensor.size == 16000) {
            "Expected input tensor of size 16000, but got ${inputTensor.size}"
        }

        if (!isInputResized) {
            interpreter.resizeInput(0, intArrayOf(1, 16000))
            interpreter.allocateTensors()
            isInputResized = true
        }

        val inputShape = interpreter.getInputTensor(0).shape()
        val outputShape = interpreter.getOutputTensor(0).shape()

        Log.d("FRILL", "Input Shape: ${inputShape.joinToString()}")
        Log.d("FRILL", "Output Shape: ${outputShape.joinToString()}")

        val inputBuffer = arrayOf(inputTensor)

        val outputBuffer = Array(1) { FloatArray(2048) }

        interpreter.run(inputBuffer, outputBuffer)

        Log.d("FRILL", "Generated Embeddings Count: ${outputBuffer.size}")
        Log.d("FRILL", "Selected Embedding (First 10 Values): ${outputBuffer[0].take(10)}...")

        return outputBuffer[0]
    }

    fun close() {
        interpreter?.close()
    }
}