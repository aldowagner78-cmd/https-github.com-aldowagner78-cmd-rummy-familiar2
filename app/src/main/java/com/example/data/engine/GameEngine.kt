package com.example.data.engine

import com.example.data.model.*
import java.util.UUID
import kotlin.random.Random

object GameEngine {

    // Generates a standard deck of 104 tiles (2 sets of 1-13 in 4 colors) + 2 Jokers = 106 tiles
    fun generateDeck(): List<Tile> {
        val deck = mutableListOf<Tile>()
        var currentId = 1

        // Colors: Red, Blue, Black, Orange
        val colors = listOf(TileColor.RED, TileColor.BLUE, TileColor.BLACK, TileColor.ORANGE)

        // Repeat twice (2 identical sets)
        repeat(2) {
            for (color in colors) {
                for (number in 1..13) {
                    deck.add(
                        Tile(
                            id = currentId++,
                            number = number,
                            color = color,
                            isJoker = false
                        )
                    )
                }
            }
        }

        // Add 2 Jokers
        repeat(2) {
            deck.add(
                Tile(
                    id = currentId++,
                    number = 0,
                    color = TileColor.JOKER,
                    isJoker = true
                )
            )
        }

        return deck.shuffled()
    }

    // Starts a new game session
    fun startNewGame(
        mode: GameMode,
        playerNames: List<String>,
        playerFlags: List<String>,
        humanIndex: Int = 0
    ): GameSession {
        val deck = generateDeck().toMutableList()
        val players = mutableListOf<Player>()

        // Deal 11 tiles to each player (Standard Burako / Rummy hand)
        val handSize = 11

        playerNames.forEachIndexed { index, name ->
            val hand = mutableListOf<Tile>()
            repeat(handSize) {
                if (deck.isNotEmpty()) {
                    hand.add(deck.removeAt(0))
                }
            }
            players.add(
                Player(
                    id = if (index == humanIndex) "human" else "ai_$index",
                    name = name,
                    countryFlag = playerFlags.getOrElse(index) { "🇦🇷" },
                    hand = hand.sortedWith(compareBy({ it.color }, { it.number })),
                    isHuman = (index == humanIndex),
                    isBoughtMuerto = false,
                    totalScore = 0,
                    roundScore = 0
                )
            )
        }

        // In Burako, we set aside two "Muertos" of 11 tiles each
        val muerto1 = mutableListOf<Tile>()
        val muerto2 = mutableListOf<Tile>()
        if (mode == GameMode.BURACO) {
            repeat(11) {
                if (deck.isNotEmpty()) muerto1.add(deck.removeAt(0))
            }
            repeat(11) {
                if (deck.isNotEmpty()) muerto2.add(deck.removeAt(0))
            }
        }

        // Face up top card for discard pile
        val discardPile = mutableListOf<Tile>()
        if (deck.isNotEmpty()) {
            discardPile.add(deck.removeAt(0))
        }

        return GameSession(
            mode = mode,
            players = players,
            pool = deck,
            discardPile = discardPile,
            meldedGroups = emptyList(),
            currentPlayerIndex = 0,
            turnPhase = TurnPhase.DRAW,
            muerto1 = muerto1,
            muerto2 = muerto2,
            muerto1Bought = false,
            muerto2Bought = false,
            winnerId = null,
            roundLog = listOf("¡El juego de ${mode.name} ha comenzado! Turno de ${playerNames[0]}.")
        )
    }

    // AI logic: Evaluates the AI's hand and plays automatically
    fun executeAiTurn(session: GameSession, onLog: (String) -> Unit): GameSession {
        var currentSession = session
        val activePlayer = currentSession.players[currentSession.currentPlayerIndex]
        
        if (activePlayer.isHuman || currentSession.winnerId != null) return currentSession

        val playerNameWithFlag = "${activePlayer.countryFlag} ${activePlayer.name}"
        onLog("Pensando turno para $playerNameWithFlag...")

        // --- 1. DRAW PHASE ---
        // Decides whether to draw from the pool (pile) or rob the discard pile (pozo)
        val discardTop = currentSession.discardPile.lastOrNull()
        var robbedPozo = false

        if (discardTop != null && currentSession.mode == GameMode.BURACO) {
            // Burako rule: Robbing the discard pile takes the ENTIRE pile.
            // AI will rob if the top card is useful (matches a set or run in hand) or if the pile has many useful cards
            val topUseful = isTileUseful(activePlayer.hand, discardTop, currentSession.mode)
            if (topUseful || currentSession.discardPile.size >= 4) {
                robbedPozo = true
                onLog("$playerNameWithFlag robó el pozo entero (${currentSession.discardPile.size} fichas)!")
                currentSession = robDiscardPile(currentSession)
            }
        } else if (discardTop != null && currentSession.mode == GameMode.RUMMY) {
            // Rummy: Just draw the top of the discard pile if it completes a meld
            val completesMeld = isTileUseful(activePlayer.hand, discardTop, currentSession.mode)
            if (completesMeld) {
                robbedPozo = true
                onLog("$playerNameWithFlag tomó ${formatTile(discardTop)} del pozo.")
                currentSession = drawFromDiscard(currentSession)
            }
        }

        if (!robbedPozo) {
            // Normal draw from face-down pile
            if (currentSession.pool.isNotEmpty()) {
                onLog("$playerNameWithFlag robó una ficha de la pila.")
                currentSession = drawFromPool(currentSession)
            } else {
                // Pile empty: End round
                onLog("La pila se ha agotado. Fin de la ronda.")
                return endRound(currentSession, onLog)
            }
        }

        // Update active player reference after drawing
        var player = currentSession.players[currentSession.currentPlayerIndex]

        // --- 2. PLAY/MELD PHASE ---
        // AI scans its hand for valid melds, then lays them down on the table
        val potentialMelds = findValidMelds(player.hand, currentSession.mode)
        var updatedMelds = currentSession.meldedGroups.toMutableList()
        var currentHand = player.hand.toMutableList()

        if (potentialMelds.isNotEmpty()) {
            potentialMelds.forEach { meld ->
                // Check if tiles are still in hand (to avoid duplicate usage)
                if (currentHand.containsAll(meld.tiles)) {
                    currentHand.removeAll(meld.tiles)
                    updatedMelds.add(meld)
                    onLog("$playerNameWithFlag bajó un juego: ${formatMeld(meld)}")
                }
            }
        }

        // Try to add remaining tiles in hand to existing melds on the table
        val meldsAfterAdditions = mutableListOf<Meld>()
        for (meld in updatedMelds) {
            var tempMeld = meld
            val tilesToAdd = mutableListOf<Tile>()
            for (tile in currentHand) {
                val newTiles = tempMeld.tiles + tile
                if (isValidMeldGroup(newTiles, tempMeld.type, currentSession.mode)) {
                    tilesToAdd.add(tile)
                    tempMeld = tempMeld.copy(tiles = newTiles)
                    onLog("$playerNameWithFlag acomodó ${formatTile(tile)} en la mesa.")
                }
            }
            currentHand.removeAll(tilesToAdd)
            meldsAfterAdditions.add(tempMeld)
        }
        updatedMelds = meldsAfterAdditions.toMutableList()

        // Apply hand updates to active player
        player = player.copy(hand = currentHand.sortedWith(compareBy({ it.color }, { it.number })))
        var updatedPlayers = currentSession.players.toMutableList()
        updatedPlayers[currentSession.currentPlayerIndex] = player
        currentSession = currentSession.copy(meldedGroups = updatedMelds, players = updatedPlayers)

        // --- 3. MUERTO BUY CHECK (Burako) ---
        // If hand is empty, buy Muerto
        if (currentSession.mode == GameMode.BURACO && player.hand.isEmpty() && !player.isBoughtMuerto) {
            currentSession = buyMuertoForCurrentPlayer(currentSession, onLog)
            player = currentSession.players[currentSession.currentPlayerIndex]
        }

        // --- 4. DISCARD PHASE ---
        // Select a tile to discard (AI discards a card that is least connected or lowest value if disconnected)
        currentHand = player.hand.toMutableList()
        if (currentHand.isNotEmpty()) {
            val discardTile = selectTileToDiscard(currentHand, currentSession.mode)
            currentHand.remove(discardTile)
            player = player.copy(hand = currentHand.sortedWith(compareBy({ it.color }, { it.number })))
            updatedPlayers = currentSession.players.toMutableList()
            updatedPlayers[currentSession.currentPlayerIndex] = player

            val updatedDiscard = currentSession.discardPile.toMutableList()
            updatedDiscard.add(discardTile)

            onLog("$playerNameWithFlag descartó ${formatTile(discardTile)}")
            
            currentSession = currentSession.copy(
                players = updatedPlayers,
                discardPile = updatedDiscard,
                turnPhase = TurnPhase.DRAW
            )
        }

        // Check if round closed (Winner check)
        if (player.hand.isEmpty()) {
            // If Burako, must have a Canasta to close!
            val teamHasCanasta = updatedMelds.any { it.isCanasta() } // simple individual rule
            if (currentSession.mode == GameMode.RUMMY || teamHasCanasta) {
                onLog("¡$playerNameWithFlag ha cerrado la partida!")
                return endRound(currentSession, onLog)
            }
        }

        // Advance turn to next player
        val nextIndex = (currentSession.currentPlayerIndex + 1) % currentSession.players.size
        onLog("Turno de ${currentSession.players[nextIndex].countryFlag} ${currentSession.players[nextIndex].name}.")
        return currentSession.copy(
            currentPlayerIndex = nextIndex,
            turnPhase = TurnPhase.DRAW
        )
    }

    // Evaluates if a card is useful (helps complete a run or set)
    private fun isTileUseful(hand: List<Tile>, tile: Tile, mode: GameMode): Boolean {
        if (tile.isJoker || (mode == GameMode.BURACO && tile.number == 2)) return true
        
        // Check if matches same color close numbers for Escalera
        val sameColor = hand.filter { it.color == tile.color }
        if (sameColor.any { kotlin.math.abs(it.number - tile.number) <= 2 }) return true

        // Check if matches same numbers for Pierna
        val sameNumber = hand.filter { it.number == tile.number }
        if (sameNumber.size >= 2) return true

        return false
    }

    private fun selectTileToDiscard(hand: List<Tile>, mode: GameMode): Tile {
        // Simple heuristic: avoid discarding wildcards if possible
        val nonWildcards = hand.filter { !it.isWildcard(mode) }
        if (nonWildcards.isEmpty()) return hand.first()

        // Find tiles that do not form pairs or sequences
        for (tile in nonWildcards) {
            val hasSequence = hand.any { it.color == tile.color && kotlin.math.abs(it.number - tile.number) == 1 }
            val hasSameNumber = hand.any { it.id != tile.id && it.number == tile.number }
            if (!hasSequence && !hasSameNumber) {
                return tile
            }
        }

        // Otherwise return highest number card
        return nonWildcards.maxByOrNull { it.number } ?: hand.first()
    }

    // Drawing from pool pile
    fun drawFromPool(session: GameSession): GameSession {
        val pool = session.pool.toMutableList()
        if (pool.isEmpty()) return session

        val drawn = pool.removeAt(0)
        val players = session.players.toMutableList()
        val currentPlayer = players[session.currentPlayerIndex]
        val updatedHand = (currentPlayer.hand + drawn).sortedWith(compareBy({ it.color }, { it.number }))

        players[session.currentPlayerIndex] = currentPlayer.copy(hand = updatedHand)

        return session.copy(
            pool = pool,
            players = players,
            turnPhase = TurnPhase.PLAY
        )
    }

    // Drawing a single tile from discard pile
    fun drawFromDiscard(session: GameSession): GameSession {
        val discard = session.discardPile.toMutableList()
        if (discard.isEmpty()) return session

        val drawn = discard.removeAt(discard.size - 1)
        val players = session.players.toMutableList()
        val currentPlayer = players[session.currentPlayerIndex]
        val updatedHand = (currentPlayer.hand + drawn).sortedWith(compareBy({ it.color }, { it.number }))

        players[session.currentPlayerIndex] = currentPlayer.copy(hand = updatedHand)

        return session.copy(
            discardPile = discard,
            players = players,
            turnPhase = TurnPhase.PLAY
        )
    }

    // Robbing the ENTIRE discard pile (Burako characteristic)
    fun robDiscardPile(session: GameSession): GameSession {
        val discard = session.discardPile.toMutableList()
        if (discard.isEmpty()) return session

        val players = session.players.toMutableList()
        val currentPlayer = players[session.currentPlayerIndex]
        val updatedHand = (currentPlayer.hand + discard).sortedWith(compareBy({ it.color }, { it.number }))

        players[session.currentPlayerIndex] = currentPlayer.copy(hand = updatedHand)

        return session.copy(
            discardPile = emptyList(),
            players = players,
            turnPhase = TurnPhase.PLAY
        )
    }

    // Places a new meld on the table
    fun meldFromPlayer(session: GameSession, selectedTiles: List<Tile>, type: Meld.MeldType): Pair<GameSession, String?> {
        val players = session.players.toMutableList()
        val currentPlayer = players[session.currentPlayerIndex]

        // Validate selection exist in hand
        if (!currentPlayer.hand.containsAll(selectedTiles)) {
            return Pair(session, "Error: Algunas fichas seleccionadas no están en tu mano.")
        }

        // Validate valid meld structure
        if (!isValidMeldGroup(selectedTiles, type, session.mode)) {
            return Pair(session, "Error: Combinación inválida para un ${if (type == Meld.MeldType.RUN) "Escalera" else "Pierna"}.")
        }

        // Remove from hand, add to table
        val updatedHand = currentPlayer.hand.toMutableList()
        selectedTiles.forEach { tile ->
            updatedHand.remove(tile)
        }

        val newMeld = Meld(
            id = UUID.randomUUID().toString(),
            tiles = selectedTiles.sortedBy { it.number },
            type = type
        )

        val updatedMelds = session.meldedGroups + newMeld
        players[session.currentPlayerIndex] = currentPlayer.copy(hand = updatedHand.sortedWith(compareBy({ it.color }, { it.number })))

        var updatedSession = session.copy(
            players = players,
            meldedGroups = updatedMelds
        )

        // Check Muerto Trigger (Burako)
        if (session.mode == GameMode.BURACO && updatedHand.isEmpty() && !currentPlayer.isBoughtMuerto) {
            updatedSession = buyMuertoForCurrentPlayer(updatedSession) { /* log ignored */ }
        }

        return Pair(updatedSession, null)
    }

    // Appends a single tile to an existing meld on the table
    fun appendTileToMeld(session: GameSession, meldId: String, tile: Tile): Pair<GameSession, String?> {
        val players = session.players.toMutableList()
        val currentPlayer = players[session.currentPlayerIndex]

        if (!currentPlayer.hand.contains(tile)) {
            return Pair(session, "Ficha no encontrada en la mano.")
        }

        val melds = session.meldedGroups.toMutableList()
        val meldIndex = melds.indexOfFirst { it.id == meldId }
        if (meldIndex == -1) return Pair(session, "Juego de mesa no encontrado.")

        val targetMeld = melds[meldIndex]
        val newTiles = (targetMeld.tiles + tile).sortedBy { it.number }

        if (!isValidMeldGroup(newTiles, targetMeld.type, session.mode)) {
            return Pair(session, "Esa ficha no encaja en este juego.")
        }

        // Update hand and meld
        val updatedHand = currentPlayer.hand.toMutableList()
        updatedHand.remove(tile)

        melds[meldIndex] = targetMeld.copy(tiles = newTiles)
        players[session.currentPlayerIndex] = currentPlayer.copy(hand = updatedHand.sortedWith(compareBy({ it.color }, { it.number })))

        var updatedSession = session.copy(
            players = players,
            meldedGroups = melds
        )

        // Check Muerto Trigger (Burako)
        if (session.mode == GameMode.BURACO && updatedHand.isEmpty() && !currentPlayer.isBoughtMuerto) {
            updatedSession = buyMuertoForCurrentPlayer(updatedSession) { /* log ignored */ }
        }

        return Pair(updatedSession, null)
    }

    // Handle Discard ending phase
    fun discardTile(session: GameSession, tile: Tile): Pair<GameSession, String?> {
        val players = session.players.toMutableList()
        val currentPlayer = players[session.currentPlayerIndex]

        if (!currentPlayer.hand.contains(tile)) {
            return Pair(session, "La ficha no está en tu mano.")
        }

        val updatedHand = currentPlayer.hand.toMutableList()
        updatedHand.remove(tile)

        players[session.currentPlayerIndex] = currentPlayer.copy(hand = updatedHand.sortedWith(compareBy({ it.color }, { it.number })))
        val discard = session.discardPile + tile

        var updatedSession = session.copy(
            players = players,
            discardPile = discard,
            turnPhase = TurnPhase.DRAW
        )

        // Check if hand empty - close game!
        if (updatedHand.isEmpty()) {
            val hasCanasta = session.meldedGroups.any { it.isCanasta() }
            if (session.mode == GameMode.RUMMY || hasCanasta) {
                // Win trigger
                updatedSession = endRound(updatedSession) { /* log */ }
                return Pair(updatedSession, "¡Ganaste la ronda!")
            }
        }

        // Next turn
        val nextIndex = (session.currentPlayerIndex + 1) % session.players.size
        updatedSession = updatedSession.copy(
            currentPlayerIndex = nextIndex
        )

        return Pair(updatedSession, null)
    }

    // Purchases "Muerto" for current player if available
    private fun buyMuertoForCurrentPlayer(session: GameSession, onLog: (String) -> Unit = {}): GameSession {
        val players = session.players.toMutableList()
        val currentPlayer = players[session.currentPlayerIndex]

        if (currentPlayer.isBoughtMuerto) return session

        val muertoTiles: List<Tile>
        var m1 = session.muerto1Bought
        var m2 = session.muerto2Bought

        if (!m1) {
            muertoTiles = session.muerto1
            m1 = true
            onLog("${currentPlayer.countryFlag} ${currentPlayer.name} compró el Muerto 1!")
        } else if (!m2) {
            muertoTiles = session.muerto2
            m2 = true
            onLog("${currentPlayer.countryFlag} ${currentPlayer.name} compró el Muerto 2!")
        } else {
            onLog("No quedan Muertos disponibles.")
            return session
        }

        players[session.currentPlayerIndex] = currentPlayer.copy(
            hand = (currentPlayer.hand + muertoTiles).sortedWith(compareBy({ it.color }, { it.number })),
            isBoughtMuerto = true
        )

        return session.copy(
            players = players,
            muerto1Bought = m1,
            muerto2Bought = m2
        )
    }

    // Validates if a group of tiles forms a valid Escalera (RUN) or Pierna (SET)
    fun isValidMeldGroup(tiles: List<Tile>, type: Meld.MeldType, mode: GameMode): Boolean {
        if (tiles.size < 3) return false

        // In Rummy, no duplicates can exist in a set/run, let's look at colors and wildcards
        val wildcards = tiles.filter { it.isWildcard(mode) }
        val normals = tiles.filter { !it.isWildcard(mode) }

        // Rules state that you cannot have more wildcards than normal cards, usually at most 1 or 2 wildcards in a meld.
        // Let's enforce max 2 wildcards or max 50% wildcards to keep the game balanced and classic.
        if (wildcards.size > tiles.size / 2) return false

        if (type == Meld.MeldType.SET) {
            // Pierna: same number, different/any colors.
            if (normals.isEmpty()) return true
            val targetNumber = normals.first().number
            // All normal tiles must have the same number!
            if (normals.any { it.number != targetNumber }) return false

            // In Rummy, a Pierna (set) must have DIFFERENT colors for each tile!
            if (mode == GameMode.RUMMY) {
                val colors = normals.map { it.color }
                if (colors.size != colors.distinct().size) return false
            }
            return tiles.size <= 4 // Max size of set is 4 in classic Rummy
        } else {
            // Run (Escalera): same color, consecutive numbers.
            if (normals.isEmpty()) return true
            val targetColor = normals.first().color
            // All normal tiles must be of the same color!
            if (normals.any { it.color != targetColor }) return false

            // Check consecutiveness with wildcard placeholders
            val sortedNormals = normals.sortedBy { it.number }
            
            // Check no duplicates in numbers
            if (sortedNormals.map { it.number }.distinct().size != sortedNormals.size) return false

            // Ensure spacing matches available wildcards
            var neededWildcards = 0
            for (i in 0 until sortedNormals.size - 1) {
                val diff = sortedNormals[i+1].number - sortedNormals[i].number
                if (diff <= 0) return false
                neededWildcards += (diff - 1)
            }

            return neededWildcards <= wildcards.size
        }
    }

    // Calculate score details and close round
    fun endRound(session: GameSession, onLog: (String) -> Unit = {}): GameSession {
        val players = session.players.map { player ->
            // Melted score minus hand score
            val playerMelds = session.meldedGroups.filter { /* simplified for individual play */ true } // In simple offline mode, we count all melds for simplicity or track who played what.
            // Let's calculate:
            // For simplicity, we add the points of all melded groups that are on the board to everyone who has cards or just the round score.
            // Let's award positive points for down sets, and subtract hand cards.
            var meldedPoints = session.meldedGroups.sumOf { it.getPoints(session.mode) }
            
            // A player gets positive points if they made moves or we just calculate individual hands.
            // Let's make it fully realistic:
            // The player who closed gets a bonus of 100 points.
            // Players get points for the tiles they placed, and subtract points of tiles in hand.
            val handPenalty = player.hand.sumOf { it.getPoints(session.mode) }
            
            // In a simple simulation:
            // Human player gets score of their lay downs minus hand.
            val isCloser = (player.hand.isEmpty())
            val closureBonus = if (isCloser) 100 else 0
            
            val roundScore = (if (player.isHuman) meldedPoints else (meldedPoints * 0.7).toInt()) + closureBonus - handPenalty
            val totalScore = player.totalScore + roundScore

            player.copy(
                roundScore = roundScore,
                totalScore = totalScore
            )
        }

        // Find winner of the round (highest total score)
        val winner = players.maxByOrNull { it.totalScore }
        onLog("Ronda terminada. Ganador de la ronda: ${winner?.name} con ${winner?.totalScore} puntos!")

        return session.copy(
            players = players,
            winnerId = winner?.id,
            turnPhase = TurnPhase.DRAW
        )
    }

    // Helpers to print/format objects
    fun formatTile(tile: Tile): String {
        if (tile.isJoker) return "🃏 Comodín"
        val colorSymbol = when (tile.color) {
            TileColor.RED -> "🔴"
            TileColor.BLUE -> "🔵"
            TileColor.BLACK -> "⚫"
            TileColor.ORANGE -> "🟠"
            TileColor.JOKER -> "🃏"
        }
        return "$colorSymbol ${tile.number}"
    }

    private fun findValidMelds(hand: List<Tile>, mode: GameMode): List<Meld> {
        val results = mutableListOf<Meld>()
        
        // 1. Group by number to find Sets (Piernas)
        val byNumber = hand.filter { !it.isWildcard(mode) && !it.isJoker }.groupBy { it.number }
        byNumber.forEach { (num, tiles) ->
            if (tiles.size >= 3) {
                results.add(Meld(
                    id = UUID.randomUUID().toString(),
                    tiles = tiles.take(4),
                    type = Meld.MeldType.SET
                ))
            }
        }
        
        // 2. Group by color to find Runs (Escaleras)
        val byColor = hand.filter { !it.isWildcard(mode) && !it.isJoker }.groupBy { it.color }
        byColor.forEach { (color, tiles) ->
            val sorted = tiles.sortedBy { it.number }
            val currentRun = mutableListOf<Tile>()
            for (tile in sorted) {
                if (currentRun.isEmpty()) {
                    currentRun.add(tile)
                } else {
                    if (tile.number == currentRun.last().number + 1) {
                        currentRun.add(tile)
                    } else if (tile.number != currentRun.last().number) {
                        if (currentRun.size >= 3) {
                            results.add(Meld(
                                id = UUID.randomUUID().toString(),
                                tiles = currentRun.toList(),
                                type = Meld.MeldType.RUN
                            ))
                        }
                        currentRun.clear()
                        currentRun.add(tile)
                    }
                }
            }
            if (currentRun.size >= 3) {
                results.add(Meld(
                    id = UUID.randomUUID().toString(),
                    tiles = currentRun.toList(),
                    type = Meld.MeldType.RUN
                ))
            }
        }
        
        return results
    }

    fun formatMeld(meld: Meld): String {
        val tilesStr = meld.tiles.joinToString(" ") { formatTile(it) }
        return "${if (meld.type == Meld.MeldType.RUN) "Escalera" else "Pierna"} ($tilesStr)"
    }
}

// Holds the complete game state during gameplay
data class GameSession(
    val mode: GameMode,
    val players: List<Player>,
    val pool: List<Tile>,
    val discardPile: List<Tile>,
    val meldedGroups: List<Meld>,
    val currentPlayerIndex: Int,
    val turnPhase: TurnPhase,
    val muerto1: List<Tile>,
    val muerto2: List<Tile>,
    val muerto1Bought: Boolean,
    val muerto2Bought: Boolean,
    val winnerId: String?,
    val roundLog: List<String> = emptyList()
)
