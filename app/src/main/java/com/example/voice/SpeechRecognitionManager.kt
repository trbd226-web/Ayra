package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

class SpeechRecognitionManager(
    private val context: Context,
    private val onResultRecognized: (String) -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningIntentActive = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _rmsAmplitude = MutableStateFlow(0f)
    val rmsAmplitude: StateFlow<Float> = _rmsAmplitude.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    private fun ensureRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@SpeechRecognitionManager)
            }
        }
    }

    fun startListening(languagePreference: String = "auto") {
        if (!isAvailable()) {
            _errorMessage.value = "Speech recognition service is not available on this device."
            _voiceState.value = VoiceState.ERROR
            return
        }

        ensureRecognizer()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            when (languagePreference) {
                "bn-BD" -> putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                "bn-IN" -> putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN")
                "en-US" -> putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                else -> putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            }
        }

        try {
            isListeningIntentActive = true
            _errorMessage.value = null
            _partialTranscript.value = ""
            speechRecognizer?.startListening(intent)
            _voiceState.value = VoiceState.LISTENING
        } catch (e: Exception) {
            _errorMessage.value = "Failed to start speech recognizer: ${e.message}"
            _voiceState.value = VoiceState.ERROR
        }
    }

    fun stopListening() {
        isListeningIntentActive = false
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _voiceState.value = VoiceState.IDLE
        _rmsAmplitude.value = 0f
    }

    fun cancel() {
        isListeningIntentActive = false
        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
        _voiceState.value = VoiceState.IDLE
        _rmsAmplitude.value = 0f
    }

    fun release() {
        isListeningIntentActive = false
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        _voiceState.value = VoiceState.IDLE
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _voiceState.value = VoiceState.LISTENING
        _errorMessage.value = null
    }

    override fun onBeginningOfSpeech() {
        _voiceState.value = VoiceState.LISTENING
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Normalize rmsdB (-2 to 10 dB typically) to 0.0 .. 1.0 range
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _rmsAmplitude.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _voiceState.value = VoiceState.PROCESSING
        _rmsAmplitude.value = 0f
    }

    override fun onError(error: Int) {
        _rmsAmplitude.value = 0f
        isListeningIntentActive = false

        when (error) {
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                // Normal pauses in listening session
                _voiceState.value = VoiceState.IDLE
            }
            SpeechRecognizer.ERROR_AUDIO -> {
                _errorMessage.value = "Microphone recording error"
                _voiceState.value = VoiceState.ERROR
            }
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                _errorMessage.value = "Microphone permission required"
                _voiceState.value = VoiceState.ERROR
            }
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                _errorMessage.value = "Network error during speech recognition"
                _voiceState.value = VoiceState.ERROR
            }
            else -> {
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    override fun onResults(results: Bundle?) {
        _voiceState.value = VoiceState.PROCESSING
        _rmsAmplitude.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim() ?: ""
        if (text.isNotBlank()) {
            _partialTranscript.value = text
            onResultRecognized(text)
        } else {
            _voiceState.value = VoiceState.IDLE
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim() ?: ""
        if (text.isNotBlank()) {
            _partialTranscript.value = text
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
