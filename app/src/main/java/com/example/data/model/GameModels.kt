package com.example.data.model


enum class TileColor {
    RED, BLUE, BLACK, ORANGE, JOKER
}

data class Tile(
    val id: Int,
    val number: Int, // 1 to 13. Or 0/99 for Joker
    val color: TileColor,
    val isJoker: Boolean = false,
    val isTempSelected: Boolean = false
) {
    // Check if tile functions as a wildcard in the current game mode
    fun isWildcard(mode: GameMode): Boolean {
        if (isJoker) return true
        // Under Burako rules, all number 2s are wildcards (comodines)
        if (mode == GameMode.BURACO && number == 2) return true
        return false
    }

    // Point values based on Burako rules (as shown in the user's manual screenshot):
    // No. 1: 15 pts, No. 2: 20 pts, No. 3-7: 5 pts, No. 8-13: 10 pts, Comodín: 50 pts
    fun getPoints(mode: GameMode): Int {
        if (isJoker) return 50
        if (mode == GameMode.BURACO) {
            return when (number) {
                1 -> 15
                2 -> 20
                in 3..7 -> 5
                in 8..13 -> 10
                else -> 10
            }
        } else {
            // Rummy classic values
            return when (number) {
                1 -> 10
                in 2..10 -> number
                in 11..13 -> 10
                else -> 10
            }
        }
    }
}

enum class GameMode {
    RUMMY, BURACO
}

enum class TurnPhase {
    DRAW, PLAY, DISCARD
}

data class Meld(
    val id: String,
    val tiles: List<Tile>,
    val type: MeldType
) {
    enum class MeldType {
        RUN, // Escalera (same color, sequential numbers)
        SET  // Pierna (same number, different/any colors depending on rules)
    }

    // Checks whether this is a Canasta (7 or more tiles in Burako)
    fun isCanasta(): Boolean = tiles.size >= 7

    // A Canasta is Pure (Pura) if it has NO wildcards (No Jokers, and in Burako no number 2s used as wildcards)
    fun isPure(mode: GameMode): Boolean {
        if (!isCanasta()) return false
        // For Burako, we check if any wildcard is present.
        // Wait, under Burako, can a number 2 be part of an Escalera in its natural position?
        // Yes, e.g., 1-2-3-4 of Blue. In that case, the '2' of Blue is NOT a wildcard, it's in its natural place!
        // To keep it simple and robust, we check if there are any Jokers, or if any number 2 is placed in an index
        // that doesn't match its natural sequential number, or if there's any duplicate color/number.
        // Let's implement a clean check.
        if (tiles.any { it.isJoker }) return false
        
        if (mode == GameMode.BURACO) {
            // If there's a 2 of a different color or placed as a wildcard
            // Let's see if the run is purely consecutive without wildcards.
            if (type == MeldType.SET) {
                // For a set (Pierna), all tiles must have the same number.
                // If there is any 2, and the set number is NOT 2, then the 2 is used as a wildcard!
                val nonWildcardTiles = tiles.filter { it.number != 2 || tiles.all { t -> t.number == 2 } }
                if (nonWildcardTiles.size < tiles.size) return false
            } else {
                // For a run (Escalera), let's see if there is any 2 being used as a wildcard.
                // If all tiles are naturally consecutive of the same color:
                val sorted = tiles.sortedBy { it.number }
                var isConsecutive = true
                for (i in 0 until sorted.size - 1) {
                    if (sorted[i+1].number - sorted[i].number != 1) {
                        isConsecutive = false
                        break
                    }
                }
                if (!isConsecutive) return false
            }
        }
        return true
    }
    
    fun getPoints(mode: GameMode): Int {
        var pts = tiles.sumOf { it.getPoints(mode) }
        if (mode == GameMode.BURACO && isCanasta()) {
            pts += if (isPure(mode)) 200 else 100
        }
        return pts
    }
}

data class Player(
    val id: String,
    val name: String,
    val avatarUrl: String = "",
    val countryFlag: String = "🇺🇸",
    val hand: List<Tile> = emptyList(),
    val isHuman: Boolean = false,
    val isBoughtMuerto: Boolean = false,
    val totalScore: Int = 0,
    val roundScore: Int = 0,
    val isReady: Boolean = true
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderFlag: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
