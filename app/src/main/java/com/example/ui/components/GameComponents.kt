package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.GameEngine
import com.example.data.model.Meld
import com.example.data.model.Tile
import com.example.data.model.TileColor
import com.example.data.model.GameMode
import com.example.ui.theme.*

// 1. Physical 3D Ivory Tile Component
@Composable
fun TactileTile(
    tile: Tile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    scale: Float = 1.0f
) {
    val width = (42 * scale).dp
    val height = (58 * scale).dp

    // Color mapper based on tile settings
    val textColor = when (tile.color) {
        TileColor.RED -> TileRed
        TileColor.BLUE -> TileBlue
        TileColor.BLACK -> TileBlack
        TileColor.ORANGE -> TileOrange
        TileColor.JOKER -> TileJoker
    }

    Box(
        modifier = modifier
            .size(width, height)
            .shadow(
                elevation = if (isSelected) 10.dp else 4.dp,
                shape = RoundedCornerShape(6.dp),
                clip = false
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(TileIvoryHighlight, TileIvoryBase, TileIvoryShadow)
                ),
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0x33000000),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .testTag("tile_${tile.id}")
    ) {
        // Subtle physically molded inner border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .border(0.5.dp, Color(0x1F000000), RoundedCornerShape(4.dp))
        ) {
            // Elegant molded circular depression in center (characteristic of Rummikub tiles)
            Box(
                modifier = Modifier
                    .size((28 * scale).dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x0A000000), Color.Transparent),
                            radius = 60f
                        )
                    )
            ) {
                if (tile.isJoker) {
                    // Joker Face icon
                    Text(
                        text = "🤡",
                        fontSize = (16 * scale).sp,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Number display with bold design-oriented typography
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = tile.number.toString(),
                            color = textColor,
                            fontSize = (22 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = (22 * scale).sp
                        )
                        // Underline indicator for numbers 6 and 9 (crucial for physical tiles clarity)
                        if (tile.number == 6 || tile.number == 9) {
                            Box(
                                modifier = Modifier
                                    .width((12 * scale).dp)
                                    .height((1.5 * scale).dp)
                                    .background(textColor)
                            )
                        }
                    }
                }
            }
        }

        // Selected physical offset lift
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x1F000000), RoundedCornerShape(6.dp))
            )
        }
    }
}

// 2. Physical Mahogany Wood Scrabble-style Rack (Atril)
// Uses custom Compose drawBehind code to draw realistic rich wood textures, grains, and an ergonomic front-lip shadow.
@Composable
fun AtrilRack(
    tiles: List<Tile>,
    selectedTiles: List<Tile>,
    onTileClick: (Tile) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Background wood color gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(MahoganyLight, MahoganyMedium, MahoganyDark)
                    )
                )

                // Render physical wood grain lines dynamically for warmth and physical authenticity
                val randomSeed = 42
                for (i in 0..15) {
                    val y = (i * size.height / 15)
                    val offsetNoise = (i * 17) % 30
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(0f, y),
                        end = Offset(size.width, y + offsetNoise),
                        strokeWidth = 2f
                    )
                }

                // Front-lip shadow effect of Scrabble wooden holder
                drawRect(
                    color = Color(0x55000000),
                    size = Size(size.width, 10f),
                    topLeft = Offset(0f, size.height - 10f)
                )
            }
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag("atril_rack")
    ) {
        Column {
            // Friendly instruction prompt inside the rack wood margin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TU ATRIL (HASTA 20% ALTURA)",
                    color = MapleWood,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (tiles.isEmpty()) {
                    Text(
                        text = "VACÍO - ¡Compra el Muerto!",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                } else {
                    Text(
                        text = "${tiles.size} fichas",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }

            // Horizontal scrolling tray mimicking physical tray slot placement
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tiles.forEach { tile ->
                    val isSelected = selectedTiles.any { it.id == tile.id }
                    TactileTile(
                        tile = tile,
                        isSelected = isSelected,
                        onClick = { onTileClick(tile) }
                    )
                }
            }
        }
    }
}

// 3. Stacked Physical Draw Pile (Pila) and Discard Pile (Pozo)
@Composable
fun DrawPilesSection(
    poolCount: Int,
    discardPile: List<Tile>,
    turnPhase: com.example.data.model.TurnPhase,
    isYourTurn: Boolean,
    onDrawPool: () -> Unit,
    onDrawDiscard: () -> Unit,
    mode: GameMode,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // A. Pila de Robar (Face Down Deck Stack)
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(110.dp)
                .clickable(enabled = isYourTurn && turnPhase == com.example.data.model.TurnPhase.DRAW) { onDrawPool() }
                .testTag("draw_pool_button")
        ) {
            // 3D overlapping stack effect of cards
            repeat((poolCount / 10).coerceAtMost(5)) { index ->
                val offset = (index * 3).dp
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = offset, top = offset)
                        .shadow(3.dp, RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(MahoganyMedium, MahoganyDark)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(1.5.dp, AmberGold, RoundedCornerShape(8.dp))
                ) {
                    // Back design pattern
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0x33FFB300),
                            radius = size.minDimension / 4,
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }
                }
            }

            // Count label on top stack
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ROBAR",
                    color = AmberGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$poolCount",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "pila",
                    color = Color.LightGray,
                    fontSize = 9.sp
                )
            }
        }

        // B. Pozo de Descarte (Face Up Discard Stack)
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(110.dp)
                .clickable(enabled = isYourTurn && turnPhase == com.example.data.model.TurnPhase.DRAW && discardPile.isNotEmpty()) { onDrawDiscard() }
                .testTag("draw_discard_button")
        ) {
            if (discardPile.isEmpty()) {
                // Empty well placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        .background(Color(0x1F000000), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pozo Vacío",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Stack of discard tiles
                val topTile = discardPile.last()
                val isBurako = mode == GameMode.BURACO

                // Draw background stacked shadows
                repeat((discardPile.size / 3).coerceAtMost(3)) { index ->
                    val offset = (index * 2).dp
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = offset, bottom = offset)
                            .background(Color(0x55000000), RoundedCornerShape(8.dp))
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(5.dp, RoundedCornerShape(8.dp))
                        .background(Color(0x33000000), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Render physical top tile
                        TactileTile(
                            tile = topTile,
                            onClick = { onDrawDiscard() },
                            scale = 0.9f
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = if (isBurako) "ROBAR TODO" else "TOMAR UNO",
                            color = if (isYourTurn && turnPhase == com.example.data.model.TurnPhase.DRAW) AmberGold else Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pozo (${discardPile.size})",
                            color = Color.LightGray,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}

// 4. Physical Board Meld Group
@Composable
fun MeldTableGroup(
    meld: Meld,
    onAppend: () -> Unit,
    canAppend: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .shadow(3.dp, RoundedCornerShape(8.dp))
            .clickable(enabled = canAppend, onClick = onAppend)
            .testTag("meld_group_${meld.id}"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FFFFFF)
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (canAppend) borderStrokeAmber() else null
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (meld.type == Meld.MeldType.RUN) "Escalera" else "Pierna",
                    color = AmberGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )
                
                if (meld.isCanasta()) {
                    Text(
                        text = "🏆 Canasta",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Horizontal layout of cards in the meld
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                meld.tiles.forEach { tile ->
                    TactileTile(
                        tile = tile,
                        onClick = {}, // clicked via group overlay
                        scale = 0.65f
                    )
                }
            }

            if (canAppend) {
                Text(
                    text = "+ ACOMODAR",
                    color = AmberGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// Helper boundary stroke decoration
@Composable
private fun borderStrokeAmber() = androidx.compose.foundation.BorderStroke(
    width = 1.5.dp,
    color = AmberGold
)
