import React, { useState } from 'react';
import { UserProfile, GameMode } from '../types';
import { HelpCircle, History, Play, Globe, Cpu } from 'lucide-react';

interface MainMenuScreenProps {
  profile: UserProfile;
  onUpdateProfile: (name: string, flag: string) => void;
  onStartGame: (mode: GameMode, isOnline: boolean) => void;
  onNavigateTo: (screen: string) => void;
}

export const MainMenuScreen: React.FC<MainMenuScreenProps> = ({
  profile,
  onUpdateProfile,
  onStartGame,
  onNavigateTo,
}) => {
  const [editName, setEditName] = useState(profile.name);
  const [selectedFlag, setSelectedFlag] = useState(profile.countryFlag);
  const flags = ['🇦🇷', '🇪🇸', '🇺🇸', '🇮🇹', '🇲🇽', '🇧🇷', '🇨🇴', '🇺🇾'];

  const xpNeeded = profile.level * 150;
  const progressPercent = Math.min(100, Math.floor((profile.xp / xpNeeded) * 100));

  return (
    <div className="min-h-screen bg-gradient-to-b from-felt-light via-felt-medium to-felt-dark p-6 flex flex-col items-center justify-start overflow-y-auto">
      {/* Premium Logo Header */}
      <div className="mt-8 text-center flex flex-col items-center">
        <div className="flex items-center gap-2">
          <span className="text-3xl">🏆</span>
          <h1 className="text-3xl md:text-4xl font-black text-gold tracking-wider select-none drop-shadow">
            BURAKO &amp; RUMMY
          </h1>
        </div>
        <span className="text-xs font-bold text-gray-300 tracking-[0.25em] mt-1 select-none">
          FAMILY CLUB
        </span>
      </div>

      {/* Profile & Level Card */}
      <div className="w-full max-w-md bg-mahogany-dark rounded-2xl p-5 border border-mahogany-light/30 shadow-2xl mt-8">
        <div className="flex items-center justify-between border-b border-mahogany-light/20 pb-4 mb-4">
          <div className="flex items-center gap-3">
            <span className="text-4xl filter drop-shadow">{selectedFlag}</span>
            <div>
              <h2 className="font-extrabold text-lg text-white">{profile.name}</h2>
              <p className="text-xs text-amber-400 font-bold">Nivel {profile.level}</p>
            </div>
          </div>
          <div className="text-right">
            <span className="text-xs text-gray-400 font-semibold block">Récord</span>
            <span className="text-sm font-bold text-emerald-400">{profile.wins} Victoras</span>
            <span className="text-gray-400 text-xs px-1">/</span>
            <span className="text-sm font-bold text-red-400">{profile.losses} Derrotas</span>
          </div>
        </div>

        {/* XP progress bar */}
        <div className="mb-2">
          <div className="flex justify-between text-xs text-gray-300 mb-1 font-bold">
            <span>Experiencia (XP)</span>
            <span>{profile.xp} / {xpNeeded} XP</span>
          </div>
          <div className="w-full bg-black/40 h-3 rounded-full overflow-hidden p-[2px]">
            <div
              className="bg-gradient-to-r from-amber-400 to-amber-600 h-full rounded-full transition-all duration-500"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>
      </div>

      {/* Edit Profile Configuration */}
      <div className="w-full max-w-md bg-black/35 rounded-2xl p-5 border border-white/5 mt-6 shadow-xl">
        <h3 className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-3">Configurar Perfil</h3>
        <div className="flex flex-col gap-3">
          <div>
            <label className="block text-xs text-gray-300 mb-1 font-bold">Nombre / Apodo</label>
            <input
              type="text"
              value={editName}
              onChange={(e) => {
                setEditName(e.target.value);
                onUpdateProfile(e.target.value, selectedFlag);
              }}
              className="w-full bg-black/40 border border-gray-600 focus:border-amber-500 rounded-lg px-3 py-2 text-white font-semibold outline-none transition-colors"
              placeholder="Ej: Aldo Wagner"
            />
          </div>
          <div>
            <label className="block text-xs text-gray-300 mb-1 font-bold">Bandera de País / Origen</label>
            <div className="grid grid-cols-8 gap-2">
              {flags.map((flag) => (
                <button
                  key={flag}
                  onClick={() => {
                    setSelectedFlag(flag);
                    onUpdateProfile(editName, flag);
                  }}
                  className={`text-2xl p-1 rounded-lg transition-transform hover:scale-110 active:scale-95 ${
                    selectedFlag === flag ? 'bg-amber-500/30 border border-amber-500' : 'bg-transparent border border-transparent'
                  }`}
                >
                  {flag}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Game Mode Launcher Actions */}
      <div className="w-full max-w-md flex flex-col gap-3 mt-6">
        <button
          onClick={() => onStartGame('BURACO', true)}
          className="w-full bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-mahogany-dark text-base font-extrabold py-3.5 px-4 rounded-xl shadow-lg transform transition active:scale-[0.98] flex items-center justify-between"
        >
          <div className="flex items-center gap-3">
            <Globe className="w-5 h-5 stroke-[2.5]" />
            <div className="text-left">
              <span className="block font-black leading-tight text-lg">BURAKO ONLINE</span>
              <span className="block text-[10px] text-mahogany-dark/85 font-bold uppercase tracking-wider">Juego Familiar Internacional</span>
            </div>
          </div>
          <Play className="w-5 h-5 fill-current" />
        </button>

        <button
          onClick={() => onStartGame('RUMMY', true)}
          className="w-full bg-white hover:bg-gray-100 text-felt-dark text-base font-extrabold py-3.5 px-4 rounded-xl shadow-lg transform transition active:scale-[0.98] flex items-center justify-between"
        >
          <div className="flex items-center gap-3">
            <Globe className="w-5 h-5 stroke-[2.5]" />
            <div className="text-left">
              <span className="block font-black leading-tight text-lg text-emerald-900">RUMMY ONLINE</span>
              <span className="block text-[10px] text-emerald-800/80 font-bold uppercase tracking-wider">Club Familiar Internacional</span>
            </div>
          </div>
          <Play className="w-5 h-5 fill-emerald-900 stroke-none" />
        </button>

        <div className="grid grid-cols-2 gap-3 mt-1">
          <button
            onClick={() => onStartGame('BURACO', false)}
            className="bg-emerald-800/60 hover:bg-emerald-800/80 border border-emerald-500/30 text-white font-extrabold py-3.5 px-3 rounded-xl shadow transform transition active:scale-[0.98] flex flex-col items-center justify-center gap-1.5"
          >
            <Cpu className="w-5 h-5 text-emerald-400" />
            <span className="text-xs uppercase tracking-wider font-black">Burako Bots</span>
          </button>
          <button
            onClick={() => onStartGame('RUMMY', false)}
            className="bg-emerald-800/60 hover:bg-emerald-800/80 border border-emerald-500/30 text-white font-extrabold py-3.5 px-3 rounded-xl shadow transform transition active:scale-[0.98] flex flex-col items-center justify-center gap-1.5"
          >
            <Cpu className="w-5 h-5 text-emerald-400" />
            <span className="text-xs uppercase tracking-wider font-black">Rummy Bots</span>
          </button>
        </div>

        <div className="grid grid-cols-2 gap-3 mt-1">
          <button
            onClick={() => onNavigateTo('rules')}
            className="bg-mahogany-medium/60 hover:bg-mahogany-medium/80 border border-mahogany-light/20 text-white font-bold py-3.5 px-3 rounded-xl shadow transform transition active:scale-[0.98] flex items-center justify-center gap-2 text-sm"
          >
            <HelpCircle className="w-4 h-4 text-amber-400" />
            <span>Reglas</span>
          </button>
          <button
            onClick={() => onNavigateTo('history')}
            className="bg-mahogany-medium/60 hover:bg-mahogany-medium/80 border border-mahogany-light/20 text-white font-bold py-3.5 px-3 rounded-xl shadow transform transition active:scale-[0.98] flex items-center justify-center gap-2 text-sm"
          >
            <History className="w-4 h-4 text-amber-400" />
            <span>Historial</span>
          </button>
        </div>
      </div>

      {/* Footer credits */}
      <p className="text-[10px] text-gray-400 font-medium select-none mt-10">
        Aldo Wagner &copy; 2026 • Diseñado para jugar en familia
      </p>
    </div>
  );
};
