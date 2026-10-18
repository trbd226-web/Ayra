package com.example.action

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.ai.AyraActionType
import com.example.ai.InterpretedCommand
import com.example.data.db.AyraDatabase
import com.example.data.model.CommandHistoryEntity
import com.example.data.model.NoteEntity
import com.example.receiver.AyraDeviceAdminReceiver
import com.example.service.AyraAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExecutionResult(
    val isSuccess: Boolean,
    val feedbackMessage: String,
    val spokenResponse: String
)

class AyraActionExecutor(private val context: Context) {

    private val database = AyraDatabase.getDatabase(context)

    suspend fun executeCommand(command: InterpretedCommand): ExecutionResult = withContext(Dispatchers.IO) {
        val result = try {
            when (command.actionType) {
                AyraActionType.OPEN_APP -> executeOpenApp(command.target)
                AyraActionType.SEARCH_YOUTUBE -> executeSearchYouTube(command.target)
                AyraActionType.SEARCH_WEB -> executeSearchWeb(command.target)
                AyraActionType.CALL_PHONE -> executeCallPhone(command.target)
                AyraActionType.SEND_SMS -> executeSendSms(command.target, command.extraParam)
                AyraActionType.TOGGLE_FLASHLIGHT -> executeToggleFlashlight(command.target == "on")
                AyraActionType.OPEN_SETTINGS -> executeOpenSettings(command.target)
                AyraActionType.OPEN_CAMERA -> executeOpenCamera()
                AyraActionType.OPEN_CALCULATOR -> executeOpenCalculator()
                AyraActionType.OPEN_CALENDAR -> executeOpenCalendar()
                AyraActionType.OPEN_CLOCK_ALARM -> executeOpenClock()
                AyraActionType.OPEN_FILES -> executeOpenFiles()
                AyraActionType.SCROLL_DOWN -> executeAccessibilityAction("SCROLL_DOWN")
                AyraActionType.SCROLL_UP -> executeAccessibilityAction("SCROLL_UP")
                AyraActionType.GO_BACK -> executeAccessibilityAction("GO_BACK")
                AyraActionType.TAKE_SCREENSHOT -> executeAccessibilityAction("TAKE_SCREENSHOT")
                AyraActionType.LOCK_SCREEN -> executeLockScreen()
                AyraActionType.QUERY_BATTERY -> executeQueryBattery()
                AyraActionType.QUERY_TIME_DATE -> executeQueryTimeDate()
                AyraActionType.QUERY_STORAGE -> executeQueryStorage()
                AyraActionType.CREATE_NOTE -> executeCreateNote(command.target, command.extraParam)
                AyraActionType.STOP_LISTENING -> ExecutionResult(
                    isSuccess = true,
                    feedbackMessage = "Voice session stopped.",
                    spokenResponse = command.spokenResponse
                )
                AyraActionType.GENERAL_QUERY -> ExecutionResult(
                    isSuccess = true,
                    feedbackMessage = "Responded to query.",
                    spokenResponse = command.spokenResponse
                )
                AyraActionType.UNKNOWN -> ExecutionResult(
                    isSuccess = false,
                    feedbackMessage = "Command not recognized.",
                    spokenResponse = "I'm not sure how to do that yet."
                )
            }
        } catch (e: Exception) {
            ExecutionResult(
                isSuccess = false,
                feedbackMessage = "Error executing action: ${e.message}",
                spokenResponse = "Sorry, I ran into an issue executing that command."
            )
        }

        // Record in command history
        try {
            database.commandDao().insertCommand(
                CommandHistoryEntity(
                    spokenText = if (command.target.isNotBlank()) "${command.actionType.name}: ${command.target}" else command.actionType.name,
                    actionType = command.actionType.name,
                    executionStatus = if (result.isSuccess) "SUCCESS" else "FAILED",
                    feedbackMessage = result.feedbackMessage,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (_: Exception) {}

        result
    }

    private fun executeOpenApp(appName: String): ExecutionResult {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val targetQuery = appName.trim().lowercase(Locale.ROOT)

        val matchedApp = resolveInfos.firstOrNull { info ->
            val label = info.loadLabel(pm).toString().lowercase(Locale.ROOT)
            label.contains(targetQuery) || targetQuery.contains(label)
        }

        return if (matchedApp != null) {
            val launchIntent = pm.getLaunchIntentForPackage(matchedApp.activityInfo.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                val appLabel = matchedApp.loadLabel(pm).toString()
                ExecutionResult(true, "Opened $appLabel", "Opening $appLabel")
            } else {
                ExecutionResult(false, "Could not launch package for $appName", "I found $appName but cannot open it directly.")
            }
        } else {
            // Check common app intents
            val fallbackPackage = when (targetQuery) {
                "youtube" -> "com.google.android.youtube"
                "chrome" -> "com.android.chrome"
                "whatsapp" -> "com.whatsapp"
                "facebook" -> "com.facebook.katana"
                "maps" -> "com.google.android.apps.maps"
                "camera" -> return executeOpenCamera()
                "calculator" -> return executeOpenCalculator()
                else -> null
            }

            if (fallbackPackage != null) {
                val launchIntent = pm.getLaunchIntentForPackage(fallbackPackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return ExecutionResult(true, "Launched $appName", "Opening $appName")
                }
            }

            ExecutionResult(false, "$appName is not installed on this device.", "I couldn't find $appName installed on your phone.")
        }
    }

    private fun executeSearchYouTube(query: String): ExecutionResult {
        val cleanQuery = query.trim()
        val intent = Intent(Intent.ACTION_SEARCH).apply {
            `package` = "com.google.android.youtube"
            putExtra("query", cleanQuery)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val webYt = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(cleanQuery))).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webYt)
            }
            ExecutionResult(true, "YouTube search opened for: $cleanQuery", "Searching YouTube for $cleanQuery")
        } catch (_: Exception) {
            ExecutionResult(false, "Failed to launch YouTube search", "I could not open YouTube search.")
        }
    }

    private fun executeSearchWeb(query: String): ExecutionResult {
        val clean = query.trim()
        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra("query", clean)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            if (searchIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(searchIntent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(clean))).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            }
            ExecutionResult(true, "Web search opened for: $clean", "Searching the web for $clean")
        } catch (e: Exception) {
            ExecutionResult(false, "Web search failed: ${e.message}", "Failed to open web search.")
        }
    }

    private fun executeCallPhone(target: String): ExecutionResult {
        val cleanTarget = target.trim()
        var phoneNumber: String? = null
        var contactName = cleanTarget

        val hasReadContacts = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (hasReadContacts && !cleanTarget.matches(Regex("^[0-9+\\-\\s()]+$"))) {
            // Resolve contact name from address book
            try {
                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    ),
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                    arrayOf("%$cleanTarget%"),
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val numIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        if (numIndex != -1) phoneNumber = it.getString(numIndex)
                        if (nameIndex != -1) contactName = it.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}
        }

        val dialTarget = phoneNumber ?: cleanTarget
        val hasCallPhone = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPhone && phoneNumber != null) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(dialTarget)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            // Safe fallback to dialer if CALL_PHONE permission is not granted or number is raw
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(dialTarget)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Calling $contactName ($dialTarget)", "Calling $contactName.")
        } catch (e: Exception) {
            ExecutionResult(false, "Could not initiate call: ${e.message}", "Unable to place call.")
        }
    }

    private fun executeSendSms(target: String, messageBody: String): ExecutionResult {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(target)}")
            putExtra("sms_body", messageBody)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "SMS drafted to $target", "Preparing message for $target.")
        } catch (e: Exception) {
            ExecutionResult(false, "Could not open messaging: ${e.message}", "Failed to open messaging app.")
        }
    }

    private fun executeToggleFlashlight(enable: Boolean): ExecutionResult {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return ExecutionResult(false, "Camera hardware not accessible", "Camera service unavailable.")
        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull()
                ?: return ExecutionResult(false, "No flashlight camera found", "Flashlight hardware not found.")
            cameraManager.setTorchMode(cameraId, enable)
            val msg = if (enable) "Flashlight turned on" else "Flashlight turned off"
            ExecutionResult(true, msg, msg)
        } catch (e: Exception) {
            ExecutionResult(false, "Flashlight error: ${e.message}", "Could not switch flashlight.")
        }
    }

    private fun executeOpenSettings(sub: String): ExecutionResult {
        val action = when (sub.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound" -> Settings.ACTION_SOUND_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Opened $sub settings", "Opening $sub settings.")
        } catch (e: Exception) {
            ExecutionResult(false, "Could not open settings: ${e.message}", "Failed to open settings.")
        }
    }

    private fun executeOpenCamera(): ExecutionResult {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Camera opened", "Opening camera.")
        } catch (e: Exception) {
            ExecutionResult(false, "Could not open camera: ${e.message}", "Camera not available.")
        }
    }

    private fun executeOpenCalculator(): ExecutionResult {
        val calcPackages = listOf(
            "com.google.android.calculator",
            "com.android.calculator2",
            "com.sec.android.app.popupcalculator",
            "com.miui.calculator"
        )
        for (pkg in calcPackages) {
            val launch = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launch)
                return ExecutionResult(true, "Calculator opened", "Opening calculator.")
            }
        }
        return ExecutionResult(false, "Calculator app not found", "Could not locate a calculator app on your phone.")
    }

    private fun executeOpenCalendar(): ExecutionResult {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("content://com.android.calendar/time")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Calendar opened", "Opening calendar.")
        } catch (_: Exception) {
            ExecutionResult(false, "Calendar app not found", "Could not open calendar.")
        }
    }

    private fun executeOpenClock(): ExecutionResult {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Clock and alarms opened", "Opening clock and alarms.")
        } catch (_: Exception) {
            ExecutionResult(false, "Clock app not found", "Could not open clock.")
        }
    }

    private fun executeOpenFiles(): ExecutionResult {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            ExecutionResult(true, "Storage access opened", "Opening files.")
        } catch (_: Exception) {
            ExecutionResult(false, "File manager not available", "Could not launch file manager.")
        }
    }

    private fun executeAccessibilityAction(action: String): ExecutionResult {
        val service = AyraAccessibilityService.instance
        if (service == null) {
            return ExecutionResult(
                false,
                "Accessibility service not enabled in Android Settings.",
                "AYRA Accessibility Service is not turned on. Please enable it in Settings to allow screen actions."
            )
        }
        val success = when (action) {
            "SCROLL_DOWN" -> service.scrollDown()
            "SCROLL_UP" -> service.scrollUp()
            "GO_BACK" -> service.goBack()
            "TAKE_SCREENSHOT" -> service.takeScreenshot()
            else -> false
        }
        val spoken = if (success) "Action performed." else "Could not perform screen action."
        return ExecutionResult(success, "Accessibility: $action", spoken)
    }

    private fun executeLockScreen(): ExecutionResult {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val adminComponent = ComponentName(context, AyraDeviceAdminReceiver::class.java)
        return if (dpm != null && dpm.isAdminActive(adminComponent)) {
            dpm.lockNow()
            ExecutionResult(true, "Screen locked.", "Screen locked.")
        } else {
            // Open screen lock settings
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ExecutionResult(false, "Device Admin not enabled. Opened Security Settings.", "Screen lock requires Device Admin. Opening Security Settings.")
        }
    }

    private fun executeQueryBattery(): ExecutionResult {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryPct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val isCharging = bm?.isCharging == true
        val statusText = if (isCharging) "charging" else "discharging"
        val message = if (batteryPct >= 0) {
            "Your battery is at $batteryPct% and currently $statusText."
        } else {
            "Could not determine battery level."
        }
        return ExecutionResult(true, message, message)
    }

    private fun executeQueryTimeDate(): ExecutionResult {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val now = Date()
        val formattedTime = timeFormat.format(now)
        val formattedDate = dateFormat.format(now)
        val response = "It is currently $formattedTime on $formattedDate."
        return ExecutionResult(true, response, response)
    }

    private fun executeQueryStorage(): ExecutionResult {
        return try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val availableBytes = statFs.availableBytes
            val totalBytes = statFs.totalBytes
            val availableGb = availableBytes / (1024 * 1024 * 1024.0)
            val totalGb = totalBytes / (1024 * 1024 * 1024.0)
            val response = String.format(Locale.US, "You have %.1f GB free out of %.1f GB total phone storage.", availableGb, totalGb)
            ExecutionResult(true, response, response)
        } catch (e: Exception) {
            ExecutionResult(false, "Storage check failed: ${e.message}", "Unable to check phone storage.")
        }
    }

    private suspend fun executeCreateNote(title: String, content: String): ExecutionResult {
        val noteTitle = title.ifBlank { "Voice Note" }
        val noteContent = content.ifBlank { "Recorded by AYRA" }
        database.noteDao().insertNote(
            NoteEntity(
                title = noteTitle,
                content = noteContent,
                createdAt = System.currentTimeMillis()
            )
        )
        return ExecutionResult(true, "Saved note: $noteContent", "Saved note: $noteContent")
    }

    fun getInstalledAppNames(): List<String> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(mainIntent, 0).mapNotNull {
            it.loadLabel(pm)?.toString()
        }.distinct().take(40)
    }
}
