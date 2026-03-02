
package com.taile.petevo.platform

import kotlinx.browser.window

class WasmWebPetStorage : PetStorage {

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
}

