# AYRA — AI Voice-Controlled Android Assistant

AYRA is a native Android voice assistant engineered to give you full hands-free control of your phone with natural speech in **English**, **Bengali (বাংলা)**, and **Banglish**. AYRA is not a messaging chatbot; it is a personal device control center that acts upon spoken commands, manages foreground voice sessions, and answers queries using the Gemini API.

---

## 🌟 Key Features & Capabilities

### 1. Voice Interaction & Session Engine
- **Android SpeechRecognizer Integration**: Real-time voice capture with live transcription and audio level RMS feedback.
- **Android Text-To-Speech (TTS)**: Spoken voice responses with adjustable speech rate and language detection (Bengali & English).
- **Interactive Glowing Voice Orb**: Pulsates and responds dynamically to speech input, processing, and speaking states.
- **Active Listening Sessions with Countdown**: Configurable 1, 3, 5, or 10-minute sessions.
- **Auto-Reset Timer**: Every successful command automatically refreshes the session countdown, allowing seamless continuous conversations.
- **Emergency Stop & Sleep**: Stop listening instantly via voice ("Ayra stop", "sleep for now", "শোনা বন্ধ করো") or UI button.

### 2. Device Automation & Actions
- **App Management**: Opens installed apps by name ("Open YouTube", "Open WhatsApp", "Open Chrome").
- **Camera & Flashlight**: Hardware torch toggle ("Turn on flashlight", "টর্চ জ্বালাও") and still camera launch.
- **Phone Calls & Contacts**: Voice dialing with contact name resolution ("Call Mother", "Call John"). Safe fallback to system dialer if direct call permission is not granted.
- **Search & Media**: Direct YouTube search query intent ("Search YouTube for Minecraft gameplay") and web searches.
- **System Settings**: Quick navigation to Wi-Fi, Bluetooth, Display, Sound, and General Android settings.
- **System Telemetry**: Real-time battery status ("What's my battery level?"), storage space availability, and current time/date queries.
- **Voice Notes & Reminders**: Create, view, and organize voice notes stored locally in a Room SQLite database.
- **Authorized File Manager**: Browse, search, view, rename, create, and delete authorized documents with exact-name confirmation.

### 3. Screen Interaction via Accessibility Service
- **Voice-Directed Scrolling**: "Scroll down", "Scroll up" using gesture dispatch.
- **Navigation Gestures**: "Go back", "Go home".
- **Screenshot Capture**: Hands-free screenshot execution without third-party overlays.
- **Zero-Spying Policy**: Only runs when user speaks an explicit command; never captures passwords, keystrokes, or private messages.

### 4. Gemini AI Intelligence Layer
- **Structured Intent Pipeline**: Uses Gemini models (`gemini-3.5-flash` or `gemini-3.1-pro-preview`) to understand complex multi-step prompts.
- **Offline Fallback Engine**: If no internet or API key is present, AYRA's local rule engine immediately executes hardware controls and device actions locally in sub-10ms.
- **Secure Key Management**: Configurable via AYRA's Settings screen and pre-injected via `BuildConfig.GEMINI_API_KEY`.

---

## 🔒 Permissions & Safety Architecture

| Permission | Purpose | Safe Fallback |
|---|---|---|
| `RECORD_AUDIO` | Voice speech recognition | Manual text input bar |
| `CALL_PHONE` | Direct voice-activated phone calls | Launches Android dialer with number pre-filled |
| `READ_CONTACTS` | Resolves contact names to phone numbers | Direct digit dialing |
| `CAMERA` | Flashlight torch toggle & camera open | Settings fallback |
| `POST_NOTIFICATIONS` | Ongoing background session countdown | In-app timer display |
| `Accessibility Service` | Hands-free scrolling and back buttons | User performs manual touch |
| `Device Admin` | Lock phone upon voice command | Opens Security Lock screen |

---

## 🛠️ Testing & Setup Instructions

1. **Build & Run**:
   - Open project in Android Studio (Electric Eel or newer recommended).
   - Sync Gradle project and run `:app:compileDebugKotlin`.
   - Run unit and Robolectric tests: `gradle :app:testDebugUnitTest`.

2. **Configure Gemini API Key**:
   - In AYRA, tap the **Settings** icon (top right).
   - Enter your Gemini API key from Google AI Studio and tap **Save Key**.
   - Tap **Test API** to verify connectivity with the selected Gemini model.

3. **Enable Screen Navigation (Optional)**:
   - Navigate to **Settings > Accessibility Gestures Setup**.
   - Tap **Open Accessibility Settings** and turn on **AYRA**.
