package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MatchHistoryEntity
import com.example.data.database.UserProfileEntity
import com.example.data.model.GameMode
import com.example.data.model.Meld
import com.example.data.model.TurnPhase
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

// --- 1. MAIN MENU SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    profile: UserProfileEntity,
    modifier: Modifier = Modifier
) {
    var editName by remember { mutableStateOf(profile.name) }
    var selectedFlag by remember { mutableStateOf(profile.countryFlag) }
    val flags = listOf("🇦🇷", "🇪🇸", "🇺🇸", "🇮🇹", "🇲🇽", "🇧🇷", "🇨🇴", "🇺🇾")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FeltGreenLight, FeltGreenMedium, FeltGreenDark)
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Premium Logo Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🏆 ",
                    fontSize = 32.sp
                )
                Text(
                    text = "BURAKO & RUMMY",
                    color = AmberGold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
            }
            Text(
                text = "FAMILY CLUB",
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Dynamic Level Badge
            Card(
                colors = CardDefaults.cardColors(containerColor = MahoganyDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⭐ Nivel ${profile.level}", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "${profile.wins} Victorias / ${profile.losses} Derrotas", color = Color.White, fontSize = 12.sp)
                }
            }

            // A. Profile Setup Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TU PERFIL FAMILIAR",
                        color = AmberGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre / Apodo", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Selecciona Bandera (País): $selectedFlag",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        flags.forEach { flag ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedFlag == flag) AmberGold else Color(0x1F000000))
                                    .clickable { selectedFlag = flag }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = flag, fontSize = 24.sp)
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.updateProfile(editName, selectedFlag) },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("save_profile_button")
                    ) {
                        Text("Guardar Perfil", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // B. Room Code setup
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONFIGURAR SALA FAMILIAR",
                        color = AmberGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val roomState by viewModel.roomCode.collectAsState()
                    OutlinedTextField(
                        value = roomState,
                        onValueChange = { viewModel.setRoomCode(it) },
                        label = { Text("Código de Sala (ej: FAMILIA77)", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_code_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Game Mode Picker toggle
                        val currentMode by viewModel.gameMode.collectAsState()
                        Button(
                            onClick = { viewModel.setGameMode(GameMode.BURACO) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentMode == GameMode.BURACO) AmberGold else Color.DarkGray,
                                contentColor = if (currentMode == GameMode.BURACO) Color.Black else Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Modo BURACO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.setGameMode(GameMode.RUMMY) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentMode == GameMode.RUMMY) AmberGold else Color.DarkGray,
                                contentColor = if (currentMode == GameMode.RUMMY) Color.Black else Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Modo RUMMY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // C. Actions
            Button(
                onClick = { viewModel.createOrJoinRoom(isOnline = true) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(vertical = 4.dp)
                    .testTag("play_online_button")
            ) {
                Icon(Icons.Default.Groups, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CONECTAR CON LA FAMILIA (Online)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Button(
                onClick = { viewModel.createOrJoinRoom(isOnline = false) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(vertical = 4.dp)
                    .testTag("play_offline_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("JUGAR CON BOTS (Offline)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo("rules") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rules_button")
                ) {
                    Icon(Icons.Default.Book, contentDescription = null, sizeModifier())
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reglas", fontSize = 12.sp)
                }

                Button(
                    onClick = { viewModel.navigateTo("history") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_button")
                ) {
                    Icon(Icons.Default.History, contentDescription = null, sizeModifier())
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Historial", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// --- 2. LOBBY SCREEN (SALA DE ESPERA FAMILIAR) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val roomCode by viewModel.roomCode.collectAsState()
    val isOnline by viewModel.isOnlineMode.collectAsState()
    val players by viewModel.roomPlayers.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val pingMs by viewModel.pingMs.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()

    var customMessage by remember { mutableStateOf("") }
    val quickPhrases = listOf("¡Listo para ganar! 👑", "¡Hola familia! 👋", "Voy por el Mate/Café ☕", "¡Qué comience el juego! 🎲")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FeltGreenMedium, FeltGreenDark)
                )
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo("menu") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "SALA: $roomCode", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = "Modo: ${gameMode.name}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                if (isOnline) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Green, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${pingMs}ms", color = Color.Green, fontSize = 11.sp)
                    }
                } else {
                    Text(text = "OFFLINE", color = Color.LightGray, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Players list grid
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "JUGADORES EN LA MESA (${players.size}/4)", color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        players.forEach { p ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MahoganyDark),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = p.countryFlag, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = p.name,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (p.isHuman) "Tú" else "Familiar",
                                        color = Color.LightGray,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                        
                        // Empty slot placeholders
                        repeat((4 - players.size).coerceAtLeast(0)) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x11FFFFFF)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "💤", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "Esperando...", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chat section (Family Chat Box)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "💬 CHAT DE LA FAMILIA", color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        reverseLayout = true
                    ) {
                        items(chatMessages.reversed()) { msg ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${msg.senderFlag} ", fontSize = 14.sp)
                                Text(text = "${msg.senderName}: ", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = msg.message, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    // Quick Phrases
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPhrases.forEach { phrase ->
                            Box(
                                modifier = Modifier
                                    .background(Color.DarkGray, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.sendChatMessage(phrase) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = phrase, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    // Text Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customMessage,
                            onValueChange = { customMessage = it },
                            placeholder = { Text("Escribe un mensaje...", color = Color.LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                viewModel.sendChatMessage(customMessage)
                                customMessage = ""
                            },
                            modifier = Modifier
                                .background(AmberGold, RoundedCornerShape(8.dp))
                                .testTag("send_chat_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Start Button
            Button(
                onClick = { viewModel.startGameplay() },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("start_game_button")
            ) {
                Text("¡COMENZAR PARTIDA!", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}

// --- 3. THE PRIMARY GAME TABLE SCREEN (RACK < 20% HEIGHT MANDATE) ---
@Composable
fun GameScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val session by viewModel.gameSession.collectAsState()
    val selectedTiles by viewModel.selectedHandTiles.collectAsState()
    val logs by viewModel.logs.collectAsState()

    if (session == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AmberGold)
        }
        return
    }

    val activeSession = session!!
    val isYourTurn = activeSession.currentPlayerIndex == 0
    val activePlayer = activeSession.players[activeSession.currentPlayerIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(FeltGreenLight, FeltGreenDark),
                    radius = 1200f
                )
            )
    ) {
        // A. Upper Navigation and Status (80% Height Area)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.80f)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo("menu") }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Exit", tint = Color.White)
                }

                // Title Mode Indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MESÓN FAMILIAR",
                        color = AmberGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "REGLAS: ${activeSession.mode.name}",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }

                // Turn Badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isYourTurn) Color(0xFF2E7D32) else Color.DarkGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isYourTurn) "TU TURNO" else "TURNO DE ${activePlayer.name.uppercase()}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Players HUD (Opponents counters)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                activeSession.players.forEachIndexed { idx, p ->
                    val isCurrent = activeSession.currentPlayerIndex == idx
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) Color(0xFF332211) else Color(0x33000000)
                        ),
                        border = if (isCurrent) BorderStroke(1.dp, AmberGold) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "${p.countryFlag} ${p.name}", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "🎴 x${p.hand.size}", color = AmberGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            
                            if (activeSession.mode == GameMode.BURACO) {
                                Text(
                                    text = if (p.isBoughtMuerto) "Muerto ✓" else "Muerto ✗",
                                    color = if (p.isBoughtMuerto) Color.Green else Color.LightGray,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }

            // Draw Section Stack (Pila / Pozo)
            DrawPilesSection(
                poolCount = activeSession.pool.size,
                discardPile = activeSession.discardPile,
                turnPhase = activeSession.turnPhase,
                isYourTurn = isYourTurn,
                onDrawPool = { viewModel.drawTileFromPool() },
                onDrawDiscard = { viewModel.drawTileFromDiscard() },
                mode = activeSession.mode
            )

            // Table Melds Board area (where matches happen)
            Text(
                text = "JUEGOS EN LA MESA (Toca un juego para acomodar tu ficha seleccionada)",
                color = AmberGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0x1F000000), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                if (activeSession.meldedGroups.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Mesa Limpia", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Baja tus Escaleras o Piernas para iniciar", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(activeSession.meldedGroups) { meld ->
                            val canAppend = isYourTurn && activeSession.turnPhase == TurnPhase.PLAY && selectedTiles.size == 1
                            MeldTableGroup(
                                meld = meld,
                                canAppend = canAppend,
                                onAppend = { viewModel.appendSelectedToMeld(meld.id) }
                            )
                        }
                    }
                }
            }

            // Live Game Console log (Tracks moves dynamically)
            Card(
                colors = CardDefaults.cardColors(containerColor = MahoganyDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    reverseLayout = true
                ) {
                    items(logs.reversed()) { log ->
                        Text(
                            text = log,
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }

            // Play Controllers
            if (isYourTurn && activeSession.turnPhase == TurnPhase.PLAY) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { viewModel.meldSelectedAsRun() },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("meld_run_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Bajar Escalera", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.meldSelectedAsSet() },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("meld_set_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Bajar Pierna", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.clearTileSelections() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("clear_selection_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Limpiar", fontSize = 10.sp)
                    }
                }
            } else if (isYourTurn && activeSession.turnPhase == TurnPhase.DISCARD) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE65100), RoundedCornerShape(4.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👉 TOCA UNA FICHA EN TU ATRIL PARA DESCARTAR Y TERMINAR TU TURNO",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // B. ATRIL RACK - MANDATE: LESS THAN 20% HEIGHT OF SCREEN
        val yourPlayer = activeSession.players.first { it.isHuman }
        
        AtrilRack(
            tiles = yourPlayer.hand,
            selectedTiles = selectedTiles,
            onTileClick = { tile ->
                if (isYourTurn && activeSession.turnPhase == TurnPhase.DISCARD) {
                    // Discard action ends turn
                    viewModel.discardSelectedTile(tile)
                } else {
                    // Toggle selection for play
                    viewModel.toggleTileSelection(tile)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.20f) // Constrains the rack to exactly 20% height
        )
    }
}

// --- 4. RULES MANUAL SCREEN ---
@Composable
fun RulesScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FeltGreenMedium, FeltGreenDark)
                )
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo("menu") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REGLAS: BURACO & RUMMY",
                    color = AmberGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0x33000000), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("REGLAS CLÁSICAS DEL BURACO", color = AmberGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "• Fichas: 104 numeradas (1 al 13 en 4 colores por duplicado) + 2 Comodines.\n" +
                        "• Escaleras: Al menos 3 números consecutivos del mismo color (ej: Rojo 4-5-6).\n" +
                        "• Piernas: Juego de 3 o 4 fichas del mismo número, cualquier color.\n" +
                        "• Comodines Especiales: El número 2 actúa como comodín en Burako, además de los Jokers.\n" +
                        "• Canasta: Grupo bajado con un mínimo de 7 fichas. Puede ser Pura (sin comodín, vale 200 pts) o Impura (con comodín, vale 100 pts).\n" +
                        "• El Muerto: Se reparte un pozo extra de 11 fichas. El jugador que se queda sin fichas en mano compra el Muerto para seguir jugando.\n" +
                        "• Cierre: Para cerrar y ganar la ronda es requisito obligatorio tener al menos una Canasta armada.",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                item {
                    Text("REGLAS DEL RUMMY / RUMMIKUB", color = AmberGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "• Escaleras: Igual que Burako (mismo color consecutivo).\n" +
                        "• Piernas (Sets): 3 o 4 fichas del mismo número, pero obligatoriamente de COLORES DIFERENTES.\n" +
                        "• Comodines: Únicamente los Jokers tradicionales (el número 2 no es comodín en Rummy).\n" +
                        "• Reorganizar: En Rummy, se pueden acoplar fichas a las piernas o escaleras existentes en la mesa.\n" +
                        "• Cierre: El primer jugador que se descarte de toda su mano gana la partida.",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                item {
                    Text("VALOR DE LAS FICHAS (BURACO)", color = AmberGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "• Número 1: 15 puntos\n" +
                        "• Número 2 (Comodín): 20 puntos\n" +
                        "• Números 3 al 7: 5 puntos\n" +
                        "• Números 8 al 13: 10 puntos\n" +
                        "• Jokers tradicionales: 50 puntos\n" +
                        "• Cierre de ronda: +100 puntos extra",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.navigateTo("menu") },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entendido, Volver", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 5. STATS & HISTORY SCREEN ---
@Composable
fun StatsHistoryScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val history by viewModel.matchHistory.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FeltGreenMedium, FeltGreenDark)
                )
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo("menu") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ESTADÍSTICAS Y PARTIDAS",
                    color = AmberGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player level progress card
            profile?.let { p ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MahoganyDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = p.countryFlag, fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "Rango: Aficionado Familiar", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "⭐ Nivel ${p.level}", color = AmberGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "XP: ${p.xp} total", color = Color.LightGray, fontSize = 12.sp)
                        }
                        
                        // Simple progress indicator
                        val progress = (p.xp % 150) / 150f
                        LinearProgressIndicator(
                            progress = { progress },
                            color = AmberGold,
                            trackColor = Color.DarkGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "HISTORIAL RECIENTE",
                color = AmberGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // History list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0x33000000), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No has jugado partidas todavía.", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { match ->
                            val isWin = match.result == "WIN"
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Modo: ${match.mode}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Rivales: ${match.opponents}",
                                            color = Color.LightGray,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (isWin) "🏆 GANADO" else "❌ PERDIDO",
                                            color = if (isWin) Color.Green else Color.Red,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "${match.score} pts",
                                            color = AmberGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.clearHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Borrar Historial", fontSize = 12.sp)
                }

                Button(
                    onClick = { viewModel.navigateTo("menu") },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Volver", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// Helpers
private fun sizeModifier() = Modifier.size(16.dp)
