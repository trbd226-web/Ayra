package com.example.ai

enum class AyraActionType {
    OPEN_APP,
    SEARCH_YOUTUBE,
    SEARCH_WEB,
    CALL_PHONE,
    SEND_SMS,
    TOGGLE_FLASHLIGHT,
    OPEN_SETTINGS,
    OPEN_CAMERA,
    OPEN_CALCULATOR,
    OPEN_CALENDAR,
    OPEN_CLOCK_ALARM,
    OPEN_FILES,
    SCROLL_UP,
    SCROLL_DOWN,
    GO_BACK,
    TAKE_SCREENSHOT,
    LOCK_SCREEN,
    QUERY_BATTERY,
    QUERY_TIME_DATE,
    QUERY_STORAGE,
    CREATE_NOTE,
    STOP_LISTENING,
    GENERAL_QUERY,
    UNKNOWN
}

data class InterpretedCommand(
    val actionType: AyraActionType,
    val target: String = "",         // e.g. "YouTube", "Mother", "flashlight on"
    val extraParam: String = "",     // e.g. query "Minecraft survival", text message body
    val spokenResponse: String,      // Text AYRA will speak aloud to user
    val requiresConfirmation: Boolean = false,
    val confirmationPrompt: String = ""
)
