package com.taile.petevo.platform

import kotlinx.browser.window

class WebPetStorage : PetStorage {

    private val storage get() = window.localStorage

    override fun savePet(json: String) {
        storage.setItem("pet_data", json)
    }

    override fun loadPet(): String? {
        return storage.getItem("pet_data")
    }

    override fun saveCooldownEnd(timestampMs: Long) {
        storage.setItem("cooldown_end", timestampMs.toString())
    }

    override fun loadCooldownEnd(): Long {
        return storage.getItem("cooldown_end")?.toLongOrNull() ?: 0L
    }

    override fun saveRunningSession(data: String) {
        storage.setItem("running_session", data)
    }

    override fun loadRunningSession(): String? {
        return storage.getItem("running_session")
    }

    override fun clearRunningSession() {
        storage.removeItem("running_session")
    }
}

