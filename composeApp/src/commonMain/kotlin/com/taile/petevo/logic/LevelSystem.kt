package com.taile.petevo.logic

import com.taile.petevo.model.Pet
import kotlin.math.roundToInt

object LevelSystem {

    /**
     * XP required to reach the next level: 100 + (level * level * 0.05)
     */
    fun xpForNextLevel(level: Int): Int {
        return (100 + level * level * 0.05).roundToInt()
    }

    /**
     * Apply XP to pet, handling level-ups with carry-over.
     */
    fun applyXp(pet: Pet, xpGained: Int): Pet {
        var currentLevel = pet.level
        var currentXp = pet.xp + xpGained

        // Handle negative XP (penalty from very sad emotion)
        if (currentXp < 0) currentXp = 0

        // Level up with carry-over
        while (currentXp >= xpForNextLevel(currentLevel)) {
            currentXp -= xpForNextLevel(currentLevel)
            currentLevel++
        }

        return pet.copy(level = currentLevel, xp = currentXp)
    }
}

