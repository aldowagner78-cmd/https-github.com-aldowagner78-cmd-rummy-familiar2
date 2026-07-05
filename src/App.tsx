import React, { useState } from 'react';
import { useLocalStorage } from './hooks/useLocalStorage';
import { UserProfile, MatchHistory, Player, GameMode } from './types';
import { MainMenuScreen } from './screens/MainMenuScreen';
import { LobbyScreen } from './screens/LobbyScreen';
import { GameScreen } from './screens/GameScreen';
import { RulesScreen } from './screens/RulesScreen';
import { StatsHistoryScreen } from './screens/StatsHistoryScreen';

export const App: React.FC = () => {
  const [currentScreen, setCurrentScreen] = useState<string>('menu');
  const [selectedMode, setSelectedMode] = useState<GameMode>('BURACO');
  const [isOnline, setIsOnline] = useState<boolean>(true);
  const [roomCode, setRoomCode] = useState<string>('FAMILIA77');
  const [lobbyPlayers, setLobbyPlayers] = useState<Player[]>([]);

  // Local storage profile and history
  const [profile, setProfile] = useLocalStorage<UserProfile>('rummy_user_profile', {
    name: 'Jugador Familiar',
    countryFlag: '🇦🇷',
    wins: 0,
    losses: 0,
    level: 1,
    xp: 0
  });

  const [history, setHistory] = useLocalStorage<MatchHistory[]>('rummy_match_history', []);

  const handleUpdateProfile = (name: string, flag: string) => {
    setProfile(prev => ({
      ...prev,
      name,
      countryFlag: flag
    }));
  };

  const handleStartGame = (mode: GameMode, online: boolean) => {
    setSelectedMode(mode);
    setIsOnline(online);

    if (online) {
      // Generate standard random room code
      const codes = ['FAMILIA77', 'DOMINGO_CLUB', 'MESA_CRIOLLA', 'BURAKO_ARG', 'ASADO_RUMMY', 'ITALIA_JUEGO'];
      const randomCode = codes[Math.floor(Math.random() * codes.length)] + Math.floor(100 + Math.random() * 900);
      setRoomCode(randomCode);
      setCurrentScreen('lobby');
    } else {
      // Start offline bots immediately
      const bots = [
        { id: 'human', name: profile.name, countryFlag: profile.countryFlag, hand: [], isHuman: true, isBoughtMuerto: false, totalScore: 0, roundScore: 0 },
        { id: 'ai_1', name: 'Sofía Bot', countryFlag: '🇪🇸', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 },
        { id: 'ai_2', name: 'Wagner Bot', countryFlag: '🇺🇸', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 },
        { id: 'ai_3', name: 'Mamá Bot', countryFlag: '🇮🇹', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 }
      ];
      setLobbyPlayers(bots);
      setCurrentScreen('game');
    }
  };

  const handleStartGameplay = (players: Player[]) => {
    setLobbyPlayers(players);
    setCurrentScreen('game');
  };

  const handleGameEnd = (result: 'WIN' | 'LOSS', score: number, opponents: string) => {
    // 1. Calculate XP rewards
    let xpEarned = result === 'WIN' ? 120 : 45;
    if (score > 0) xpEarned += Math.floor(score * 0.1); // 10% of score converted to bonus XP

    let newXp = profile.xp + xpEarned;
    let newLevel = profile.level;
    let xpNeeded = newLevel * 150;

    // Loop for level ups
    while (newXp >= xpNeeded) {
      newXp -= xpNeeded;
      newLevel += 1;
      xpNeeded = newLevel * 150;
    }

    // Update profile
    setProfile(prev => ({
      ...prev,
      wins: prev.wins + (result === 'WIN' ? 1 : 0),
      losses: prev.losses + (result === 'LOSS' ? 1 : 0),
      xp: newXp,
      level: newLevel
    }));

    // 2. Log to Match History
    const newHistoryEntry: MatchHistory = {
      id: Math.random().toString(36).substring(2, 9),
      mode: selectedMode,
      score,
      result,
      timestamp: Date.now(),
      opponents
    };

    setHistory(prev => [newHistoryEntry, ...prev]);

    // Go to history screen to review accomplishments
    setCurrentScreen('history');
  };

  const handleClearHistory = () => {
    if (window.confirm('¿Seguro que deseas borrar el historial de partidas?')) {
      setHistory([]);
    }
  };

  return (
    <div className="w-full min-h-screen bg-grey-dark select-none text-white">
      {currentScreen === 'menu' && (
        <MainMenuScreen
          profile={profile}
          onUpdateProfile={handleUpdateProfile}
          onStartGame={handleStartGame}
          onNavigateTo={setCurrentScreen}
        />
      )}

      {currentScreen === 'lobby' && (
        <LobbyScreen
          profile={profile}
          roomCode={roomCode}
          setRoomCode={setRoomCode}
          isOnlineMode={isOnline}
          gameMode={selectedMode}
          onNavigateTo={setCurrentScreen}
          onStartGameplay={handleStartGameplay}
        />
      )}

      {currentScreen === 'game' && (
        <GameScreen
          initialPlayers={lobbyPlayers}
          mode={selectedMode}
          isOnlineMode={isOnline}
          onNavigateTo={setCurrentScreen}
          onGameEnd={handleGameEnd}
        />
      )}

      {currentScreen === 'rules' && (
        <RulesScreen onNavigateTo={setCurrentScreen} />
      )}

      {currentScreen === 'history' && (
        <StatsHistoryScreen
          history={history}
          onClearHistory={handleClearHistory}
          onNavigateTo={setCurrentScreen}
        />
      )}
    </div>
  );
};
