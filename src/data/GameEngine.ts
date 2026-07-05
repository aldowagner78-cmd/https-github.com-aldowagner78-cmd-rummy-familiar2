import { Tile, TileColor, GameMode, Meld, Player, GameSession } from '../types';

export const GameEngine = {
  // Generates a standard deck of 104 tiles (2 sets of 1-13 in 4 colors) + 2 Jokers = 106 tiles
  generateDeck(): Tile[] {
    const deck: Tile[] = [];
    let currentId = 1;
    const colors: TileColor[] = ['RED', 'BLUE', 'BLACK', 'ORANGE'];

    // Repeat twice (2 identical sets)
    for (let r = 0; r < 2; r++) {
      for (const color of colors) {
        for (let number = 1; number <= 13; number++) {
          deck.push({
            id: currentId++,
            number,
            color,
            isJoker: false
          });
        }
      }
    }

    // Add 2 Jokers
    for (let j = 0; j < 2; j++) {
      deck.push({
        id: currentId++,
        number: 0,
        color: 'JOKER',
        isJoker: true
      });
    }

    // Fisher-Yates shuffle
    for (let i = deck.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [deck[i], deck[j]] = [deck[j], deck[i]];
    }

    return deck;
  },

  // Helper: Checks if tile functions as a wildcard in the current game mode
  isWildcard(tile: Tile, mode: GameMode): boolean {
    if (tile.isJoker) return true;
    // Under Burako rules, all number 2s are wildcards (comodines)
    if (mode === 'BURACO' && tile.number === 2) return true;
    return false;
  },

  // Helper: Get points for a tile
  getTilePoints(tile: Tile, mode: GameMode): number {
    if (tile.isJoker) return 50;
    if (mode === 'BURACO') {
      if (tile.number === 1) return 15;
      if (tile.number === 2) return 20;
      if (tile.number >= 3 && tile.number <= 7) return 5;
      if (tile.number >= 8 && tile.number <= 13) return 10;
      return 10;
    } else {
      // Rummy classic values
      if (tile.number === 1) return 10;
      if (tile.number >= 2 && tile.number <= 10) return tile.number;
      if (tile.number >= 11 && tile.number <= 13) return 10;
      return 10;
    }
  },

  // Sorts tiles in hand (by color, then by number)
  sortHand(tiles: Tile[]): Tile[] {
    return [...tiles].sort((a, b) => {
      if (a.color !== b.color) {
        return a.color.localeCompare(b.color);
      }
      return a.number - b.number;
    });
  },

  // Helper: check if a meld is a Canasta (7 or more tiles in Burako)
  isCanasta(meld: Meld): boolean {
    return meld.tiles.length >= 7;
  },

  // Helper: check if a Canasta is pure (no wildcards / jokers / non-natural 2s)
  isPureCanasta(meld: Meld, mode: GameMode): boolean {
    if (!this.isCanasta(meld)) return false;
    if (meld.tiles.some(t => t.isJoker)) return false;

    if (mode === 'BURACO') {
      if (meld.type === 'SET') {
        // For a set (Pierna), all tiles must be same number.
        // If there's a 2 and the set is NOT 2s, then 2 is wildcard (impure).
        const nonTwo = meld.tiles.filter(t => t.number !== 2);
        if (nonTwo.length < meld.tiles.length && nonTwo.length > 0) return false;
      } else {
        // For a run (Escalera), verify it is naturally consecutive without wildcard usage of 2s.
        const sorted = [...meld.tiles].sort((a, b) => a.number - b.number);
        for (let i = 0; i < sorted.length - 1; i++) {
          if (sorted[i + 1].number - sorted[i].number !== 1) {
            return false;
          }
        }
      }
    }
    return true;
  },

  // Calculate points of a melded group
  getMeldPoints(meld: Meld, mode: GameMode): number {
    let pts = meld.tiles.reduce((acc, tile) => acc + this.getTilePoints(tile, mode), 0);
    if (mode === 'BURACO' && this.isCanasta(meld)) {
      pts += this.isPureCanasta(meld, mode) ? 200 : 100;
    }
    return pts;
  },

  // Starts a new game session
  startNewGame(
    mode: GameMode,
    playerNames: string[],
    playerFlags: string[],
    humanIndex: number = 0
  ): GameSession {
    const deck = this.generateDeck();
    const players: Player[] = [];
    const handSize = 11;

    playerNames.forEach((name, index) => {
      const hand: Tile[] = [];
      for (let h = 0; h < handSize; h++) {
        if (deck.length > 0) {
          hand.push(deck.shift()!);
        }
      }
      players.push({
        id: index === humanIndex ? 'human' : `ai_${index}`,
        name,
        countryFlag: playerFlags[index] || '🇦🇷',
        hand: this.sortHand(hand),
        isHuman: index === humanIndex,
        isBoughtMuerto: false,
        totalScore: 0,
        roundScore: 0,
        isReady: true
      });
    });

    // In Burako, we set aside two "Muertos" of 11 tiles each
    const muerto1: Tile[] = [];
    const muerto2: Tile[] = [];
    if (mode === 'BURACO') {
      for (let m = 0; m < 11; m++) {
        if (deck.length > 0) muerto1.push(deck.shift()!);
        if (deck.length > 0) muerto2.push(deck.shift()!);
      }
    }

    // Face up top card for discard pile
    const discardPile: Tile[] = [];
    if (deck.length > 0) {
      discardPile.push(deck.shift()!);
    }

    return {
      mode,
      players,
      pool: deck,
      discardPile,
      meldedGroups: [],
      currentPlayerIndex: 0,
      turnPhase: 'DRAW',
      muerto1,
      muerto2,
      muerto1Bought: false,
      muerto2Bought: false,
      winnerId: null,
      roundLog: [`¡El juego de ${mode} ha comenzado! Turno de ${playerNames[0]}.`]
    };
  },

  // Draw tile from pool
  drawFromPool(session: GameSession): GameSession {
    if (session.pool.length === 0) return session;
    const pool = [...session.pool];
    const drawn = pool.shift()!;
    const players = session.players.map((p, idx) => {
      if (idx === session.currentPlayerIndex) {
        return {
          ...p,
          hand: this.sortHand([...p.hand, drawn])
        };
      }
      return p;
    });

    return {
      ...session,
      pool,
      players,
      turnPhase: 'PLAY'
    };
  },

  // Draw top card from discard pile (Rummy style)
  drawFromDiscard(session: GameSession): GameSession {
    if (session.discardPile.length === 0) return session;
    const discardPile = [...session.discardPile];
    const drawn = discardPile.pop()!;
    const players = session.players.map((p, idx) => {
      if (idx === session.currentPlayerIndex) {
        return {
          ...p,
          hand: this.sortHand([...p.hand, drawn])
        };
      }
      return p;
    });

    return {
      ...session,
      discardPile,
      players,
      turnPhase: 'PLAY'
    };
  },

  // Rob the ENTIRE discard pile (Burako characteristic "Robar el Pozo")
  robDiscardPile(session: GameSession): GameSession {
    if (session.discardPile.length === 0) return session;
    const discardPile = [...session.discardPile];
    const players = session.players.map((p, idx) => {
      if (idx === session.currentPlayerIndex) {
        return {
          ...p,
          hand: this.sortHand([...p.hand, ...discardPile])
        };
      }
      return p;
    });

    return {
      ...session,
      discardPile: [],
      players,
      turnPhase: 'PLAY'
    };
  },

  // Validates if a group of tiles forms a valid Escalera (RUN) or Pierna (SET)
  isValidMeldGroup(tiles: Tile[], type: 'RUN' | 'SET', mode: GameMode): boolean {
    if (tiles.length < 3) return false;

    const wildcards = tiles.filter(t => this.isWildcard(t, mode));
    const normals = tiles.filter(t => !this.isWildcard(t, mode));

    // Rule: Cannot have more wildcards than normal cards (max 50% wildcards in a meld)
    if (wildcards.length > Math.floor(tiles.length / 2)) return false;

    if (type === 'SET') {
      // Pierna: same number, different/any colors
      if (normals.length === 0) return true;
      const targetNumber = normals[0].number;
      // All normal tiles must have the same number
      if (normals.some(t => t.number !== targetNumber)) return false;

      // In Rummy, a Pierna (set) must have DIFFERENT colors for each tile!
      if (mode === 'RUMMY') {
        const colors = normals.map(t => t.color);
        const uniqueColors = new Set(colors);
        if (colors.length !== uniqueColors.size) return false;
      }
      return tiles.length <= 4; // Max size of set is 4 in classic Rummy
    } else {
      // Escalera (Run): same color, consecutive numbers
      if (normals.length === 0) return true;
      const targetColor = normals[0].color;
      // All normal tiles must be of the same color
      if (normals.some(t => t.color !== targetColor)) return false;

      const sortedNormals = [...normals].sort((a, b) => a.number - b.number);
      // No duplicate numbers in the run
      const numbers = sortedNormals.map(t => t.number);
      const uniqueNumbers = new Set(numbers);
      if (numbers.length !== uniqueNumbers.size) return false;

      // Check consecutiveness with wildcard gap spaces
      let neededWildcards = 0;
      for (let i = 0; i < sortedNormals.length - 1; i++) {
        const diff = sortedNormals[i + 1].number - sortedNormals[i].number;
        if (diff <= 0) return false;
        neededWildcards += (diff - 1);
      }

      return neededWildcards <= wildcards.length;
    }
  },

  // Places a new meld on the table from selected tiles
  meldFromPlayer(session: GameSession, selectedTiles: Tile[], type: 'RUN' | 'SET'): { updatedSession: GameSession; error: string | null } {
    const players = [...session.players];
    const currentPlayer = players[session.currentPlayerIndex];

    // Validate tiles exist in hand
    const handIds = new Set(currentPlayer.hand.map(t => t.id));
    if (!selectedTiles.every(t => handIds.has(t.id))) {
      return { updatedSession: session, error: 'Error: Algunas fichas seleccionadas no están en tu mano.' };
    }

    // Validate meld structure
    if (!this.isValidMeldGroup(selectedTiles, type, session.mode)) {
      return { updatedSession: session, error: `Error: Combinación inválida para una ${type === 'RUN' ? 'Escalera' : 'Pierna'}.` };
    }

    // Remove from hand, add to table
    const selectedIds = new Set(selectedTiles.map(t => t.id));
    const updatedHand = currentPlayer.hand.filter(t => !selectedIds.has(t.id));

    const newMeld: Meld = {
      id: Math.random().toString(36).substring(2, 9),
      tiles: [...selectedTiles].sort((a, b) => a.number - b.number),
      type
    };

    const updatedMelds = [...session.meldedGroups, newMeld];
    players[session.currentPlayerIndex] = {
      ...currentPlayer,
      hand: this.sortHand(updatedHand)
    };

    let updatedSession: GameSession = {
      ...session,
      players,
      meldedGroups: updatedMelds
    };

    // Check Muerto Trigger (Burako)
    if (session.mode === 'BURACO' && updatedHand.length === 0 && !currentPlayer.isBoughtMuerto) {
      updatedSession = this.buyMuertoForCurrentPlayer(updatedSession, () => {});
    }

    return { updatedSession, error: null };
  },

  // Appends a single tile to an existing meld on the table
  appendTileToMeld(session: GameSession, meldId: string, tile: Tile): { updatedSession: GameSession; error: string | null } {
    const players = [...session.players];
    const currentPlayer = players[session.currentPlayerIndex];

    if (!currentPlayer.hand.some(t => t.id === tile.id)) {
      return { updatedSession: session, error: 'Ficha no encontrada en la mano.' };
    }

    const melds = [...session.meldedGroups];
    const meldIdx = melds.findIndex(m => m.id === meldId);
    if (meldIdx === -1) return { updatedSession: session, error: 'Juego de mesa no encontrado.' };

    const targetMeld = melds[meldIdx];
    const newTiles = [...targetMeld.tiles, tile].sort((a, b) => a.number - b.number);

    if (!this.isValidMeldGroup(newTiles, targetMeld.type, session.mode)) {
      return { updatedSession: session, error: 'Esa ficha no encaja en este juego.' };
    }

    // Update hand and meld
    const updatedHand = currentPlayer.hand.filter(t => t.id !== tile.id);
    melds[meldIdx] = {
      ...targetMeld,
      tiles: newTiles
    };

    players[session.currentPlayerIndex] = {
      ...currentPlayer,
      hand: this.sortHand(updatedHand)
    };

    let updatedSession: GameSession = {
      ...session,
      players,
      meldedGroups: melds
    };

    // Check Muerto Trigger (Burako)
    if (session.mode === 'BURACO' && updatedHand.length === 0 && !currentPlayer.isBoughtMuerto) {
      updatedSession = this.buyMuertoForCurrentPlayer(updatedSession, () => {});
    }

    return { updatedSession, error: null };
  },

  // Discard tile to end turn phase
  discardTile(session: GameSession, tile: Tile): { updatedSession: GameSession; error: string | null } {
    const players = [...session.players];
    const currentPlayer = players[session.currentPlayerIndex];

    if (!currentPlayer.hand.some(t => t.id === tile.id)) {
      return { updatedSession: session, error: 'La ficha no está en tu mano.' };
    }

    const updatedHand = currentPlayer.hand.filter(t => t.id !== tile.id);
    players[session.currentPlayerIndex] = {
      ...currentPlayer,
      hand: this.sortHand(updatedHand)
    };

    const discardPile = [...session.discardPile, tile];

    let updatedSession: GameSession = {
      ...session,
      players,
      discardPile,
      turnPhase: 'DRAW'
    };

    // Check close game trigger
    if (updatedHand.length === 0) {
      const hasCanasta = session.meldedGroups.some(m => this.isCanasta(m));
      if (session.mode === 'RUMMY' || hasCanasta) {
        updatedSession = this.endRound(updatedSession, () => {});
        return { updatedSession, error: '¡Ganaste la ronda!' };
      }
    }

    // Next turn index
    const nextIndex = (session.currentPlayerIndex + 1) % session.players.length;
    updatedSession = {
      ...updatedSession,
      currentPlayerIndex: nextIndex
    };

    return { updatedSession, error: null };
  },

  // Buy Muerto for current player in Burako
  buyMuertoForCurrentPlayer(session: GameSession, onLog: (msg: string) => void): GameSession {
    const players = [...session.players];
    const currentPlayer = players[session.currentPlayerIndex];

    if (currentPlayer.isBoughtMuerto) return session;

    let muertoTiles: Tile[] = [];
    let m1 = session.muerto1Bought;
    let m2 = session.muerto2Bought;

    if (!m1) {
      muertoTiles = session.muerto1;
      m1 = true;
      onLog(`${currentPlayer.countryFlag} ${currentPlayer.name} compró el Muerto 1!`);
    } else if (!m2) {
      muertoTiles = session.muerto2;
      m2 = true;
      onLog(`${currentPlayer.countryFlag} ${currentPlayer.name} compró el Muerto 2!`);
    } else {
      onLog('No quedan Muertos disponibles.');
      return session;
    }

    players[session.currentPlayerIndex] = {
      ...currentPlayer,
      hand: this.sortHand([...currentPlayer.hand, ...muertoTiles]),
      isBoughtMuerto: true
    };

    return {
      ...session,
      players,
      muerto1Bought: m1,
      muerto2Bought: m2
    };
  },

  // Evaluates if a card is useful (helps complete a run or set)
  isTileUseful(hand: Tile[], tile: Tile, mode: GameMode): boolean {
    if (this.isWildcard(tile, mode)) return true;

    // Check if matches same color close numbers for Escalera
    const sameColor = hand.filter(t => t.color === tile.color);
    if (sameColor.some(t => Math.abs(t.number - tile.number) <= 2)) return true;

    // Check if matches same numbers for Pierna
    const sameNumber = hand.filter(t => t.number === tile.number);
    if (sameNumber.length >= 2) return true;

    return false;
  },

  // Choose a tile to discard from hand (Heuristic)
  selectTileToDiscard(hand: Tile[], mode: GameMode): Tile {
    const nonWildcards = hand.filter(t => !this.isWildcard(t, mode));
    if (nonWildcards.length === 0) return hand[0];

    // Find cards that have zero connection to remaining hand
    for (const tile of nonWildcards) {
      const hasSequence = hand.some(t => t.color === tile.color && Math.abs(t.number - tile.number) === 1);
      const hasSameNumber = hand.some(t => t.id !== tile.id && t.number === tile.number);
      if (!hasSequence && !hasSameNumber) {
        return tile;
      }
    }

    // Otherwise return highest number card
    return [...nonWildcards].sort((a, b) => b.number - a.number)[0] || hand[0];
  },

  // Scan AI's hand for valid combinations
  findValidMelds(hand: Tile[], mode: GameMode): Meld[] {
    const results: Meld[] = [];

    // 1. Group by number to find Sets (Piernas)
    const normalNonWildcards = hand.filter(t => !this.isWildcard(t, mode));
    const numberGroups: { [key: number]: Tile[] } = {};
    normalNonWildcards.forEach(t => {
      if (!numberGroups[t.number]) numberGroups[t.number] = [];
      numberGroups[t.number].push(t);
    });

    Object.entries(numberGroups).forEach(([, tiles]) => {
      if (tiles.length >= 3) {
        results.push({
          id: Math.random().toString(36).substring(2, 9),
          tiles: tiles.slice(0, 4),
          type: 'SET'
        });
      }
    });

    // 2. Group by color to find Runs (Escaleras)
    const colorGroups: { [key: string]: Tile[] } = {};
    normalNonWildcards.forEach(t => {
      if (!colorGroups[t.color]) colorGroups[t.color] = [];
      colorGroups[t.color].push(t);
    });

    Object.entries(colorGroups).forEach(([, tiles]) => {
      const sorted = [...tiles].sort((a, b) => a.number - b.number);
      let currentRun: Tile[] = [];
      for (const tile of sorted) {
        if (currentRun.length === 0) {
          currentRun.push(tile);
        } else {
          if (tile.number === currentRun[currentRun.length - 1].number + 1) {
            currentRun.push(tile);
          } else if (tile.number !== currentRun[currentRun.length - 1].number) {
            if (currentRun.length >= 3) {
              results.push({
                id: Math.random().toString(36).substring(2, 9),
                tiles: [...currentRun],
                type: 'RUN'
              });
            }
            currentRun = [tile];
          }
        }
      }
      if (currentRun.length >= 3) {
        results.push({
          id: Math.random().toString(36).substring(2, 9),
          tiles: [...currentRun],
          type: 'RUN'
        });
      }
    });

    return results;
  },

  // Executes AI player automatically
  executeAiTurn(session: GameSession, onLog: (msg: string) => void): GameSession {
    let currentSession = { ...session };
    const activePlayer = currentSession.players[currentSession.currentPlayerIndex];

    if (activePlayer.isHuman || currentSession.winnerId !== null) return currentSession;

    const nameWithFlag = `${activePlayer.countryFlag} ${activePlayer.name}`;
    onLog(`Pensando turno para ${nameWithFlag}...`);

    // 1. DRAW PHASE
    const discardTop = currentSession.discardPile[currentSession.discardPile.length - 1];
    let robbedPozo = false;

    if (discardTop) {
      const topUseful = this.isTileUseful(activePlayer.hand, discardTop, currentSession.mode);
      if (currentSession.mode === 'BURACO') {
        if (topUseful || currentSession.discardPile.length >= 4) {
          robbedPozo = true;
          onLog(`${nameWithFlag} robó el pozo entero (${currentSession.discardPile.length} fichas)!`);
          currentSession = this.robDiscardPile(currentSession);
        }
      } else {
        // Rummy
        if (topUseful) {
          robbedPozo = true;
          onLog(`${nameWithFlag} tomó ${this.formatTile(discardTop)} del pozo.`);
          currentSession = this.drawFromDiscard(currentSession);
        }
      }
    }

    if (!robbedPozo) {
      if (currentSession.pool.length > 0) {
        onLog(`${nameWithFlag} robó una ficha de la pila.`);
        currentSession = this.drawFromPool(currentSession);
      } else {
        onLog('La pila se ha agotado. Fin de la ronda.');
        return this.endRound(currentSession, onLog);
      }
    }

    let player = currentSession.players[currentSession.currentPlayerIndex];

    // 2. PLAY PHASE
    const potentialMelds = this.findValidMelds(player.hand, currentSession.mode);
    let updatedMelds = [...currentSession.meldedGroups];
    let currentHand = [...player.hand];

    if (potentialMelds.length > 0) {
      potentialMelds.forEach(meld => {
        // Double check tiles are still in hand
        const handIds = new Set(currentHand.map(t => t.id));
        if (meld.tiles.every(t => handIds.has(t.id))) {
          const meldIds = new Set(meld.tiles.map(t => t.id));
          currentHand = currentHand.filter(t => !meldIds.has(t.id));
          updatedMelds.push(meld);
          onLog(`${nameWithFlag} bajó un juego: ${this.formatMeld(meld)}`);
        }
      });
    }

    // Try to append tiles to existing melds on the table
    const meldsAfterAdditions: Meld[] = [];
    for (const meld of updatedMelds) {
      let tempMeld = { ...meld };
      const tilesToAdd: Tile[] = [];
      for (const tile of currentHand) {
        const newTiles = [...tempMeld.tiles, tile].sort((a, b) => a.number - b.number);
        if (this.isValidMeldGroup(newTiles, tempMeld.type, currentSession.mode)) {
          tilesToAdd.push(tile);
          tempMeld = {
            ...tempMeld,
            tiles: newTiles
          };
          onLog(`${nameWithFlag} acomodó ${this.formatTile(tile)} en la mesa.`);
        }
      }
      const addedIds = new Set(tilesToAdd.map(t => t.id));
      currentHand = currentHand.filter(t => !addedIds.has(t.id));
      meldsAfterAdditions.push(tempMeld);
    }
    updatedMelds = meldsAfterAdditions;

    // Apply hand updates to AI
    player = {
      ...player,
      hand: this.sortHand(currentHand)
    };
    let updatedPlayers = [...currentSession.players];
    updatedPlayers[currentSession.currentPlayerIndex] = player;
    currentSession = {
      ...currentSession,
      meldedGroups: updatedMelds,
      players: updatedPlayers
    };

    // 3. MUERTO BUY CHECK (Burako)
    if (currentSession.mode === 'BURACO' && player.hand.length === 0 && !player.isBoughtMuerto) {
      currentSession = this.buyMuertoForCurrentPlayer(currentSession, onLog);
      player = currentSession.players[currentSession.currentPlayerIndex];
      currentHand = [...player.hand];
    }

    // 4. DISCARD PHASE
    if (currentHand.length > 0) {
      const discardTile = this.selectTileToDiscard(currentHand, currentSession.mode);
      currentHand = currentHand.filter(t => t.id !== discardTile.id);
      player = {
        ...player,
        hand: this.sortHand(currentHand)
      };
      updatedPlayers = [...currentSession.players];
      updatedPlayers[currentSession.currentPlayerIndex] = player;

      const discardPile = [...currentSession.discardPile, discardTile];
      onLog(`${nameWithFlag} descartó ${this.formatTile(discardTile)}.`);

      currentSession = {
        ...currentSession,
        players: updatedPlayers,
        discardPile,
        turnPhase: 'DRAW'
      };
    }

    // Check if round closed
    if (player.hand.length === 0) {
      const teamHasCanasta = updatedMelds.some(m => this.isCanasta(m));
      if (currentSession.mode === 'RUMMY' || teamHasCanasta) {
        onLog(`¡${nameWithFlag} ha cerrado la partida!`);
        return this.endRound(currentSession, onLog);
      }
    }

    // Advance turn to next player
    const nextIndex = (currentSession.currentPlayerIndex + 1) % currentSession.players.length;
    onLog(`Turno de ${currentSession.players[nextIndex].countryFlag} ${currentSession.players[nextIndex].name}.`);

    return {
      ...currentSession,
      currentPlayerIndex: nextIndex,
      turnPhase: 'DRAW'
    };
  },

  // Calculate scores and close round
  endRound(session: GameSession, onLog: (msg: string) => void): GameSession {
    const players = session.players.map((player) => {
      const meldedPoints = session.meldedGroups.reduce((acc, m) => acc + this.getMeldPoints(m, session.mode), 0);
      const handPenalty = player.hand.reduce((acc, t) => acc + this.getTilePoints(t, session.mode), 0);

      const isCloser = player.hand.length === 0;
      const closureBonus = isCloser ? 100 : 0;

      // Real calculation
      const pointsPlaced = player.isHuman ? meldedPoints : Math.floor(meldedPoints * 0.7);
      const roundScore = pointsPlaced + closureBonus - handPenalty;
      const totalScore = player.totalScore + roundScore;

      return {
        ...player,
        roundScore,
        totalScore
      };
    });

    const winner = [...players].sort((a, b) => b.totalScore - a.totalScore)[0];
    onLog(`Ronda terminada. Ganador de la ronda: ${winner.name} con ${winner.totalScore} puntos!`);

    return {
      ...session,
      players,
      winnerId: winner.id,
      turnPhase: 'DRAW'
    };
  },

  // Helpers to print/format objects
  formatTile(tile: Tile): string {
    if (tile.isJoker) return '🃏 Comodín';
    const colorSymbol: Record<string, string> = {
      RED: '🔴',
      BLUE: '🔵',
      BLACK: '⚫',
      ORANGE: '🟠',
      JOKER: '🃏'
    };
    const symbol = colorSymbol[tile.color] || '🃏';

    const colorEmoji = tile.color === 'ORANGE' ? '🟠' : symbol;
    return `${colorEmoji} ${tile.number}`;
  },

  formatMeld(meld: Meld): string {
    const tilesStr = meld.tiles.map(t => this.formatTile(t)).join(' ');
    return `${meld.type === 'RUN' ? 'Escalera' : 'Pierna'} (${tilesStr})`;
  }
};
