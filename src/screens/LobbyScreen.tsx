import React, { useState, useEffect, useRef } from 'react';
import { UserProfile, Player, ChatMessage, GameMode } from '../types';
import { Send, ArrowLeft, Users, Wifi, ShieldAlert, Play } from 'lucide-react';

interface LobbyScreenProps {
  profile: UserProfile;
  roomCode: string;
  setRoomCode: (code: string) => void;
  isOnlineMode: boolean;
  gameMode: GameMode;
  onNavigateTo: (screen: string) => void;
  onStartGameplay: (players: Player[]) => void;
}

export const LobbyScreen: React.FC<LobbyScreenProps> = ({
  profile,
  roomCode,
  setRoomCode: _setRoomCode,
  isOnlineMode,
  gameMode,
  onNavigateTo,
  onStartGameplay,
}) => {
  const [players, setPlayers] = useState<Player[]>([
    { id: 'human', name: profile.name, countryFlag: profile.countryFlag, hand: [], isHuman: true, isBoughtMuerto: false, totalScore: 0, roundScore: 0 }
  ]);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [inputText, setInputText] = useState('');
  const [ping, setPing] = useState(42);
  const [logs, setLogs] = useState<string[]>(['Conectando con el servidor en la nube...']);
  const chatEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll chat to bottom
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages]);

  // Simulate network updates and players joining
  useEffect(() => {
    let pingInterval: any;
    if (isOnlineMode) {
      pingInterval = setInterval(() => {
        setPing(Math.floor(Math.random() * (110 - 35 + 1)) + 35);
      }, 2000);
    }

    const timer1 = setTimeout(() => {
      addLog(`Conexión establecida. Sala: ${roomCode}`);
      if (isOnlineMode) {
        addLog('Esperando que la familia se una...');
      }
    }, 1000);

    let timers: any[] = [];

    if (isOnlineMode) {
      // Tía Sofía joins
      timers.push(setTimeout(() => {
        setPlayers(prev => [
          ...prev,
          { id: 'ai_1', name: 'Tía Sofía', countryFlag: '🇪🇸', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 }
        ]);
        addLog('Tía Sofía 🇪🇸 se ha unido a la sala.');
        addChatMsg('Tía Sofía', '🇪🇸', '¡Hola a todos! Qué lindo jugar en familia.');
      }, 2500));

      // Tío Wagner joins
      timers.push(setTimeout(() => {
        setPlayers(prev => [
          ...prev,
          { id: 'ai_2', name: 'Tío Wagner', countryFlag: '🇺🇸', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 }
        ]);
        addLog('Tío Wagner 🇺🇸 se ha unido a la sala.');
        addChatMsg('Tío Wagner', '🇺🇸', 'Hello family! Ready to win this game.');
      }, 4500));

      // Mamá joins
      timers.push(setTimeout(() => {
        setPlayers(prev => [
          ...prev,
          { id: 'ai_3', name: 'Mamá', countryFlag: '🇮🇹', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 }
        ]);
        addLog('Próximo jugador: Mamá 🇮🇹 se ha unido.');
        addChatMsg('Mamá', '🇮🇹', '¡Ciao! Preparé café mientras jugamos.');
      }, 6500));
    } else {
      // Offline mode: bots are instantly available
      timers.push(setTimeout(() => {
        setPlayers([
          { id: 'human', name: profile.name, countryFlag: profile.countryFlag, hand: [], isHuman: true, isBoughtMuerto: false, totalScore: 0, roundScore: 0 },
          { id: 'ai_1', name: 'Sofía Bot', countryFlag: '🇪🇸', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 },
          { id: 'ai_2', name: 'Wagner Bot', countryFlag: '🇺🇸', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 },
          { id: 'ai_3', name: 'Mamá Bot', countryFlag: '🇮🇹', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 }
        ]);
        addLog('Modo offline listo. Bots conectados de manera local.');
      }, 1500));
    }

    return () => {
      clearInterval(pingInterval);
      clearTimeout(timer1);
      timers.forEach(clearTimeout);
    };
  }, [isOnlineMode, roomCode]);

  const addLog = (msg: string) => {
    setLogs(prev => [...prev, msg]);
  };

  const addChatMsg = (name: string, flag: string, text: string) => {
    setChatMessages(prev => [
      ...prev,
      { id: Math.random().toString(), senderName: name, senderFlag: flag, message: text, timestamp: Date.now() }
    ]);
  };

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim()) return;

    addChatMsg(profile.name, profile.countryFlag, inputText.trim());
    setInputText('');

    // Simulate response
    if (isOnlineMode) {
      setTimeout(() => {
        const responses = [
          { name: 'Tía Sofía', flag: '🇪🇸', text: '¡Exacto! Me encanta.' },
          { name: 'Tío Wagner', flag: '🇺🇸', text: 'Haha, totally agree with you!' },
          { name: 'Mamá', flag: '🇮🇹', text: '¡Mucha suerte! ☕' }
        ];
        const randomRes = responses[Math.floor(Math.random() * responses.length)];
        addChatMsg(randomRes.name, randomRes.flag, randomRes.text);
      }, 1500 + Math.random() * 1500);
    }
  };

  const handleLaunchGame = () => {
    // Start game with currently joined players
    // Ensure we have at least some bots if online family is still joining
    let finalPlayers = [...players];
    if (isOnlineMode && finalPlayers.length < 4) {
      const remainingBots = [
        { id: 'ai_1', name: 'Tía Sofía', countryFlag: '🇪🇸', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 },
        { id: 'ai_2', name: 'Tío Wagner', countryFlag: '🇺🇸', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 },
        { id: 'ai_3', name: 'Mamá', countryFlag: '🇮🇹', hand: [], isHuman: false, isBoughtMuerto: false, totalScore: 0, roundScore: 0 }
      ];
      const currentIds = new Set(finalPlayers.map(p => p.id));
      remainingBots.forEach(bot => {
        if (!currentIds.has(bot.id)) {
          finalPlayers.push(bot);
        }
      });
    }
    onStartGameplay(finalPlayers);
  };

  return (
    <div className="min-h-screen bg-felt-dark flex flex-col items-stretch justify-between p-4 max-w-lg mx-auto border-x border-white/5 shadow-2xl">
      {/* Top Navigation / App bar */}
      <div className="flex items-center justify-between border-b border-white/10 pb-3 mb-3">
        <button
          onClick={() => onNavigateTo('menu')}
          className="flex items-center gap-1 text-gray-300 hover:text-white transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span className="font-bold text-sm">Menú</span>
        </button>

        <div className="text-center">
          <span className="text-[10px] text-gold font-bold uppercase tracking-widest block">Sala de Espera</span>
          <span className="text-lg font-black text-white">{roomCode}</span>
        </div>

        <div className="flex items-center gap-2">
          {isOnlineMode ? (
            <div className="flex items-center gap-1.5 bg-emerald-500/10 border border-emerald-500/20 px-2 py-1 rounded-full">
              <Wifi className="w-3.5 h-3.5 text-emerald-400" />
              <span className="text-[10px] font-black text-emerald-400">{ping}ms</span>
            </div>
          ) : (
            <div className="flex items-center gap-1.5 bg-amber-500/10 border border-amber-500/20 px-2 py-1 rounded-full">
              <ShieldAlert className="w-3.5 h-3.5 text-amber-400" />
              <span className="text-[10px] font-black text-amber-400">Offline</span>
            </div>
          )}
        </div>
      </div>

      {/* Main Grid: Players List & Logs */}
      <div className="flex-1 overflow-y-auto space-y-4 mb-4 pr-1">
        {/* Players Panel */}
        <div className="bg-black/25 rounded-2xl p-4 border border-white/5 shadow-lg">
          <div className="flex items-center gap-2 mb-3">
            <Users className="w-4 h-4 text-amber-400" />
            <h3 className="text-xs font-black text-amber-400 uppercase tracking-widest">Jugadores en la Sala ({players.length}/4)</h3>
          </div>
          <div className="grid grid-cols-2 gap-2.5">
            {players.map((player) => (
              <div
                key={player.id}
                className="bg-mahogany-dark/80 border border-mahogany-light/20 rounded-xl p-3 flex items-center gap-3"
              >
                <span className="text-3xl filter drop-shadow">{player.countryFlag}</span>
                <div>
                  <h4 className="font-extrabold text-sm text-white leading-tight truncate max-w-[100px]">{player.name}</h4>
                  <span className="text-[10px] text-emerald-400 font-bold uppercase tracking-wide">Listo</span>
                </div>
              </div>
            ))}
            {/* Waiting placeholders */}
            {Array.from({ length: 4 - players.length }).map((_, idx) => (
              <div
                key={idx}
                className="bg-black/15 border border-white/5 border-dashed rounded-xl p-3 flex items-center justify-center text-gray-500 text-xs font-bold py-6"
              >
                <span>Esperando conexión...</span>
              </div>
            ))}
          </div>
        </div>

        {/* System Logs Panel */}
        <div className="bg-black/35 rounded-2xl p-3.5 border border-white/5 shadow-inner h-28 overflow-y-auto flex flex-col gap-1 text-[11px] font-mono text-emerald-400/90">
          {logs.map((log, idx) => (
            <div key={idx} className="flex items-start gap-1">
              <span className="text-gray-500 font-bold">&gt;</span>
              <span>{log}</span>
            </div>
          ))}
        </div>

        {/* Live Chat Panel */}
        <div className="bg-black/30 rounded-2xl border border-white/5 shadow-lg flex flex-col h-56">
          <div className="border-b border-white/5 px-3 py-2 text-xs font-bold text-gray-300">
            Chat Familiar
          </div>
          <div className="flex-1 overflow-y-auto p-3 space-y-2.5">
            {chatMessages.length === 0 && (
              <div className="text-center text-gray-500 text-xs py-8 font-medium">
                Envia un mensaje para empezar la charla familiar...
              </div>
            )}
            {chatMessages.map((msg) => (
              <div
                key={msg.id}
                className={`flex flex-col max-w-[85%] ${
                  msg.senderName === profile.name ? 'ml-auto items-end' : 'mr-auto items-start'
                }`}
              >
                <span className="text-[10px] text-gray-400 font-bold mb-0.5 flex items-center gap-1">
                  <span>{msg.senderFlag}</span>
                  <span>{msg.senderName}</span>
                </span>
                <div
                  className={`rounded-2xl px-3.5 py-2 text-sm font-semibold shadow-md ${
                    msg.senderName === profile.name
                      ? 'bg-amber-500 text-mahogany-dark rounded-tr-none'
                      : 'bg-mahogany-medium text-white rounded-tl-none'
                  }`}
                >
                  {msg.message}
                </div>
              </div>
            ))}
            <div ref={chatEndRef} />
          </div>

          <form onSubmit={handleSendMessage} className="p-2 border-t border-white/5 flex gap-2">
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="Escribe un mensaje..."
              className="flex-1 bg-black/40 border border-gray-700 focus:border-amber-500 rounded-xl px-3.5 py-2 text-sm font-semibold text-white outline-none outline-0"
            />
            <button
              type="submit"
              className="bg-amber-500 text-mahogany-dark p-2 rounded-xl hover:bg-amber-400 transition transform active:scale-95"
            >
              <Send className="w-5 h-5 fill-current stroke-none" />
            </button>
          </form>
        </div>
      </div>

      {/* Launch Actions */}
      <button
        onClick={handleLaunchGame}
        className="w-full bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-mahogany-dark font-black py-4 px-6 rounded-2xl shadow-xl transform transition active:scale-[0.98] flex items-center justify-center gap-2 text-lg"
      >
        <Play className="w-5 h-5 fill-current" />
        <span>COMENZAR {gameMode}</span>
      </button>
    </div>
  );
};
