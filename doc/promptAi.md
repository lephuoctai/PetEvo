# ROLE: SENIOR KOTLIN MULTIPLATFORM & GAME SYSTEMS ARCHITECT

## 🎯 PROJECT OVERVIEW

Build "FocusPet", a Hardcore Focus App using Kotlin Multiplatform (KMP) for Android and PWA.

The app rewards users for staying focused and punishes them (and their pet) for distractions.

## 🏗️ ARCHITECTURE & TECH STACK

- **Language:** Kotlin
- **Framework:** Compose Multiplatform (Shared UI)
- **Platforms:** Android (Native), Web (PWA)
- **State Management:** MVI or StateFlow-based Reducer in `shared` module.
- **Persistence:** Local only (SharedPreferences for Android, LocalStorage for Web).
- **Structure:**
    - `shared/model`: Immutable data models.
    - `shared/logic`: XpCalculator, EmotionEngine, LevelSystem.
    - `shared/engine`: FocusEngine (State machine).
    - `androidMain`: Foreground Service, DND Manager, Hardware Toggles.
    - `webMain`: Visibility API, Wake Lock API.

## 🧠 CORE GAME LOGIC (STRICT ADHERENCE)

### 1. XP Formulas

- **Deep Focus (High Risk):** `y = ((x / 30)^2 + 0.8) * 100`
- **Pomodoro (Stable):** `y = 1.5 * x`
- *Note: Minimum session is 10 minutes. Preview projected XP in UI during setup.*

### 2. Pet Data Model

```
data class Pet(
    val level: Int = 1,
    val xp: Int = 0,
    val emotion: Int = 50, // Range -10 to 99
    val totalFocusMinutes: Int = 0,
    val streak: Int = 0,
    val lastActiveDate: Long = 0
)
```

### 3. Emotion XP Multipliers

- **87 to 99 (Happy):** 150% XP
- **80 to 86 (Neutral):** 110% XP
- **50 to 79 (Neutral):** 95% XP
- **0 to 49 (Sad):** 80% XP
- **10 to -1 (Very Sad):** -110% XP
- *Clamp emotion value: `emotion.coerceIn(-10, 99)`*

### 4. Leveling System

- XP for next level: `100 + (level * level * 0.05)`
- Level up carries over remaining XP.

## 🛡️ THE HARDCORE FOCUS ENGINE (STATE MACHINE)

### Session Logic:

- **Success:** Timer reaches 0. Reward full XP * multiplier. Emotion +8 (Deep) or +5 (Pomo).
- **Failure Trigger:** - **Web:** `visibilitychange` (hidden) or `window.blur`.
    - **Android:** User exits app or cancels.
- **Failure Penalty:** User gets only 50% of projected XP. Emotion -15 (Deep) or -8 (Pomo). **No streak counted.**
- **Cooldown:** On failure, block new sessions for **5 minutes** (track timestamp).

### Streak Logic:

- Reset to 0 at 00:00 if no successful session (min 10m) occurred the previous day.

## 📱 PLATFORM SPECIFIC REQUIREMENTS

### Android (Deep Hardware Control):

- **Foreground Service:** Keep timer running if app is in foreground but screen is off.
- **Strict Lockdown:** - Enable **Do Not Disturb (DND)** (Calls allowed, notifications blocked).
    - Toggle **OFF** WiFi, Bluetooth, and GPS upon starting session.
- **Emergency Cancel:** Two-step confirmation dialog (Are you sure you want to abandon your pet?).

### Web (PWA):

- **Wake Lock API:** Prevent screen from dimming/sleeping.
- **Strict Visibility:** Immediate failure if tab is switched or minimized.

## 🎨 UI/UX DESIGN SPECIFICATIONS

- **Responsive:** Support Portrait & Landscape.
- **Home Screen:** Pet avatar, Gradient Emotion Bar , Level/XP progress.
- **Setup Screen:** Time slider (min 10), Mode toggle, **Live XP Preview**.
- **Focus Screen:** Large countdown, Progress bar, Pet "Focusing" animation (Low FPS).
- **Result Screen:** "Success" with level-up animation or "Failure" showing sad pet and cooldown timer.

## 🛠️ IMPLEMENTATION STEPS FOR AGENT

1. Build `shared/model` and `shared/logic` (Deterministic XP/Emotion math).
2. Build `FocusEngine` in `shared` module with state handling (IDLE, RUNNING, SUCCESS, FAIL, COOLDOWN).
3. Create `SystemController` interface for platform-specific hardware control.
4. Implement `androidMain` (Permissions for DND, WiFi, BT, GPS + Foreground Service).
5. Implement `webMain` (WakeLock and Visibility monitoring).
6. Build the Compose Multiplatform UI.

**GO! DO NOT OVERENGINEER. KEEP IT LIGHTWEIGHT AND EMOTIONALLY IMPACTFUL.**