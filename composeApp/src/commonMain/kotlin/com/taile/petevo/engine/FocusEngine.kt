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

    companion object {
        private const val COOLDOWN_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    }

    init {
        loadPet()
        checkCooldown()
    }

    private fun loadPet() {
        val json = storage.loadPet()
        if (json != null) {
            val pet = deserializePet(json)
            _state.update { it.copy(pet = pet) }
        }
        // Check streak reset at midnight
        checkStreakReset()
    }

    private fun savePet(pet: Pet) {
        storage.savePet(serializePet(pet))
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
        if (_state.value.sessionState != SessionState.IDLE) return

        val duration = durationMinutes.coerceAtLeast(10)
        val projectedXp = previewXp(mode, duration)

        val session = FocusSession(
            mode = mode,
            durationMinutes = duration,
            startTimeMs = currentTimeMillis(),
            projectedXp = projectedXp,
            remainingSeconds = duration * 60
        )

        _state.update {
            it.copy(
                sessionState = SessionState.RUNNING,
                session = session,
                lastXpGained = 0,
                didLevelUp = false
            )
        }

        // Platform: wake lock + focus mode
        systemController.acquireWakeLock()
        systemController.enableFocusMode()
        systemController.toggleConnectivity(false)

        // Start countdown
        timerJob = scope.launch {
            var remaining = duration * 60
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _state.update { it.copy(session = it.session.copy(remainingSeconds = remaining)) }
            }
            if (isActive) {
                onSuccess()
            }
        }

        // Monitor visibility
        visibilityJob = scope.launch {
            systemController.observeAppVisibility().collect { visible ->
                if (!visible && _state.value.sessionState == SessionState.RUNNING) {
                    onFailure()
                }
            }
        }
    }

    private fun onSuccess() {
        timerJob?.cancel()
        visibilityJob?.cancel()

        val currentState = _state.value
        val session = currentState.session
        val pet = currentState.pet

        // Calculate XP with emotion multiplier
        val baseXp = XpCalculator.calculateXp(session.mode, session.durationMinutes)
        val multiplier = EmotionEngine.getMultiplier(pet.emotion)
        val xpGained = (baseXp * multiplier).roundToInt()

        // Apply emotion boost
        val newEmotion = EmotionEngine.applyDelta(pet.emotion, EmotionEngine.successDelta(session.mode))

        // Apply XP and level up
        val prevLevel = pet.level
        var updatedPet = LevelSystem.applyXp(pet.copy(emotion = newEmotion), xpGained)

        // Update streak and focus minutes
        updatedPet = updatedPet.copy(
            totalFocusMinutes = updatedPet.totalFocusMinutes + session.durationMinutes,
            streak = updatedPet.streak + 1,
            lastActiveDate = currentTimeMillis()
        )

        savePet(updatedPet)

        // Release platform resources
        systemController.releaseWakeLock()
        systemController.disableFocusMode()
        systemController.toggleConnectivity(true)

        _state.update {
            it.copy(
                pet = updatedPet,
                sessionState = SessionState.SUCCESS,
                lastXpGained = xpGained,
                didLevelUp = updatedPet.level > prevLevel
            )
        }
    }

    private fun onFailure() {
        timerJob?.cancel()
        visibilityJob?.cancel()

        val currentState = _state.value
        val session = currentState.session
        val pet = currentState.pet

        // 50% of projected XP on failure
        val baseXp = XpCalculator.calculateXp(session.mode, session.durationMinutes)
        val multiplier = EmotionEngine.getMultiplier(pet.emotion)
        val xpGained = ((baseXp * multiplier) * 0.5).roundToInt()

        // Apply emotion penalty
        val newEmotion = EmotionEngine.applyDelta(pet.emotion, EmotionEngine.failureDelta(session.mode))

        // Apply XP (no streak counted)
        val prevLevel = pet.level
        var updatedPet = LevelSystem.applyXp(pet.copy(emotion = newEmotion), xpGained)
        updatedPet = updatedPet.copy(lastActiveDate = currentTimeMillis())

        savePet(updatedPet)

        // Set cooldown
        val cooldownEnd = currentTimeMillis() + COOLDOWN_DURATION_MS
        storage.saveCooldownEnd(cooldownEnd)

        // Release platform resources
        systemController.releaseWakeLock()
        systemController.disableFocusMode()
        systemController.toggleConnectivity(true)

        _state.update {
            it.copy(
                pet = updatedPet,
                sessionState = SessionState.FAIL,
                lastXpGained = xpGained,
                didLevelUp = updatedPet.level > prevLevel
            )
        }

        startCooldownTimer(cooldownEnd)
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
        if (_state.value.sessionState == SessionState.RUNNING) {
            onFailure()
        }
    }

    fun acknowledge() {
        // After viewing result screen, go back to IDLE or COOLDOWN
        val cooldownEnd = storage.loadCooldownEnd()
        val now = currentTimeMillis()
        if (cooldownEnd > now) {
            _state.update { it.copy(sessionState = SessionState.COOLDOWN) }
            startCooldownTimer(cooldownEnd)
        } else {
            _state.update { it.copy(sessionState = SessionState.IDLE) }
        }
    }

    // Simple manual JSON serialization (no kotlinx.serialization dependency needed)
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
}

