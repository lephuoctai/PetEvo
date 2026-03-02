package com.taile.petevo.platform

/**
 * Platform-specific storage for persisting pet data.
 */
interface PetStorage {
    fun savePet(json: String)
    fun loadPet(): String?
    fun saveCooldownEnd(timestampMs: Long)
    fun loadCooldownEnd(): Long

    /** Persist a running session so we can detect exit-without-finishing */
    fun saveRunningSession(data: String)
    fun loadRunningSession(): String?
    fun clearRunningSession()
}

