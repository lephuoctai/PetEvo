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
    val state by engine.state.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    // Auto-navigate based on session state changes
    LaunchedEffect(state.sessionState) {
        when (state.sessionState) {
            SessionState.SUCCESS, SessionState.FAIL -> currentScreen = Screen.RESULT
            SessionState.RUNNING -> currentScreen = Screen.FOCUS
            else -> {}
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
                        onStartSetup = { currentScreen = Screen.SETUP }
                    )

                    Screen.SETUP -> SetupScreen(
                        previewXp = { mode, duration -> engine.previewXp(mode, duration) },
                        onStart = { mode, duration ->
                            engine.startSession(mode, duration)
                        },
                        onBack = { currentScreen = Screen.HOME }
                    )

                    Screen.FOCUS -> FocusScreen(
                        state = state,
                        onCancel = { engine.cancelSession() }
                    )

                    Screen.RESULT -> ResultScreen(
                        state = state,
                        onDismiss = {
                            engine.acknowledge()
                            currentScreen = Screen.HOME
                        }
                    )
                }
            }
        }
    }
}
