package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.engine.GameEngine
import com.example.data.engine.GameSession
import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
        
        // Seed default profile if empty
        viewModelScope.launch {
            repository.userProfile.collectLatest { profile ->
                if (profile == null) {
                    repository.saveProfile(
                        UserProfileEntity(
                            id = 1,
                            name = "Aldo Wagner",
                            avatarUrl = "",
                            countryFlag = "🇦🇷",
                            wins = 5,
                            losses = 2,
                            level = 4,
                            xp = 420
                        )
                    )
                }
            }
        }
    }

    // 1. Database-backed states
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val matchHistory: StateFlow<List<MatchHistoryEntity>> = repository.matchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Navigation State
    private val _currentScreen = MutableStateFlow("menu") // "menu", "lobby", "game", "rules", "history"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // 3. Gameplay State
    private val _gameSession = MutableStateFlow<GameSession?>(null)
    val gameSession: StateFlow<GameSession?> = _gameSession.asStateFlow()

    private val _selectedHandTiles = MutableStateFlow<List<Tile>>(emptyList())
    val selectedHandTiles: StateFlow<List<Tile>> = _selectedHandTiles.asStateFlow()

    // 4. Online Rooms / Family connection state
    private val _roomCode = MutableStateFlow("FAMILIA77")
    val roomCode: StateFlow<String> = _roomCode.asStateFlow()

    private val _isOnlineMode = MutableStateFlow(false)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    private val _roomPlayers = MutableStateFlow<List<Player>>(emptyList())
    val roomPlayers: StateFlow<List<Player>> = _roomPlayers.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _pingMs = MutableStateFlow(0)
    val pingMs: StateFlow<Int> = _pingMs.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(listOf("Cargando sala..."))
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.BURACO)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun setGameMode(mode: GameMode) {
        _gameMode.value = mode
    }

    fun setRoomCode(code: String) {
        _roomCode.value = code.uppercase()
    }

    // Starts or joins a room for multiplayer feel (family members around the world)
    fun createOrJoinRoom(isOnline: Boolean) {
        _isOnlineMode.value = isOnline
        _currentScreen.value = "lobby"
        
        _chatMessages.value = emptyList()
        _logs.value = listOf("Conectando con el servidor en la nube...")
        
        viewModelScope.launch {
            if (isOnline) {
                // Simulate network latency fluctuations
                launch {
                    while (_currentScreen.value == "lobby" || _currentScreen.value == "game") {
                        _pingMs.value = (35..110).random()
                        delay(2000)
                    }
                }

                // Simulate family members from different countries joining the lobby
                delay(1200)
                addLog("Conexión establecida. Sala: ${_roomCode.value}")
                
                // Fetch profile
                val profile = userProfile.value ?: UserProfileEntity()
                
                _roomPlayers.value = listOf(
                    Player(id = "human", name = profile.name, countryFlag = profile.countryFlag, isHuman = true)
                )

                delay(1500)
                addLog("Tía Sofía 🇪🇸 se ha unido a la sala.")
                _roomPlayers.value = _roomPlayers.value + Player(id = "ai_1", name = "Tía Sofía", countryFlag = "🇪🇸")
                sendSimulatedChat("Tía Sofía", "🇪🇸", "¡Hola a todos! Qué lindo jugar Rummy en familia.")

                delay(1800)
                addLog("Tío Wagner 🇺🇸 se ha unido a la sala.")
                _roomPlayers.value = _roomPlayers.value + Player(id = "ai_2", name = "Tío Wagner", countryFlag = "🇺🇸")
                sendSimulatedChat("Tío Wagner", "🇺🇸", "Hello family! ready to win this game.")

                delay(1200)
                addLog("Próximo jugador: Mamá 🇮🇹 se ha unido.")
                _roomPlayers.value = _roomPlayers.value + Player(id = "ai_3", name = "Mamá", countryFlag = "🇮🇹")
                sendSimulatedChat("Mamá", "🇮🇹", "¡Ciao! Preparé café mientras jugamos.")
            } else {
                // Offline Local AI
                val profile = userProfile.value ?: UserProfileEntity()
                _roomPlayers.value = listOf(
                    Player(id = "human", name = profile.name, countryFlag = profile.countryFlag, isHuman = true),
                    Player(id = "ai_1", name = "Sofía Bot", countryFlag = "🇪🇸"),
                    Player(id = "ai_2", name = "Wagner Bot", countryFlag = "🇺🇸"),
                    Player(id = "ai_3", name = "Mamá Bot", countryFlag = "🇮🇹")
                )
                addLog("Modo offline listo.")
            }
        }
    }

    // Sends a chat message
    fun sendChatMessage(msgText: String) {
        if (msgText.isBlank()) return
        val profile = userProfile.value ?: UserProfileEntity()
        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = profile.name,
            senderFlag = profile.countryFlag,
            message = msgText
        )
        _chatMessages.value = _chatMessages.value + newMessage

        // Simulate reactive responses from family members occasionally!
        if (_isOnlineMode.value) {
            viewModelScope.launch {
                delay(2000)
                val familyResponses = listOf(
                    Pair("Tía Sofía", Pair("🇪🇸", "¡Exacto!")),
                    Pair("Tío Wagner", Pair("🇺🇸", "Haha, good one!")),
                    Pair("Mamá", Pair("🇮🇹", "¡Mucha suerte! ☕"))
                )
                val randomResponder = familyResponses.random()
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderName = randomResponder.first,
                    senderFlag = randomResponder.second.first,
                    message = randomResponder.second.second
                )
            }
        }
    }

    private fun sendSimulatedChat(sender: String, flag: String, msg: String) {
        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = sender,
            senderFlag = flag,
            message = msg
        )
        _chatMessages.value = _chatMessages.value + newMessage
    }

    fun addLog(log: String) {
        _logs.value = _logs.value + log
    }

    // Starts the actual gameplay
    fun startGameplay() {
        val names = _roomPlayers.value.map { it.name }
        val flags = _roomPlayers.value.map { it.countryFlag }
        
        val session = GameEngine.startNewGame(
            mode = _gameMode.value,
            playerNames = names,
            playerFlags = flags,
            humanIndex = 0
        )
        _gameSession.value = session
        _selectedHandTiles.value = emptyList()
        _currentScreen.value = "game"

        // If AI's turn is first, run it (but usually player 0 is human)
        checkAndExecuteAi()
    }

    // User selects/unselects a tile in their hand
    fun toggleTileSelection(tile: Tile) {
        val currentSelected = _selectedHandTiles.value.toMutableList()
        if (currentSelected.any { it.id == tile.id }) {
            currentSelected.removeAll { it.id == tile.id }
        } else {
            currentSelected.add(tile)
        }
        _selectedHandTiles.value = currentSelected
    }

    fun clearTileSelections() {
        _selectedHandTiles.value = emptyList()
    }

    // Human Player draws a card from the Pool Pile
    fun drawTileFromPool() {
        val session = _gameSession.value ?: return
        if (session.currentPlayerIndex != 0 || session.turnPhase != TurnPhase.DRAW) return

        val updated = GameEngine.drawFromPool(session)
        _gameSession.value = updated
        addLog("Has robado una ficha de la pila.")
    }

    // Human Player draws/robs from discard pile
    fun drawTileFromDiscard() {
        val session = _gameSession.value ?: return
        if (session.currentPlayerIndex != 0 || session.turnPhase != TurnPhase.DRAW) return
        if (session.discardPile.isEmpty()) return

        val updated: GameSession
        if (session.mode == GameMode.BURACO) {
            // Rob entire discard pile
            updated = GameEngine.robDiscardPile(session)
            addLog("Has robado el pozo completo (${session.discardPile.size} fichas)!")
        } else {
            // Draw top card only
            updated = GameEngine.drawFromDiscard(session)
            addLog("Has tomado ${GameEngine.formatTile(session.discardPile.last())} del pozo.")
        }
        _gameSession.value = updated
    }

    // Melds a run (Escalera) from current selection
    fun meldSelectedAsRun() {
        val session = _gameSession.value ?: return
        if (session.currentPlayerIndex != 0 || session.turnPhase != TurnPhase.PLAY) return

        val selected = _selectedHandTiles.value
        if (selected.size < 3) {
            addLog("Selecciona al menos 3 fichas para formar una Escalera.")
            return
        }

        val (updatedSession, error) = GameEngine.meldFromPlayer(session, selected, Meld.MeldType.RUN)
        if (error != null) {
            addLog(error)
        } else {
            _gameSession.value = updatedSession
            _selectedHandTiles.value = emptyList()
            addLog("¡Has bajado una Escalera!")
        }
    }

    // Melds a set (Pierna) from current selection
    fun meldSelectedAsSet() {
        val session = _gameSession.value ?: return
        if (session.currentPlayerIndex != 0 || session.turnPhase != TurnPhase.PLAY) return

        val selected = _selectedHandTiles.value
        if (selected.size < 3) {
            addLog("Selecciona al menos 3 fichas para formar una Pierna.")
            return
        }

        val (updatedSession, error) = GameEngine.meldFromPlayer(session, selected, Meld.MeldType.SET)
        if (error != null) {
            addLog(error)
        } else {
            _gameSession.value = updatedSession
            _selectedHandTiles.value = emptyList()
            addLog("¡Has bajado una Pierna!")
        }
    }

    // Appends selected tile to a meld on table
    fun appendSelectedToMeld(meldId: String) {
        val session = _gameSession.value ?: return
        if (session.currentPlayerIndex != 0 || session.turnPhase != TurnPhase.PLAY) return

        val selected = _selectedHandTiles.value
        if (selected.size != 1) {
            addLog("Selecciona exactamente 1 ficha de tu mano para acomodar.")
            return
        }

        val (updatedSession, error) = GameEngine.appendTileToMeld(session, meldId, selected.first())
        if (error != null) {
            addLog(error)
        } else {
            _gameSession.value = updatedSession
            _selectedHandTiles.value = emptyList()
            addLog("Ficha acomodada exitosamente.")
        }
    }

    // Discard a tile from selection to finish turn
    fun discardSelectedTile(tile: Tile) {
        val session = _gameSession.value ?: return
        if (session.currentPlayerIndex != 0 || session.turnPhase == TurnPhase.DRAW) return

        val (updatedSession, error) = GameEngine.discardTile(session, tile)
        if (error != null) {
            addLog(error)
        } else {
            _gameSession.value = updatedSession
            _selectedHandTiles.value = emptyList()
            addLog("Has descartado: ${GameEngine.formatTile(tile)}.")
            
            // Trigger AI moves sequence
            checkAndExecuteAi()
        }
    }

    // Executes AIs sequentially with beautiful delay to simulate actual physical thinking and gameplay rhythm!
    private fun checkAndExecuteAi() {
        viewModelScope.launch {
            var session = _gameSession.value ?: return@launch
            while (session.currentPlayerIndex != 0 && session.winnerId == null) {
                delay(2000) // Aesthetic visual thinking pause
                session = GameEngine.executeAiTurn(session) { logMsg ->
                    addLog(logMsg)
                }
                _gameSession.value = session
            }
            
            // Round finished check
            if (session.winnerId != null) {
                handleRoundEnded(session)
            }
        }
    }

    private suspend fun handleRoundEnded(session: GameSession) {
        val humanPlayer = session.players.firstOrNull { it.isHuman } ?: return
        val isWin = session.winnerId == "human"
        
        // Save to Database Match History
        val matchResult = if (isWin) "WIN" else "LOSS"
        val opponents = session.players.filter { !it.isHuman }.joinToString(", ") { it.name }
        
        val newMatch = MatchHistoryEntity(
            mode = session.mode.name,
            score = humanPlayer.totalScore,
            result = matchResult,
            opponents = opponents
        )
        repository.addMatchToHistory(newMatch)

        // Update User Profile entity
        val currentProfile = userProfile.value ?: UserProfileEntity()
        val newWins = currentProfile.wins + (if (isWin) 1 else 0)
        val newLosses = currentProfile.losses + (if (isWin) 0 else 1)
        val newXp = currentProfile.xp + (if (isWin) 200 else 50) + (humanPlayer.totalScore.coerceAtLeast(0) / 10)
        val newLevel = 1 + (newXp / 150) // simple level up trigger

        repository.saveProfile(
            currentProfile.copy(
                wins = newWins,
                losses = newLosses,
                xp = newXp,
                level = newLevel
            )
        )
    }

    // Resets profile stats
    fun updateProfile(name: String, flag: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveProfile(current.copy(name = name, countryFlag = flag))
            addLog("Perfil actualizado.")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            addLog("Historial de partidas borrado.")
        }
    }
}
