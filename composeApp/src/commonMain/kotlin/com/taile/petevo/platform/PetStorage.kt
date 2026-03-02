package com.taile.petevo.platform

/**
 * Platform-specific storage for persisting pet data.
 */
interface PetStorage {
    fun savePet(json: String)
    fun loadPet(): String?
    fun saveCooldownEnd(timestampMs: Long)
    fun loadCooldownEnd(): Long
}

