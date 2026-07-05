import React, { useState, useEffect, useRef } from 'react';
import { Player, Tile, GameSession, GameMode } from '../types';
import { GameEngine } from '../data/GameEngine';
import { ArrowLeft, MessageSquare, AlertCircle, RefreshCw, Star } from 'lucide-react';

interface GameScreenProps {
  initialPlayers: Player[];
  mode: GameMode;
  isOnlineMode: boolean;
  onNavigateTo: (screen: string) => void;
  onGameEnd: (result: 'WIN' | 'LOSS', score: number, opponents: string) => void;
}

export const GameScreen: React.FC<GameScreenProps> = ({
  initialPlayers,
  mode,
  isOnlineMode: _isOnlineMode,
  onNavigateTo,
  onGameEnd,
}) => {
  const [session, setSession] = useState<GameSession | null>(null);
  const [selectedTileIds, setSelectedTileIds] = useState<Set<number>>(new Set());
  const [logs, setLogs] = useState<string[]>([]);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [showLogs, setShowLogs] = useState(false);
  const logContainerRef = useRef<HTMLDivElement>(null);

  // Initialize Game Session
  useEffect(() => {
    const playerNames = initialPlayers.map(p => p.name);
    const playerFlags = initialPlayers.map(p => p.countryFlag);
    const humanIdx = initialPlayers.findIndex(p => p.isHuman);

    const newSession = GameEngine.startNewGame(mode, playerNames, playerFlags, humanIdx >= 0 ? humanIdx : 0);
    setSession(newSession);
    setLogs(newSession.roundLog);
  }, [mode, initialPlayers]);

  // Auto-scroll logs
  useEffect(() => {
    if (logContainerRef.current) {
      logContainerRef.current.scrollTop = logContainerRef.current.scrollHeight;
    }
  }, [logs, showLogs]);

  const handleAddLog = (msg: string) => {
    setLogs(prev => [...prev, msg]);
  };

  const clearError = () => setErrorMsg(null);

  // AI Turn Handling
  useEffect(() => {
    if (!session || session.winnerId !== null) return;

    const currentPlayer = session.players[session.currentPlayerIndex];
    if (!currentPlayer.isHuman) {
      // Run AI turn after a short delay for realistic pacing
      const aiTimer = setTimeout(() => {
        const updated = GameEngine.executeAiTurn(session, handleAddLog);
        setSession(updated);
      }, 1500);

      return () => clearTimeout(aiTimer);
    }
  }, [session]);

  if (!session) {
    return (
      <div className="min-h-screen bg-felt-dark flex items-center justify-center">
        <div className="text-center">
          <RefreshCw className="w-8 h-8 text-gold animate-spin mx-auto mb-2" />
          <span className="font-bold text-sm text-gray-300">Repartiendo fichas físicas...</span>
        </div>
      </div>
    );
  }

  const players = session.players;
  const humanPlayer = players.find(p => p.id === 'human') || players[0];
  const isMyTurn = players[session.currentPlayerIndex].id === 'human' && session.winnerId === null;

  // Actions
  const handleDrawFromPool = () => {
    if (!isMyTurn || session.turnPhase !== 'DRAW') return;
    clearError();
    const updated = GameEngine.drawFromPool(session);
    setSession(updated);
    handleAddLog(`👉 ${humanPlayer.countryFlag} ${humanPlayer.name} robó una ficha de la pila.`);
  };

  const handleDrawFromDiscard = () => {
    if (!isMyTurn || session.turnPhase !== 'DRAW') return;
    if (session.discardPile.length === 0) return;
    clearError();
    const topTile = session.discardPile[session.discardPile.length - 1];
    const updated = GameEngine.drawFromDiscard(session);
    setSession(updated);
    handleAddLog(`👉 ${humanPlayer.countryFlag} ${humanPlayer.name} tomó ${GameEngine.formatTile(topTile)} del pozo.`);
  };

  const handleRobDiscardPile = () => {
    if (!isMyTurn || session.turnPhase !== 'DRAW') return;
    if (session.discardPile.length === 0) return;
    clearError();
    const count = session.discardPile.length;
    const updated = GameEngine.robDiscardPile(session);
    setSession(updated);
    handleAddLog(`👉 ${humanPlayer.countryFlag} ${humanPlayer.name} robó el pozo entero (${count} fichas).`);
  };

  const handleTileSelect = (tile: Tile) => {
    if (!isMyTurn) return;
    setSelectedTileIds(prev => {
      const next = new Set(prev);
      if (next.has(tile.id)) {
        next.delete(tile.id);
      } else {
        next.add(tile.id);
      }
      return next;
    });
  };

  const handleSortHand = () => {
    const updatedPlayers = session.players.map(p => {
      if (p.id === 'human') {
        return {
          ...p,
          hand: GameEngine.sortHand(p.hand)
        };
      }
      return p;
    });
    setSession({
      ...session,
      players: updatedPlayers
    });
    setSelectedTileIds(new Set());
  };

  const handleMeld = (type: 'RUN' | 'SET') => {
    if (!isMyTurn || session.turnPhase !== 'PLAY') return;
    if (selectedTileIds.size < 3) {
      setErrorMsg('Debes seleccionar al menos 3 fichas para bajar juego.');
      return;
    }

    const selectedTiles = humanPlayer.hand.filter(t => selectedTileIds.has(t.id));
    const { updatedSession, error } = GameEngine.meldFromPlayer(session, selectedTiles, type);

    if (error) {
      setErrorMsg(error);
    } else {
      clearError();
      setSession(updatedSession);
      setSelectedTileIds(new Set());
      const formatted = GameEngine.formatMeld({ id: '', tiles: selectedTiles, type });
      handleAddLog(`🎉 ${humanPlayer.countryFlag} ${humanPlayer.name} bajó juego: ${formatted}`);
    }
  };

  const handleAppendToMeld = (meldId: string) => {
    if (!isMyTurn || session.turnPhase !== 'PLAY') return;
    if (selectedTileIds.size !== 1) {
      setErrorMsg('Selecciona exactamente una sola ficha de tu atril para acomodar.');
      return;
    }

    const tileId = Array.from(selectedTileIds)[0];
    const tile = humanPlayer.hand.find(t => t.id === tileId);
    if (!tile) return;

    const { updatedSession, error } = GameEngine.appendTileToMeld(session, meldId, tile);

    if (error) {
      setErrorMsg(error);
    } else {
      clearError();
      setSession(updatedSession);
      setSelectedTileIds(new Set());
      handleAddLog(`🧩 ${humanPlayer.countryFlag} ${humanPlayer.name} acomodó ${GameEngine.formatTile(tile)} en la mesa.`);
    }
  };

  const handleDiscard = () => {
    if (!isMyTurn || session.turnPhase !== 'PLAY') return;
    if (selectedTileIds.size !== 1) {
      setErrorMsg('Selecciona una sola ficha de tu atril para descartar y terminar tu turno.');
      return;
    }

    const tileId = Array.from(selectedTileIds)[0];
    const tile = humanPlayer.hand.find(t => t.id === tileId);
    if (!tile) return;

    const { updatedSession, error } = GameEngine.discardTile(session, tile);

    if (error) {
      setErrorMsg(error);
    } else {
      clearError();
      setSession(updatedSession);
      setSelectedTileIds(new Set());
      handleAddLog(`🎴 ${humanPlayer.countryFlag} ${humanPlayer.name} descartó ${GameEngine.formatTile(tile)}.`);

      // Check if closed
      if (updatedSession.winnerId !== null) {
        handleEndRound(updatedSession);
      }
    }
  };

  const handleEndRound = (finalSession: GameSession) => {
    const isWin = finalSession.winnerId === 'human';
    const scoreEarned = humanPlayer.roundScore;
    const opponents = players.filter(p => !p.isHuman).map(p => `${p.name} ${p.countryFlag}`).join(', ');

    setTimeout(() => {
      onGameEnd(isWin ? 'WIN' : 'LOSS', scoreEarned, opponents);
    }, 4000);
  };

  return (
    <div className="min-h-screen bg-felt-dark flex flex-col justify-between items-stretch overflow-hidden select-none relative">
      {/* 1. HEADER / OPONENTS PANEL */}
      <div className="bg-black/45 border-b border-white/5 px-4 py-2.5 flex items-center justify-between shadow-md z-10">
        <button
          onClick={() => onNavigateTo('menu')}
          className="flex items-center text-gray-300 hover:text-white transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span className="font-extrabold text-xs ml-1">Salir</span>
        </button>

        {/* Players Status Horizontal List */}
        <div className="flex items-center gap-3 flex-1 justify-center max-w-sm px-4">
          {players.map((player) => {
            const isTurn = players[session.currentPlayerIndex].id === player.id;
            return (
              <div
                key={player.id}
                className={`relative px-2 py-1 rounded-xl flex flex-col items-center border transition-all ${
                  isTurn
                    ? 'bg-amber-500/20 border-gold shadow-[0_0_10px_rgba(255,179,0,0.3)]'
                    : 'bg-black/20 border-transparent'
                }`}
              >
                <div className="flex items-center gap-1">
                  <span className="text-xl filter drop-shadow">{player.countryFlag}</span>
                  <span className={`text-[10px] font-black uppercase ${player.isHuman ? 'text-amber-400' : 'text-gray-300'}`}>
                    {player.name.slice(0, 7)}
                  </span>
                </div>
                <div className="flex items-center gap-1.5 mt-0.5">
                  <span className="text-[9px] text-gray-400 font-bold">Fichas: {player.hand.length}</span>
                  <span className="text-[9px] text-gold font-bold">Pts: {player.totalScore}</span>
                </div>
                {isTurn && (
                  <span className="absolute -bottom-1.5 w-2 h-2 rounded-full bg-gold animate-ping" />
                )}
              </div>
            );
          })}
        </div>

        {/* Active game mode display */}
        <div className="text-right">
          <span className="bg-mahogany-medium border border-mahogany-light/30 text-gold text-[9px] font-black px-2 py-0.5 rounded-full uppercase tracking-wider">
            {mode}
          </span>
        </div>
      </div>

      {/* 2. MAIN FELT GAME BOARD */}
      <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3.5 relative flex flex-col items-stretch justify-start">
        {/* Error notifications */}
        {errorMsg && (
          <div className="bg-red-500/95 text-white text-xs font-bold px-3.5 py-2 rounded-xl flex items-center justify-between shadow-lg animate-bounce mx-auto max-w-md">
            <div className="flex items-center gap-1.5">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorMsg}</span>
            </div>
            <button onClick={clearError} className="text-[10px] text-white/80 font-black ml-4 uppercase">
              OK
            </button>
          </div>
        )}

        {/* Table Pile & Discard Deck */}
        <div className="grid grid-cols-3 gap-4 max-w-sm mx-auto items-center justify-center bg-black/15 rounded-2xl p-3 border border-white/5">
          {/* Card Pool / Pilas */}
          <button
            onClick={handleDrawFromPool}
            disabled={!isMyTurn || session.turnPhase !== 'DRAW' || session.pool.length === 0}
            className={`h-20 rounded-xl relative border flex flex-col items-center justify-center shadow-lg transition transform ${
              isMyTurn && session.turnPhase === 'DRAW' && session.pool.length > 0
                ? 'bg-mahogany-medium border-gold hover:scale-105 active:scale-95'
                : 'bg-mahogany-dark border-transparent opacity-80'
            }`}
          >
            <div className="absolute inset-1 rounded-lg border border-dashed border-white/10" />
            <span className="text-2xl font-bold text-amber-500 select-none">🎴</span>
            <span className="text-[10px] font-bold text-gray-400 mt-1">Robar ({session.pool.length})</span>
          </button>

          {/* Discard Pile / Pozo */}
          <div className="flex flex-col items-center">
            {session.discardPile.length > 0 ? (
              <button
                onClick={handleDrawFromDiscard}
                disabled={!isMyTurn || session.turnPhase !== 'DRAW'}
                className={`h-20 w-16 bg-tile-ivory rounded-xl border-b-4 border-tile-shadow shadow-md flex flex-col items-center justify-between p-1.5 relative transition transform ${
                  isMyTurn && session.turnPhase === 'DRAW'
                    ? 'border-gold border-2 hover:scale-105'
                    : 'border-transparent'
                }`}
              >
                {/* Visual top tile */}
                {(() => {
                  const top = session.discardPile[session.discardPile.length - 1];
                  const colorClass = {
                    RED: 'text-tile-red',
                    BLUE: 'text-tile-blue',
                    BLACK: 'text-tile-black',
                    ORANGE: 'text-tile-orange',
                    JOKER: 'text-tile-joker'
                  }[top.color];

                  return (
                    <div className="flex flex-col items-center justify-between h-full w-full">
                      <div className="flex justify-between w-full text-[10px] font-black leading-none">
                        <span>{top.isJoker ? '🃏' : top.number}</span>
                      </div>
                      <span className={`text-2xl font-black ${colorClass}`}>
                        {top.isJoker ? 'J' : top.number}
                      </span>
                      <span className="text-[8px] text-gray-500 font-bold leading-none">
                        Total: {session.discardPile.length}
                      </span>
                    </div>
                  );
                })()}
              </button>
            ) : (
              <div className="h-20 w-16 border-2 border-dashed border-white/15 rounded-xl flex items-center justify-center text-[10px] font-bold text-gray-500">
                Vacío
              </div>
            )}
          </div>

          {/* Special Action: Robar todo el Pozo (Burako style) */}
          <button
            onClick={handleRobDiscardPile}
            disabled={!isMyTurn || session.turnPhase !== 'DRAW' || session.discardPile.length === 0}
            className={`h-20 rounded-xl px-2 relative border flex flex-col items-center justify-center shadow-lg text-center transition transform ${
              isMyTurn && session.turnPhase === 'DRAW' && session.discardPile.length > 0
                ? 'bg-amber-500 border-gold text-mahogany-dark font-black hover:scale-105 active:scale-95'
                : 'bg-black/30 border-transparent text-gray-500 font-bold opacity-60'
            }`}
          >
            <span className="text-xl">🙌</span>
            <span className="text-[10px] leading-tight font-black uppercase tracking-wider mt-1">Robar Pozo</span>
          </button>
        </div>

        {/* Melded Groups on the board */}
        <div className="flex-1 bg-black/20 rounded-2xl p-3 border border-white/5 space-y-3 min-h-[140px] overflow-y-auto">
          <h4 className="text-[10px] font-black text-amber-500/80 uppercase tracking-widest border-b border-white/5 pb-1 flex items-center justify-between">
            <span>Juegos Bajados en la Mesa</span>
            {isMyTurn && session.turnPhase === 'PLAY' && selectedTileIds.size === 1 && (
              <span className="text-emerald-400 font-bold animate-pulse text-[9px] lowercase tracking-normal bg-emerald-500/10 px-2 py-0.5 rounded-full">
                toca un juego para acomodar ficha seleccionada
              </span>
            )}
          </h4>

          {session.meldedGroups.length === 0 && (
            <div className="h-24 flex items-center justify-center text-center text-gray-500 text-xs font-semibold">
              No hay juegos bajados aún en la mesa.<br />¡Sé el primero en bajar juego!
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
            {session.meldedGroups.map((meld) => {
              const isCanastaType = GameEngine.isCanasta(meld);
              const isPure = GameEngine.isPureCanasta(meld, mode);

              return (
                <div
                  key={meld.id}
                  onClick={() => handleAppendToMeld(meld.id)}
                  className={`bg-mahogany-dark/80 rounded-xl p-2.5 border transition-all relative select-none flex flex-col gap-2 ${
                    isMyTurn && session.turnPhase === 'PLAY' && selectedTileIds.size === 1
                      ? 'border-emerald-500 hover:bg-emerald-950/20 cursor-pointer shadow-[0_0_8px_rgba(16,185,129,0.3)] hover:scale-[1.01]'
                      : isCanastaType
                      ? isPure
                        ? 'border-amber-400 shadow-[0_0_10px_rgba(251,191,36,0.2)]'
                        : 'border-purple-400'
                      : 'border-mahogany-light/30'
                  }`}
                >
                  {/* Meld title / canasta badge */}
                  <div className="flex justify-between items-center">
                    <span className="text-[9px] font-black uppercase text-gray-400">
                      {meld.type === 'RUN' ? 'Escalera' : 'Pierna'}
                    </span>
                    {isCanastaType && (
                      <div className={`flex items-center gap-0.5 px-1.5 py-0.5 rounded text-[8px] font-black uppercase tracking-wider ${
                        isPure ? 'bg-amber-500 text-mahogany-dark' : 'bg-purple-600 text-white'
                      }`}>
                        <Star className="w-2.5 h-2.5 fill-current" />
                        <span>Canasta {isPure ? 'Pura' : 'Impura'}</span>
                      </div>
                    )}
                  </div>

                  {/* Melded cards layout */}
                  <div className="flex flex-wrap gap-1">
                    {meld.tiles.map((tile) => {
                      const colorClass = {
                        RED: 'text-tile-red',
                        BLUE: 'text-tile-blue',
                        BLACK: 'text-tile-black',
                        ORANGE: 'text-tile-orange',
                        JOKER: 'text-tile-joker'
                      }[tile.color];

                      return (
                        <div
                          key={tile.id}
                          className="h-10 w-7 bg-tile-ivory rounded border-b-2 border-tile-shadow flex flex-col items-center justify-center relative shadow"
                        >
                          <span className={`text-[11px] font-black ${colorClass}`}>
                            {tile.isJoker ? '🃏' : tile.number}
                          </span>
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Round Winner / Overlay screen */}
      {session.winnerId !== null && (
        <div className="absolute inset-0 bg-black/85 backdrop-blur-sm z-50 flex flex-col items-center justify-center p-6 text-center">
          <div className="bg-mahogany-dark border-2 border-gold rounded-3xl p-8 max-w-sm w-full shadow-2xl relative overflow-hidden">
            <div className="absolute top-0 inset-x-0 bg-gradient-to-b from-amber-500/10 to-transparent h-24" />
            <span className="text-5xl block mb-3">🏆</span>
            <h3 className="text-2xl font-black text-gold uppercase tracking-wider">¡Ronda Terminada!</h3>
            <p className="text-xs text-gray-300 font-bold mt-1">Cálculo final de mesa física</p>

            <div className="space-y-3 mt-6 border-y border-white/5 py-4">
              {players.map((player) => (
                <div key={player.id} className="flex justify-between items-center text-sm">
                  <div className="flex items-center gap-1.5 font-extrabold text-white">
                    <span>{player.countryFlag}</span>
                    <span>{player.name}</span>
                  </div>
                  <div className="text-right">
                    <span className="text-xs text-gray-400 mr-2">Puntos:</span>
                    <span className={`font-black ${player.roundScore >= 0 ? 'text-emerald-400' : 'text-red-400'}`}>
                      {player.roundScore >= 0 ? '+' : ''}{player.roundScore}
                    </span>
                  </div>
                </div>
              ))}
            </div>

            <p className="text-xs text-gray-400 mt-4 font-semibold animate-pulse">
              Redirigiendo de vuelta al club en instantes...
            </p>
          </div>
        </div>
      )}

      {/* 3. LOG FOOTER (MODAL LOG TOGGLE) */}
      <div className="bg-black/30 border-t border-white/5 z-10 flex flex-col">
        {showLogs && (
          <div
            ref={logContainerRef}
            className="h-28 overflow-y-auto bg-black/85 p-3 font-mono text-[10px] text-emerald-400 border-b border-white/10 flex flex-col gap-1 shadow-inner"
          >
            {logs.map((log, idx) => (
              <div key={idx} className="flex items-start gap-1">
                <span className="text-gray-500 font-bold">&gt;</span>
                <span>{log}</span>
              </div>
            ))}
          </div>
        )}

        {/* Action button bar */}
        <div className="px-4 py-2 flex items-center justify-between border-b border-white/5 bg-black/20 gap-2">
          {/* Logs toggle */}
          <button
            onClick={() => setShowLogs(!showLogs)}
            className="flex items-center gap-1 text-gray-400 hover:text-white text-xs font-bold px-2 py-1.5 rounded-lg bg-black/15 border border-white/5"
          >
            <MessageSquare className="w-3.5 h-3.5" />
            <span>Mensajes {showLogs ? '▲' : '▼'}</span>
          </button>

          {/* Gameplay Actions for Turn Phase: PLAY */}
          {isMyTurn && session.turnPhase === 'PLAY' ? (
            <div className="flex items-center gap-2">
              <button
                onClick={() => handleMeld('SET')}
                disabled={selectedTileIds.size < 3}
                className={`text-[10px] font-black uppercase px-2.5 py-1.5 rounded-lg transition transform active:scale-95 ${
                  selectedTileIds.size >= 3
                    ? 'bg-amber-500 hover:bg-amber-400 text-mahogany-dark shadow-md'
                    : 'bg-black/30 text-gray-500 cursor-not-allowed border border-white/5'
                }`}
              >
                Meld Set (Pierna)
              </button>
              <button
                onClick={() => handleMeld('RUN')}
                disabled={selectedTileIds.size < 3}
                className={`text-[10px] font-black uppercase px-2.5 py-1.5 rounded-lg transition transform active:scale-95 ${
                  selectedTileIds.size >= 3
                    ? 'bg-amber-500 hover:bg-amber-400 text-mahogany-dark shadow-md'
                    : 'bg-black/30 text-gray-500 cursor-not-allowed border border-white/5'
                }`}
              >
                Meld Run (Escalera)
              </button>
              <button
                onClick={handleDiscard}
                disabled={selectedTileIds.size !== 1}
                className={`text-[10px] font-black uppercase px-3 py-1.5 rounded-lg transition transform active:scale-95 ${
                  selectedTileIds.size === 1
                    ? 'bg-red-600 hover:bg-red-500 text-white shadow-md'
                    : 'bg-black/30 text-gray-500 cursor-not-allowed border border-white/5'
                }`}
              >
                Descartar
              </button>
            </div>
          ) : (
            <div className="text-xs text-gray-400 font-extrabold flex items-center gap-1">
              <AlertCircle className="w-3.5 h-3.5 text-amber-500 animate-pulse" />
              <span>
                {session.winnerId !== null
                  ? 'Fin de juego.'
                  : isMyTurn
                  ? session.turnPhase === 'DRAW'
                    ? 'FASE ROBO: Toma de la pila o pozo'
                    : 'FASE ACCIÓN: Juega o descarta'
                  : `Turno de: ${players[session.currentPlayerIndex].countryFlag} ${players[session.currentPlayerIndex].name}`}
              </span>
            </div>
          )}

          <button
            onClick={handleSortHand}
            className="text-[10px] text-gray-300 font-black uppercase bg-black/15 border border-white/5 px-2.5 py-1.5 rounded-lg hover:text-white"
          >
            Ordenar Atril
          </button>
        </div>
      </div>

      {/* 4. PHYSICAL SCRABBLE WOODEN TILE RACK (MAX 20% HEIGHT) */}
      <div className="h-[20vh] max-h-[140px] bg-gradient-to-b from-mahogany-light via-mahogany-medium to-mahogany-dark border-t-2 border-maple relative px-3.5 py-2.5 flex flex-col justify-start items-stretch shadow-inner select-none z-10">
        {/* Rack Wood Edge Overlay */}
        <div className="absolute top-0 inset-x-0 h-[3px] bg-white/20" />
        <div className="absolute bottom-0 inset-x-0 h-[4px] bg-black/40" />

        {/* Small header for the wooden rack */}
        <div className="flex justify-between items-center mb-1 bg-black/10 px-2 py-0.5 rounded select-none">
          <span className="text-[9px] font-black text-maple uppercase tracking-widest leading-none">
            Tu Atril Físico
          </span>
          <span className="text-[9px] font-bold text-gray-400 leading-none">
            {humanPlayer.hand.length} Fichas restantes
          </span>
        </div>

        {/* Horizontal scrollable Ivory Tiles wrapper */}
        <div className="flex-1 overflow-x-auto flex items-center justify-start gap-1.5 pb-2.5 pt-1.5 px-1 scroll-smooth">
          {humanPlayer.hand.map((tile) => {
            const isSelected = selectedTileIds.has(tile.id);
            const colorClass = {
              RED: 'text-tile-red',
              BLUE: 'text-tile-blue',
              BLACK: 'text-tile-black',
              ORANGE: 'text-tile-orange',
              JOKER: 'text-tile-joker'
            }[tile.color];

            return (
              <button
                key={tile.id}
                onClick={() => handleTileSelect(tile)}
                className={`h-16 w-11 min-w-[44px] bg-gradient-to-b from-tile-highlight via-tile-ivory to-tile-shadow rounded-lg border-b-4 border-tile-shadow flex flex-col items-center justify-between p-1.5 relative select-none shrink-0 transition-transform ${
                  isSelected
                    ? 'border-gold border-2 -translate-y-3 shadow-[0_4px_12px_rgba(255,179,0,0.4)]'
                    : 'hover:-translate-y-1'
                }`}
              >
                {/* Upper left number indicator */}
                <div className="w-full text-left leading-none">
                  <span className={`text-[8px] font-extrabold ${colorClass}`}>
                    {tile.isJoker ? '🃏' : tile.number}
                  </span>
                </div>

                {/* Central main display number */}
                <span className={`text-xl font-black ${colorClass} leading-none select-none`}>
                  {tile.isJoker ? 'J' : tile.number}
                </span>

                {/* Bottom subtle bar style decoration */}
                <div className="w-full h-[3px] bg-black/5 rounded-full mt-1" />
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
