package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AyraPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ayra_settings_prefs", Context.MODE_PRIVATE)

    private val _geminiApiKey = MutableStateFlow(getSavedGeminiApiKey())
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow(prefs.getString(KEY_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _wakeWordEnabled = MutableStateFlow(prefs.getBoolean(KEY_WAKE_WORD, true))
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()

    private val _backgroundSessionEnabled = MutableStateFlow(prefs.getBoolean(KEY_BG_SESSION, true))
    val backgroundSessionEnabled: StateFlow<Boolean> = _backgroundSessionEnabled.asStateFlow()

    private val _sessionDurationMinutes = MutableStateFlow(prefs.getInt(KEY_SESSION_DURATION, 3))
    val sessionDurationMinutes: StateFlow<Int> = _sessionDurationMinutes.asStateFlow()

    private val _speechLanguage = MutableStateFlow(prefs.getString(KEY_SPEECH_LANG, "auto") ?: "auto")
    val speechLanguage: StateFlow<String> = _speechLanguage.asStateFlow()

    private val _speechRate = MutableStateFlow(prefs.getFloat(KEY_SPEECH_RATE, 1.0f))
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _voiceResponseEnabled = MutableStateFlow(prefs.getBoolean(KEY_VOICE_RESP_ENABLED, true))
    val voiceResponseEnabled: StateFlow<Boolean> = _voiceResponseEnabled.asStateFlow()

    private val _confirmSensitiveActions = MutableStateFlow(prefs.getBoolean(KEY_CONFIRM_ACTIONS, true))
    val confirmSensitiveActions: StateFlow<Boolean> = _confirmSensitiveActions.asStateFlow()

    private val _setupCompleted = MutableStateFlow(prefs.getBoolean(KEY_SETUP_COMPLETED, false))
    val setupCompleted: StateFlow<Boolean> = _setupCompleted.asStateFlow()

    fun getEffectiveApiKey(): String {
        val userKey = _geminiApiKey.value.trim()
        if (userKey.isNotEmpty()) return userKey
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun getSavedGeminiApiKey(): String {
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    fun setGeminiApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
        _geminiApiKey.value = key.trim()
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString(KEY_MODEL, model).apply()
        _selectedModel.value = model
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WAKE_WORD, enabled).apply()
        _wakeWordEnabled.value = enabled
    }

    fun setBackgroundSessionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BG_SESSION, enabled).apply()
        _backgroundSessionEnabled.value = enabled
    }

    fun setSessionDurationMinutes(minutes: Int) {
        val valid = if (minutes in listOf(1, 3, 5, 10)) minutes else 3
        prefs.edit().putInt(KEY_SESSION_DURATION, valid).apply()
        _sessionDurationMinutes.value = valid
    }

    fun setSpeechLanguage(lang: String) {
        prefs.edit().putString(KEY_SPEECH_LANG, lang).apply()
        _speechLanguage.value = lang
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
        _speechRate.value = rate
    }

    fun setVoiceResponseEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_RESP_ENABLED, enabled).apply()
        _voiceResponseEnabled.value = enabled
    }

    fun setConfirmSensitiveActions(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_ACTIONS, enabled).apply()
        _confirmSensitiveActions.value = enabled
    }

    fun setSetupCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETED, completed).apply()
        _setupCompleted.value = completed
    }

    companion object {
        private const val KEY_API_KEY = "key_gemini_api"
        private const val KEY_MODEL = "key_gemini_model"
        private const val KEY_WAKE_WORD = "key_wake_word"
        private const val KEY_BG_SESSION = "key_bg_session"
        private const val KEY_SESSION_DURATION = "key_session_duration"
        private const val KEY_SPEECH_LANG = "key_speech_lang"
        private const val KEY_SPEECH_RATE = "key_speech_rate"
        private const val KEY_VOICE_RESP_ENABLED = "key_voice_resp_enabled"
        private const val KEY_CONFIRM_ACTIONS = "key_confirm_actions"
        private const val KEY_SETUP_COMPLETED = "key_setup_completed"
    }
}
