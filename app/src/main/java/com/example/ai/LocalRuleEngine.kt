package com.example.ai

import java.util.Locale

object LocalRuleEngine {

    fun matchLocalIntent(rawInput: String): InterpretedCommand? {
        val text = rawInput.trim().lowercase(Locale.ROOT)
        if (text.isEmpty()) return null

        // Strip wake phrase if present at the beginning
        val cleaned = text
            .replace(Regex("^(hey|hei|hi|hello|এই|হে|হেই|হ্যালো)\\s+(ayra|aira|আইরা|আয়রা)[,\\s]*"), "")
            .replace(Regex("^(ayra|aira|আইরা|আয়রা)[,\\s]*"), "")
            .trim()

        val prompt = if (cleaned.isNotEmpty()) cleaned else text

        // Stop listening / sleep
        if (prompt.contains("stop listening") || prompt.contains("sleep for now") ||
            prompt.contains("go to sleep") || prompt.contains("stop session") ||
            prompt == "stop" || prompt == "sleep" ||
            prompt.contains("শোনা বন্ধ করো") || prompt.contains("বন্ধ করো") ||
            prompt.contains("shuna bondho") || prompt.contains("sleep dao")
        ) {
            return InterpretedCommand(
                actionType = AyraActionType.STOP_LISTENING,
                spokenResponse = "Stopping listening session. Wake me whenever you need."
            )
        }

        // Flashlight / Torch
        if (prompt.contains("flashlight") || prompt.contains("torch") ||
            prompt.contains("টর্চ") || prompt.contains("ফ্ল্যাশলাইট")
        ) {
            val turnOff = prompt.contains("off") || prompt.contains("বন্ধ") || prompt.contains("nibhao")
            val target = if (turnOff) "off" else "on"
            return InterpretedCommand(
                actionType = AyraActionType.TOGGLE_FLASHLIGHT,
                target = target,
                spokenResponse = if (turnOff) "Turning off flashlight." else "Turning on flashlight."
            )
        }

        // Camera
        if (prompt.contains("open camera") || prompt.contains("take photo") ||
            prompt.contains("ক্যামেরা খোলো") || prompt.contains("ছবি তোলো") ||
            prompt.contains("camera kholo")
        ) {
            return InterpretedCommand(
                actionType = AyraActionType.OPEN_CAMERA,
                target = "camera",
                spokenResponse = "Opening camera."
            )
        }

        // Calculator
        if (prompt.contains("calculator") || prompt.contains("ক্যালকুলেটর") || prompt.contains("hisab")) {
            return InterpretedCommand(
                actionType = AyraActionType.OPEN_CALCULATOR,
                target = "calculator",
                spokenResponse = "Opening calculator."
            )
        }

        // Calendar
        if (prompt.contains("calendar") || prompt.contains("ক্যালেন্ডার") || prompt.contains("dinpanjika")) {
            return InterpretedCommand(
                actionType = AyraActionType.OPEN_CALENDAR,
                target = "calendar",
                spokenResponse = "Opening calendar."
            )
        }

        // Clock / Alarm
        if (prompt.contains("alarm") || prompt.contains("clock") ||
            prompt.contains("ঘড়ি") || prompt.contains("অ্যালার্ম")
        ) {
            return InterpretedCommand(
                actionType = AyraActionType.OPEN_CLOCK_ALARM,
                target = "clock",
                spokenResponse = "Opening clock and alarms."
            )
        }

        // Battery query
        if (prompt.contains("battery") || prompt.contains("charge") ||
            prompt.contains("ব্যাটারি") || prompt.contains("চার্জ")
        ) {
            return InterpretedCommand(
                actionType = AyraActionType.QUERY_BATTERY,
                spokenResponse = "Checking current battery status."
            )
        }

        // Time and Date query
        if (prompt.contains("what time") || prompt.contains("current time") ||
            prompt.contains("what is the date") || prompt.contains("today's date") ||
            prompt.contains("কয়টা বাজে") || prompt.contains("সময় কত") ||
            prompt.contains("আজকে কি বার") || prompt.contains("ajker tarikh")
        ) {
            return InterpretedCommand(
                actionType = AyraActionType.QUERY_TIME_DATE,
                spokenResponse = "Checking current time and date."
            )
        }

        // Storage query
        if (prompt.contains("storage") || prompt.contains("memory") ||
            prompt.contains("স্টোরেজ") || prompt.contains("মেমোরি")
        ) {
            return InterpretedCommand(
                actionType = AyraActionType.QUERY_STORAGE,
                spokenResponse = "Checking available phone storage."
            )
        }

        // Screenshot
        if (prompt.contains("screenshot") || prompt.contains("স্ক্রিনশট") || prompt.contains("screen shot")) {
            return InterpretedCommand(
                actionType = AyraActionType.TAKE_SCREENSHOT,
                spokenResponse = "Taking screenshot."
            )
        }

        // Screen interactions (Accessibility)
        if (prompt.contains("scroll down") || prompt.contains("নিচে নামাও") || prompt.contains("niche scroll")) {
            return InterpretedCommand(
                actionType = AyraActionType.SCROLL_DOWN,
                spokenResponse = "Scrolling down."
            )
        }
        if (prompt.contains("scroll up") || prompt.contains("উপরে উঠাও") || prompt.contains("upore scroll")) {
            return InterpretedCommand(
                actionType = AyraActionType.SCROLL_UP,
                spokenResponse = "Scrolling up."
            )
        }
        if (prompt.contains("go back") || prompt == "back" || prompt.contains("পেছনে যাও") || prompt.contains("pechone jao")) {
            return InterpretedCommand(
                actionType = AyraActionType.GO_BACK,
                spokenResponse = "Going back."
            )
        }

        // Lock screen
        if (prompt.contains("lock screen") || prompt.contains("lock phone") ||
            prompt.contains("ফোন লক করো") || prompt.contains("lock koro")
        ) {
            return InterpretedCommand(
                actionType = AyraActionType.LOCK_SCREEN,
                spokenResponse = "Locking screen."
            )
        }

        // Settings
        if (prompt.contains("wifi setting") || prompt.contains("wi-fi setting") ||
            prompt.contains("bluetooth setting") || prompt.contains("settings") ||
            prompt.contains("সেটিংস")
        ) {
            val sub = when {
                prompt.contains("wifi") || prompt.contains("wi-fi") -> "wifi"
                prompt.contains("bluetooth") -> "bluetooth"
                prompt.contains("display") || prompt.contains("brightness") -> "display"
                prompt.contains("sound") || prompt.contains("volume") -> "sound"
                else -> "general"
            }
            return InterpretedCommand(
                actionType = AyraActionType.OPEN_SETTINGS,
                target = sub,
                spokenResponse = "Opening $sub settings."
            )
        }

        // Files / Documents
        if (prompt.contains("open file") || prompt.contains("my files") ||
            prompt.contains("find my downloaded pdf") || prompt.contains("downloads") ||
            prompt.contains("ফাইল")
        ) {
            return InterpretedCommand(
                actionType = AyraActionType.OPEN_FILES,
                spokenResponse = "Opening file manager."
            )
        }

        // Note creation: "save this note ...", "create note ...", "take a note ..."
        val noteMatch = Regex("^(save this note|save note|create note|take a note|নোট সেভ করো|নোট লেখো|note lekho)\\s*(.*)")
            .find(prompt)
        if (noteMatch != null) {
            val noteBody = noteMatch.groupValues[2].trim()
            val textToSave = if (noteBody.isNotEmpty()) noteBody else "Voice note from AYRA"
            return InterpretedCommand(
                actionType = AyraActionType.CREATE_NOTE,
                target = "Note",
                extraParam = textToSave,
                spokenResponse = "Saving note: $textToSave"
            )
        }

        // YouTube search: "search youtube for ...", "open youtube and search for ..."
        val ytSearchMatch = Regex("(search|play|find)?\\s*on youtube (for)?\\s*(.+)|search youtube for\\s*(.+)|youtube e search koro\\s*(.+)")
            .find(prompt)
        if (ytSearchMatch != null) {
            val query = (ytSearchMatch.groupValues.drop(1).firstOrNull { it.isNotBlank() } ?: "").trim()
            return InterpretedCommand(
                actionType = AyraActionType.SEARCH_YOUTUBE,
                target = query,
                spokenResponse = "Searching YouTube for $query"
            )
        }

        // Open app: "open youtube", "open whatsapp", "open chrome", "facebook kholo"
        val openAppMatch = Regex("^(open|launch|kholo|চালু করো|খোলো)\\s+([a-zA-Z0-9\\s]+)$")
            .find(prompt)
        if (openAppMatch != null) {
            val appName = openAppMatch.groupValues[2].trim()
            if (appName.isNotEmpty() && !appName.startsWith("note") && !appName.startsWith("file")) {
                return InterpretedCommand(
                    actionType = AyraActionType.OPEN_APP,
                    target = appName,
                    spokenResponse = "Opening $appName."
                )
            }
        }

        // Calls: "call my mother", "call mom", "call 017...", "ফোন করো"
        val callMatch = Regex("(call|phone|ডায়াল করো|ফোন করো)\\s+([a-zA-Z0-9\\s\\+\\-]+)")
            .find(prompt)
        if (callMatch != null) {
            val contactOrNum = callMatch.groupValues[2].trim()
            return InterpretedCommand(
                actionType = AyraActionType.CALL_PHONE,
                target = contactOrNum,
                spokenResponse = "Calling $contactOrNum",
                requiresConfirmation = true,
                confirmationPrompt = "Do you want to place a call to $contactOrNum?"
            )
        }

        // Web search: "search for ...", "google ...", "search ..."
        val webSearchMatch = Regex("^(search for|search|google|খুঁজো|অনুসন্ধান করো)\\s+(.+)$")
            .find(prompt)
        if (webSearchMatch != null) {
            val query = webSearchMatch.groupValues[2].trim()
            return InterpretedCommand(
                actionType = AyraActionType.SEARCH_WEB,
                target = query,
                spokenResponse = "Searching web for $query"
            )
        }

        return null
    }
}
