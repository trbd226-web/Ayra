package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AyraApplication
import com.example.action.AyraActionExecutor
import com.example.action.ExecutionResult
import com.example.ai.AyraActionType
import com.example.ai.GeminiClient
import com.example.ai.InterpretedCommand
import com.example.ai.LocalRuleEngine
import com.example.data.model.CommandHistoryEntity
import com.example.data.model.NoteEntity
import com.example.service.AyraVoiceSessionService
import com.example.voice.SpeechRecognitionManager
import com.example.voice.TextToSpeechManager
import com.example.voice.VoiceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AyraViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AyraApplication
    val preferences = app.preferences
    val database = app.database

    val actionExecutor = AyraActionExecutor(application)
    val geminiClient = GeminiClient()
    val ttsManager = TextToSpeechManager(application)

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _sessionRemainingSeconds = MutableStateFlow(180)
    val sessionRemainingSeconds: StateFlow<Int> = _sessionRemainingSeconds.asStateFlow()

    private val _currentPrompt = MutableStateFlow("")
    val currentPrompt: StateFlow<String> = _currentPrompt.asStateFlow()

    private val _spokenResponseText = MutableStateFlow("I'm AYRA, your personal phone assistant. How can I assist you?")
    val spokenResponseText: StateFlow<String> = _spokenResponseText.asStateFlow()

    private val _lastExecutionResult = MutableStateFlow<ExecutionResult?>(null)
    val lastExecutionResult: StateFlow<ExecutionResult?> = _lastExecutionResult.asStateFlow()

    private val _pendingConfirmationCommand = MutableStateFlow<InterpretedCommand?>(null)
    val pendingConfirmationCommand: StateFlow<InterpretedCommand?> = _pendingConfirmationCommand.asStateFlow()

    private val _apiTestStatus = MutableStateFlow<String?>(null)
    val apiTestStatus: StateFlow<String?> = _apiTestStatus.asStateFlow()

    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi.asStateFlow()

    private var sessionCountdownJob: Job? = null

    val allNotes: StateFlow<List<NoteEntity>> = database.noteDao()
        .getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commandHistory: StateFlow<List<CommandHistoryEntity>> = database.commandDao()
        .getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val speechManager = SpeechRecognitionManager(application) { recognizedText ->
        handleRecognizedInput(recognizedText)
    }

    val voiceState: StateFlow<VoiceState> = speechManager.voiceState
    val rmsAmplitude: StateFlow<Float> = speechManager.rmsAmplitude
    val partialTranscript: StateFlow<String> = speechManager.partialTranscript
    val speechError: StateFlow<String?> = speechManager.errorMessage
    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking

    fun startListeningSession() {
        val durationMins = preferences.sessionDurationMinutes.value
        val totalSecs = durationMins * 60
        _sessionRemainingSeconds.value = totalSecs
        _isSessionActive.value = true

        if (preferences.backgroundSessionEnabled.value) {
            AyraVoiceSessionService.start(getApplication(), totalSecs)
        }

        startCountdownTimer()
        speechManager.startListening(preferences.speechLanguage.value)
    }

    fun triggerSingleListening() {
        ttsManager.stopSpeaking()
        if (!_isSessionActive.value) {
            startListeningSession()
        } else {
            resetSessionTimer()
            speechManager.startListening(preferences.speechLanguage.value)
        }
    }

    fun stopSession() {
        _isSessionActive.value = false
        sessionCountdownJob?.cancel()
        speechManager.stopListening()
        ttsManager.stopSpeaking()
        AyraVoiceSessionService.stop(getApplication())
    }

    fun resetSessionTimer() {
        val totalSecs = preferences.sessionDurationMinutes.value * 60
        _sessionRemainingSeconds.value = totalSecs
        if (_isSessionActive.value && preferences.backgroundSessionEnabled.value) {
            AyraVoiceSessionService.start(getApplication(), totalSecs)
        }
    }

    private fun startCountdownTimer() {
        sessionCountdownJob?.cancel()
        sessionCountdownJob = viewModelScope.launch {
            while (isActive && _isSessionActive.value && _sessionRemainingSeconds.value > 0) {
                delay(1000)
                _sessionRemainingSeconds.value -= 1
            }
            if (_sessionRemainingSeconds.value <= 0) {
                stopSession()
            }
        }
    }

    fun handleRecognizedInput(input: String) {
        val cleanInput = input.trim()
        if (cleanInput.isEmpty()) return

        _currentPrompt.value = cleanInput

        viewModelScope.launch {
            // First check local rule for instant processing
            val localMatch = LocalRuleEngine.matchLocalIntent(cleanInput)
            val command = if (localMatch != null) {
                localMatch
            } else {
                // Query Gemini
                val key = preferences.getEffectiveApiKey()
                val model = preferences.selectedModel.value
                val apps = actionExecutor.getInstalledAppNames().joinToString(", ")
                geminiClient.interpretAndAnswer(cleanInput, key, model, apps)
            }

            // Check if confirmation is required
            if (command.requiresConfirmation && preferences.confirmSensitiveActions.value) {
                _pendingConfirmationCommand.value = command
                speakResponse(command.confirmationPrompt.ifBlank { "Are you sure you want to proceed?" })
                return@launch
            }

            executeInterpretedCommand(command)
        }
    }

    fun confirmPendingAction() {
        val pending = _pendingConfirmationCommand.value ?: return
        _pendingConfirmationCommand.value = null
        viewModelScope.launch {
            executeInterpretedCommand(pending)
        }
    }

    fun cancelPendingAction() {
        _pendingConfirmationCommand.value = null
        speakResponse("Action cancelled.")
    }

    private suspend fun executeInterpretedCommand(command: InterpretedCommand) {
        if (command.actionType == AyraActionType.STOP_LISTENING) {
            speakResponse(command.spokenResponse)
            delay(1200)
            stopSession()
            return
        }

        val result = actionExecutor.executeCommand(command)
        _lastExecutionResult.value = result

        val speechToSpeak = result.spokenResponse.ifBlank { command.spokenResponse }
        speakResponse(speechToSpeak)

        // Reset countdown timer upon successful command!
        resetSessionTimer()

        // If session is still active and user did not stop, listen again after speech finishes
        if (_isSessionActive.value) {
            viewModelScope.launch {
                // wait for speech to finish before reopening mic
                delay(2500)
                if (_isSessionActive.value && voiceState.value == VoiceState.IDLE) {
                    speechManager.startListening(preferences.speechLanguage.value)
                }
            }
        }
    }

    fun speakResponse(text: String) {
        _spokenResponseText.value = text
        if (preferences.voiceResponseEnabled.value) {
            ttsManager.speak(
                text = text,
                languageCode = preferences.speechLanguage.value,
                rate = preferences.speechRate.value
            )
        }
    }

    fun repeatLastResponse() {
        ttsManager.repeatLast()
    }

    fun stopSpeaking() {
        ttsManager.stopSpeaking()
    }

    fun sendTextMessage(text: String) {
        handleRecognizedInput(text)
    }

    // Notes Management
    fun createNote(title: String, content: String, isReminder: Boolean = false, reminderTime: Long? = null) {
        viewModelScope.launch {
            database.noteDao().insertNote(
                NoteEntity(
                    title = title.ifBlank { "Quick Note" },
                    content = content,
                    isReminder = isReminder,
                    reminderTimeMillis = reminderTime
                )
            )
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            database.noteDao().deleteNote(note)
        }
    }

    // Command History Management
    fun toggleFavoriteCommand(id: Long, isFav: Boolean) {
        viewModelScope.launch {
            database.commandDao().toggleFavorite(id, isFav)
        }
    }

    fun clearCommandHistory() {
        viewModelScope.launch {
            database.commandDao().clearHistory()
        }
    }

    // API Testing
    fun testGeminiApi(apiKey: String, model: String) {
        viewModelScope.launch {
            _isTestingApi.value = true
            _apiTestStatus.value = "Testing connection to Gemini ($model)..."
            val result = geminiClient.testApiKeyConnection(apiKey, model)
            _isTestingApi.value = false
            if (result.isSuccess) {
                _apiTestStatus.value = result.getOrNull()
            } else {
                _apiTestStatus.value = "Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionCountdownJob?.cancel()
        speechManager.release()
        ttsManager.shutdown()
        AyraVoiceSessionService.stop(getApplication())
    }
}
