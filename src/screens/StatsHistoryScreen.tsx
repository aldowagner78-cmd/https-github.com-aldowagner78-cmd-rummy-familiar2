import React from 'react';
import { MatchHistory } from '../types';
import { ArrowLeft, History, Trophy, TrendingUp, Award } from 'lucide-react';

interface StatsHistoryScreenProps {
  history: MatchHistory[];
  onClearHistory?: () => void;
  onNavigateTo: (screen: string) => void;
}

export const StatsHistoryScreen: React.FC<StatsHistoryScreenProps> = ({
  history,
  onClearHistory,
  onNavigateTo,
}) => {
  // Aggregate stats
  const totalMatches = history.length;
  const wins = history.filter(m => m.result === 'WIN').length;
  const losses = totalMatches - wins;
  const winPercent = totalMatches > 0 ? Math.round((wins / totalMatches) * 100) : 0;
  const totalPoints = history.reduce((acc, m) => acc + m.score, 0);
  const avgPoints = totalMatches > 0 ? Math.round(totalPoints / totalMatches) : 0;
  const maxPoints = totalMatches > 0 ? Math.max(...history.map(m => m.score)) : 0;

  return (
    <div className="min-h-screen bg-felt-dark text-white flex flex-col items-stretch justify-start p-4 max-w-lg mx-auto border-x border-white/5 shadow-2xl overflow-y-auto">
      {/* Top App Bar */}
      <div className="flex items-center justify-between border-b border-white/10 pb-3 mb-4">
        <button
          onClick={() => onNavigateTo('menu')}
          className="flex items-center gap-1 text-gray-300 hover:text-white transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span className="font-bold text-sm">Volver</span>
        </button>
        <h1 className="text-base font-black text-white flex items-center gap-1.5 uppercase tracking-wider">
          <History className="w-4 h-4 text-gold" />
          <span>Historial de Club</span>
        </h1>
        <div className="w-10" />
      </div>

      <div className="space-y-4 flex-1">
        {/* STATS OVERVIEW CARDS */}
        <div className="grid grid-cols-2 gap-3">
          <div className="bg-mahogany-dark/80 rounded-2xl p-4 border border-mahogany-light/20 flex flex-col items-center justify-center text-center">
            <TrendingUp className="w-5 h-5 text-gold mb-1" />
            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest leading-none">Efectividad</span>
            <span className="text-2xl font-black text-white mt-1">{winPercent}%</span>
            <span className="text-[10px] text-emerald-400 font-bold mt-1">
              {wins}V / {losses}D
            </span>
          </div>

          <div className="bg-mahogany-dark/80 rounded-2xl p-4 border border-mahogany-light/20 flex flex-col items-center justify-center text-center">
            <Trophy className="w-5 h-5 text-gold mb-1" />
            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest leading-none">Puntos Máx</span>
            <span className="text-2xl font-black text-white mt-1">{maxPoints}</span>
            <span className="text-[10px] text-amber-400 font-bold mt-1">
              Promedio: {avgPoints} pts
            </span>
          </div>
        </div>

        {/* MATCH HISTORY LIST */}
        <div className="bg-black/25 rounded-2xl p-4 border border-white/5 flex-1 flex flex-col min-h-[300px]">
          <div className="flex items-center justify-between border-b border-white/5 pb-2 mb-3">
            <h3 className="text-xs font-black text-amber-500 uppercase tracking-widest flex items-center gap-1">
              <Award className="w-4 h-4" />
              <span>Partidas Recientes ({totalMatches})</span>
            </h3>
            {onClearHistory && totalMatches > 0 && (
              <button
                onClick={onClearHistory}
                className="text-[10px] text-red-400 hover:text-red-300 font-bold uppercase transition-colors"
              >
                Limpiar
              </button>
            )}
          </div>

          {history.length === 0 ? (
            <div className="flex-1 flex flex-col items-center justify-center text-center py-12 text-gray-500">
              <History className="w-10 h-10 mb-2 stroke-[1.5]" />
              <p className="text-xs font-bold leading-relaxed">
                Aún no has disputado partidas en el club.<br />
                ¡Comienza un juego desde el menú principal!
              </p>
            </div>
          ) : (
            <div className="space-y-2.5 overflow-y-auto max-h-[320px]">
              {history.map((match) => (
                <div
                  key={match.id}
                  className="bg-black/20 border border-white/5 rounded-xl p-3 flex items-center justify-between"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className={`text-[9px] font-black uppercase px-2 py-0.5 rounded-full ${
                        match.mode === 'BURACO' ? 'bg-blue-600/20 text-blue-400' : 'bg-emerald-600/20 text-emerald-400'
                      }`}>
                        {match.mode}
                      </span>
                      <span className="text-[10px] text-gray-400 font-semibold">
                        {new Date(match.timestamp).toLocaleDateString('es-ES')}
                      </span>
                    </div>
                    <p className="text-[11px] font-bold text-gray-300 truncate max-w-[180px]">
                      Vs: {match.opponents}
                    </p>
                  </div>

                  <div className="text-right">
                    <span className={`text-xs font-black uppercase px-2.5 py-1 rounded-lg ${
                      match.result === 'WIN'
                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                        : 'bg-red-500/10 text-red-400 border border-red-500/20'
                    }`}>
                      {match.result === 'WIN' ? 'Victoria' : 'Derrota'}
                    </span>
                    <span className="block text-xs font-black text-white mt-1.5">
                      {match.score >= 0 ? '+' : ''}{match.score} pts
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
