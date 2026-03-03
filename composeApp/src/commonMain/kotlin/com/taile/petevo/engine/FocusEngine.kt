package com.taile.petevo.engine

import com.taile.petevo.logic.EmotionEngine
import com.taile.petevo.logic.LevelSystem
import com.taile.petevo.logic.XpCalculator
import com.taile.petevo.model.FocusMode
import com.taile.petevo.model.FocusSession
import com.taile.petevo.model.Pet
import com.taile.petevo.model.SessionState
import com.taile.petevo.platform.PetStorage
import com.taile.petevo.platform.SystemController
import com.taile.petevo.platform.currentTimeMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt

private const val TAG = "bug"

// Simple logging expect/actual
internal expect fun logDebug(tag: String, message: String)

data class FocusUiState(
    val pet: Pet = Pet(),
    val sessionState: SessionState = SessionState.IDLE,
    val session: FocusSession = FocusSession(),
    val cooldownRemainingSeconds: Int = 0,
    val lastXpGained: Int = 0,
    val didLevelUp: Boolean = false
)

class FocusEngine(
    private val systemController: SystemController,
    private val storage: PetStorage,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(FocusUiState())
    val state: StateFlow<FocusUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var visibilityJob: Job? = null
    private var cooldownJob: Job? = null

    // Thread-safe flag to prevent double end-session calls
    private val sessionEndedFlow = MutableStateFlow(false)
    private var sessionEnded: Boolean
        get() = sessionEndedFlow.value
        set(value) { sessionEndedFlow.value = value }

    companion object {
        private const val COOLDOWN_DURATION_MS = 5000
    }

    init {
        logDebug(TAG, "FocusEngine init")
        loadPet()
        recoverSession()
        checkCooldown()
    }

    private fun loadPet() {
        val json = storage.loadPet()
        if (json != null) {
            val pet = deserializePet(json)
            logDebug(TAG, "loadPet: level=${pet.level} xp=${pet.xp} emotion=${pet.emotion}")
            _state.update { it.copy(pet = pet) }
        }
        checkStreakReset()
    }

    private fun savePet(pet: Pet) {
        logDebug(TAG, "savePet: level=${pet.level} xp=${pet.xp} emotion=${pet.emotion}")
        storage.savePet(serializePet(pet))
    }

    private fun recoverSession() {
        val savedData = storage.loadRunningSession()
        logDebug(TAG, "recoverSession: savedData=$savedData")
        if (savedData != null) {
            val session = deserializeSession(savedData)
            if (session != null) {
                logDebug(TAG, "recoverSession: found unfinished session -> FAIL")
                storage.clearRunningSession()
                applyFailurePenalty(session)
            } else {
                storage.clearRunningSession()
            }
        }
    }

    private fun checkStreakReset() {
        val pet = _state.value.pet
        val now = currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        if (pet.lastActiveDate > 0 && (now - pet.lastActiveDate) > oneDayMs) {
            val updatedPet = pet.copy(streak = 0)
            _state.update { it.copy(pet = updatedPet) }
            savePet(updatedPet)
        }
    }

    private fun checkCooldown() {
        val cooldownEnd = storage.loadCooldownEnd()
        val now = currentTimeMillis()
        if (cooldownEnd > now) {
            logDebug(TAG, "checkCooldown: on cooldown, remaining=${(cooldownEnd - now) / 1000}s")
            _state.update { it.copy(sessionState = SessionState.COOLDOWN) }
            startCooldownTimer(cooldownEnd)
        }
    }

    fun previewXp(mode: FocusMode, durationMinutes: Int): Int {
        val baseXp = XpCalculator.calculateXp(mode, durationMinutes)
        val multiplier = EmotionEngine.getMultiplier(_state.value.pet.emotion)
        return (baseXp * multiplier).roundToInt()
    }

    fun startSession(mode: FocusMode, durationMinutes: Int) {
        logDebug(TAG, "startSession: mode=$mode dur=$durationMinutes state=${_state.value.sessionState}")
        if (_state.value.sessionState != SessionState.IDLE) {
            logDebug(TAG, "startSession: BLOCKED, not IDLE")
            return
        }

        val duration = durationMinutes.coerceAtLeast(10)
        val projectedXp = previewXp(mode, duration)

        val session = FocusSession(
            mode = mode,
            durationMinutes = duration,
            startTimeMs = currentTimeMillis(),
            projectedXp = projectedXp,
            remainingSeconds = duration * 60
        )

        sessionEnded = false
        storage.saveRunningSession(serializeSession(session))

        _state.update {
            it.copy(
                sessionState = SessionState.RUNNING,
                session = session,
                lastXpGained = 0,
                didLevelUp = false
            )
        }

        systemController.acquireWakeLock()
        systemController.enableFocusMode()
        systemController.toggleConnectivity(false)

        // Countdown timer
        timerJob = scope.launch {
            logDebug(TAG, "timerJob: started ${duration * 60}s")
            var remaining = duration * 60
            while (remaining > 0 && isActive) {
                //debug testing
                delay(1L)
                remaining--
                _state.update { it.copy(session = it.session.copy(remainingSeconds = remaining)) }
            }
            if (isActive && !sessionEnded) {
                logDebug(TAG, "timerJob: done -> endSessionWithSuccess")
                endSessionWithSuccess()
            }
        }

        // Visibility monitor (1.5s grace period to avoid false triggers on start)
        visibilityJob = scope.launch {
            delay(1500L)
            logDebug(TAG, "visibilityJob: monitoring started")
            systemController.observeAppVisibility().collect { visible ->
                logDebug(TAG, "visibility: visible=$visible ended=$sessionEnded state=${_state.value.sessionState}")
                if (!visible && !sessionEnded && _state.value.sessionState == SessionState.RUNNING) {
                    logDebug(TAG, "visibility: hidden -> endSessionWithFailure")
                    endSessionWithFailure()
                }
            }
        }
    }

    /**
     * Simple guard: only the first caller wins.
     * Safe because: JS/Wasm is single-threaded, and on JVM all callers
     * are on the same coroutine dispatcher (no true parallelism).
     */
    private fun tryEndSession(): Boolean {
        if (!sessionEndedFlow.compareAndSet(expect = false, update = true)) {
            logDebug(TAG, "tryEndSession: ALREADY ENDED")
            return false
        }
        logDebug(TAG, "tryEndSession: OK")
        return true
    }

    private fun endSessionWithSuccess() {
        if (!tryEndSession()) return

        timerJob?.cancel()
        visibilityJob?.cancel()
        timerJob = null
        visibilityJob = null

        storage.clearRunningSession()

        val currentState = _state.value
        val session = currentState.session
        val pet = currentState.pet

        val baseXp = XpCalculator.calculateXp(session.mode, session.durationMinutes)
        val multiplier = EmotionEngine.getMultiplier(pet.emotion)
        val xpGained = (baseXp * multiplier).roundToInt()

        val newEmotion = EmotionEngine.applyDelta(pet.emotion, EmotionEngine.successDelta(session.mode))
        val prevLevel = pet.level
        var updatedPet = LevelSystem.applyXp(pet.copy(emotion = newEmotion), xpGained)
        updatedPet = updatedPet.copy(
            totalFocusMinutes = updatedPet.totalFocusMinutes + session.durationMinutes,
            streak = updatedPet.streak + 1,
            lastActiveDate = currentTimeMillis()
        )

        savePet(updatedPet)
        releasePlatformResources()

        logDebug(TAG, "SUCCESS: xp=$xpGained level=${updatedPet.level} emotion=${updatedPet.emotion}")

        systemController.vibrate(true)
        systemController.playNotificationSound(true)

        _state.update {
            it.copy(
                pet = updatedPet,
                sessionState = SessionState.SUCCESS,
                lastXpGained = xpGained,
                didLevelUp = updatedPet.level > prevLevel
            )
        }
    }

    private fun endSessionWithFailure() {
        if (!tryEndSession()) return

        timerJob?.cancel()
        visibilityJob?.cancel()
        timerJob = null
        visibilityJob = null

        storage.clearRunningSession()

        val session = _state.value.session
        logDebug(TAG, "FAILURE: mode=${session.mode} dur=${session.durationMinutes}")

        applyFailurePenalty(session)
    }

    private fun applyFailurePenalty(session: FocusSession) {
        val pet = _state.value.pet
        val xpGained = 0
        val newEmotion = EmotionEngine.applyDelta(pet.emotion, EmotionEngine.failureDelta(session.mode))
        val updatedPet = pet.copy(emotion = newEmotion, lastActiveDate = currentTimeMillis())

        savePet(updatedPet)

        val cooldownEnd = currentTimeMillis() + COOLDOWN_DURATION_MS
        storage.saveCooldownEnd(cooldownEnd)
        releasePlatformResources()

        logDebug(TAG, "applyFailurePenalty: emotion=${updatedPet.emotion} -> state=FAIL")

        systemController.vibrate(false)
        systemController.playNotificationSound(false)

        _state.update {
            it.copy(
                pet = updatedPet,
                sessionState = SessionState.FAIL,
                lastXpGained = xpGained,
                didLevelUp = false
            )
        }
        // Do NOT start cooldown timer here — it would immediately overwrite
        // FAIL → COOLDOWN and skip the Result screen. The cooldown timer
        // starts when the user taps "acknowledge" on the Result screen.
    }

    private fun releasePlatformResources() {
        try {
            systemController.releaseWakeLock()
            systemController.disableFocusMode()
            systemController.toggleConnectivity(true)
        } catch (e: Exception) {
            logDebug(TAG, "releasePlatformResources error: ${e.message}")
        }
    }

    private fun startCooldownTimer(cooldownEndMs: Long) {
        cooldownJob?.cancel()
        cooldownJob = scope.launch {
            while (isActive) {
                val remaining = ((cooldownEndMs - currentTimeMillis()) / 1000).toInt()
                if (remaining <= 0) {
                    _state.update { it.copy(sessionState = SessionState.IDLE, cooldownRemainingSeconds = 0) }
                    break
                }
                _state.update { it.copy(cooldownRemainingSeconds = remaining, sessionState = SessionState.COOLDOWN) }
                delay(1000L)
            }
        }
    }

    fun cancelSession() {
        logDebug(TAG, "cancelSession: state=${_state.value.sessionState}")
        if (_state.value.sessionState == SessionState.RUNNING) {
            endSessionWithFailure()
        }
    }

    fun acknowledge() {
        logDebug(TAG, "acknowledge")
        val cooldownEnd = storage.loadCooldownEnd()
        val now = currentTimeMillis()
        if (cooldownEnd > now) {
            _state.update { it.copy(sessionState = SessionState.COOLDOWN) }
            startCooldownTimer(cooldownEnd)
        } else {
            _state.update { it.copy(sessionState = SessionState.IDLE) }
        }
    }

    // --- Serialization ---

    private fun serializePet(pet: Pet): String {
        return "${pet.level},${pet.xp},${pet.emotion},${pet.totalFocusMinutes},${pet.streak},${pet.lastActiveDate}"
    }

    private fun deserializePet(json: String): Pet {
        return try {
            val parts = json.split(",")
            Pet(
                level = parts[0].toInt(),
                xp = parts[1].toInt(),
                emotion = parts[2].toInt(),
                totalFocusMinutes = parts[3].toInt(),
                streak = parts[4].toInt(),
                lastActiveDate = parts[5].toLong()
            )
        } catch (e: Exception) {
            Pet()
        }
    }

    private fun serializeSession(session: FocusSession): String {
        return "${session.mode.name},${session.durationMinutes},${session.startTimeMs},${session.projectedXp}"
    }

    private fun deserializeSession(data: String): FocusSession? {
        return try {
            val parts = data.split(",")
            FocusSession(
                mode = FocusMode.valueOf(parts[0]),
                durationMinutes = parts[1].toInt(),
                startTimeMs = parts[2].toLong(),
                projectedXp = parts[3].toInt()
            )
        } catch (e: Exception) {
            null
        }
    }
}
