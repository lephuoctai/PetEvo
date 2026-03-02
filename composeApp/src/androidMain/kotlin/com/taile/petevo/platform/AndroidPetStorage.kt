package com.taile.petevo.platform

import android.content.Context
import android.content.SharedPreferences

class AndroidPetStorage(context: Context) : PetStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("petevo_prefs", Context.MODE_PRIVATE)

    override fun savePet(json: String) {
        prefs.edit().putString("pet_data", json).apply()
    }

    override fun loadPet(): String? {
        return prefs.getString("pet_data", null)
    }

    override fun saveCooldownEnd(timestampMs: Long) {
        prefs.edit().putLong("cooldown_end", timestampMs).apply()
    }

    override fun loadCooldownEnd(): Long {
        return prefs.getLong("cooldown_end", 0L)
    }
}

