package com.taile.petevo

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.taile.petevo.engine.FocusEngine
import com.taile.petevo.model.SessionState
import com.taile.petevo.platform.rememberPetStorage
import com.taile.petevo.platform.rememberSystemController
import com.taile.petevo.ui.Screen
import com.taile.petevo.ui.screens.FocusScreen
import com.taile.petevo.ui.screens.HomeScreen
import com.taile.petevo.ui.screens.ResultScreen
import com.taile.petevo.ui.screens.SetupScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import androidx.compose.foundation.layout.Box

@Composable
fun App() {
    val systemController = rememberSystemController()
    val petStorage = rememberPetStorage()
    val engineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    val engine = remember { FocusEngine(systemController, petStorage, engineScope) }
    val stateValue = engine.state.collectAsState()
    // Read the snapshot state (not delegated, so derivedStateOf can track it)
    val state = stateValue.value

    // Manual override for SETUP screen (no SessionState equivalent)
    var showSetup by remember { mutableStateOf(false) }

    // Screen is derived DIRECTLY from sessionState — instant, no async
    val currentScreen = when (state.sessionState) {
        SessionState.RUNNING -> Screen.FOCUS
        SessionState.SUCCESS, SessionState.FAIL -> Screen.RESULT
        SessionState.COOLDOWN, SessionState.IDLE -> {
            if (showSetup) Screen.SETUP else Screen.HOME
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
        ) {
            Crossfade(targetState = currentScreen) { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        state = state,
                        onStartSetup = { showSetup = true }
                    )

                    Screen.SETUP -> SetupScreen(
                        previewXp = { mode, duration -> engine.previewXp(mode, duration) },
                        onStart = { mode, duration ->
                            showSetup = false
                            engine.startSession(mode, duration)
                        },
                        onBack = { showSetup = false }
                    )

                    Screen.FOCUS -> FocusScreen(
                        state = state,
                        onCancel = { engine.cancelSession() }
                    )

                    Screen.RESULT -> ResultScreen(
                        state = state,
                        onDismiss = {
                            showSetup = false
                            engine.acknowledge()
                        }
                    )
                }
            }
        }
    }
}
