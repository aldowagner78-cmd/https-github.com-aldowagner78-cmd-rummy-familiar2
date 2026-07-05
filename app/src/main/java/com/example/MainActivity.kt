package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.UserProfileEntity
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val gameViewModel: GameViewModel = viewModel()
                val currentScreen by gameViewModel.currentScreen.collectAsState()
                val profile by gameViewModel.userProfile.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Crossfade(
                        targetState = currentScreen,
                        modifier = Modifier.padding(innerPadding),
                        label = "screen_navigation"
                    ) { screen ->
                        when (screen) {
                            "menu" -> {
                                MainMenuScreen(
                                    viewModel = gameViewModel,
                                    profile = profile ?: UserProfileEntity()
                                )
                            }
                            "lobby" -> {
                                LobbyScreen(viewModel = gameViewModel)
                            }
                            "game" -> {
                                GameScreen(viewModel = gameViewModel)
                            }
                            "rules" -> {
                                RulesScreen(viewModel = gameViewModel)
                            }
                            "history" -> {
                                StatsHistoryScreen(viewModel = gameViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
