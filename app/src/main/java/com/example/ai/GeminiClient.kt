package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun testApiKeyConnection(apiKey: String, model: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be blank"))
        }

        val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val payload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "Respond with 'AYRA_OK' only."))
                    })
                })
            })
        }

        try {
            val request = Request.Builder()
                .url(requestUrl)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("Connection successful! Gemini model $model responded.")
            } else {
                val errorMsg = try {
                    val json = JSONObject(body)
                    json.optJSONObject("error")?.optString("message") ?: "HTTP $code"
                } catch (_: Exception) {
                    "HTTP $code: $body"
                }
                Result.failure(Exception("Gemini API Error: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network or connection error: ${e.localizedMessage ?: "Unknown"}"))
        }
    }

    suspend fun interpretAndAnswer(
        userInput: String,
        apiKey: String,
        model: String,
        installedAppsSummary: String = ""
    ): InterpretedCommand = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            // Local fallback if no key
            return@withContext LocalRuleEngine.matchLocalIntent(userInput)
                ?: InterpretedCommand(
                    actionType = AyraActionType.GENERAL_QUERY,
                    spokenResponse = "I understood '$userInput'. Please configure a Gemini API key in Settings to enable full AI reasoning and web answers."
                )
        }

        val systemInstruction = """
            You are AYRA, a smart Android voice assistant. The user can speak in English, Bengali, or Banglish (mixed Bengali and English in Latin script).
            Your job is to understand what the user wants to accomplish on their Android phone.
            Installed apps on user's device: [$installedAppsSummary].
            
            Return ONLY a valid JSON object matching this schema:
            {
               "action": "OPEN_APP" | "SEARCH_YOUTUBE" | "SEARCH_WEB" | "CALL_PHONE" | "SEND_SMS" | "TOGGLE_FLASHLIGHT" | "OPEN_SETTINGS" | "OPEN_CAMERA" | "OPEN_CALCULATOR" | "OPEN_CALENDAR" | "OPEN_CLOCK_ALARM" | "OPEN_FILES" | "SCROLL_UP" | "SCROLL_DOWN" | "GO_BACK" | "TAKE_SCREENSHOT" | "LOCK_SCREEN" | "QUERY_BATTERY" | "QUERY_TIME_DATE" | "QUERY_STORAGE" | "CREATE_NOTE" | "STOP_LISTENING" | "GENERAL_QUERY",
               "target": "target name, e.g. app name, contact name, settings section, flashlight on/off, or note title",
               "extraParam": "additional parameter, search query, note content, or message body",
               "spokenResponse": "A natural, friendly, concise answer spoken back to the user in the language they used (Bengali if they asked in Bengali/Banglish, English if asked in English)",
               "requiresConfirmation": true/false (true if deleting files or placing sensitive calls)
            }
            Never wrap in markdown or backticks, just raw json.
        """.trimIndent()

        val payload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", userInput))
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemInstruction))
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            })
        }

        val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        try {
            val request = Request.Builder()
                .url(requestUrl)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // Network / key failed, fallback to local rule
                val local = LocalRuleEngine.matchLocalIntent(userInput)
                return@withContext local ?: InterpretedCommand(
                    actionType = AyraActionType.GENERAL_QUERY,
                    spokenResponse = "I couldn't reach the AI service right now. Please check your internet or API key."
                )
            }

            val rootJson = JSONObject(body)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawJsonText = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseStructuredResponse(rawJsonText, userInput)
        } catch (e: Exception) {
            val local = LocalRuleEngine.matchLocalIntent(userInput)
            local ?: InterpretedCommand(
                actionType = AyraActionType.GENERAL_QUERY,
                spokenResponse = "Network error. Checked offline rules for your command."
            )
        }
    }

    private fun parseStructuredResponse(rawJson: String, originalPrompt: String): InterpretedCommand {
        try {
            val clean = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(clean)
            val actionStr = obj.optString("action", "GENERAL_QUERY").uppercase()
            val target = obj.optString("target", "")
            val extraParam = obj.optString("extraParam", "")
            val spokenResponse = obj.optString("spokenResponse", "Understood.")
            val requiresConfirmation = obj.optBoolean("requiresConfirmation", false)

            val actionType = try {
                AyraActionType.valueOf(actionStr)
            } catch (_: Exception) {
                AyraActionType.GENERAL_QUERY
            }

            return InterpretedCommand(
                actionType = actionType,
                target = target,
                extraParam = extraParam,
                spokenResponse = spokenResponse,
                requiresConfirmation = requiresConfirmation,
                confirmationPrompt = if (requiresConfirmation) "Are you sure you want to proceed with $target?" else ""
            )
        } catch (_: Exception) {
            val fallback = LocalRuleEngine.matchLocalIntent(originalPrompt)
            return fallback ?: InterpretedCommand(
                actionType = AyraActionType.GENERAL_QUERY,
                spokenResponse = rawJson.ifBlank { "I received your request." }
            )
        }
    }
}
