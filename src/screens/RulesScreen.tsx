import React from 'react';
import { ArrowLeft, BookOpen, HelpCircle, Trophy } from 'lucide-react';

interface RulesScreenProps {
  onNavigateTo: (screen: string) => void;
}

export const RulesScreen: React.FC<RulesScreenProps> = ({ onNavigateTo }) => {
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
          <BookOpen className="w-4 h-4 text-gold" />
          <span>Reglas de la Mesa</span>
        </h1>
        <div className="w-10" />
      </div>

      <div className="space-y-5 flex-1 pr-1">
        {/* Intro */}
        <p className="text-xs text-gray-300 leading-relaxed text-center font-medium bg-black/10 p-3 rounded-xl border border-white/5">
          🏆 ¡Bienvenido a la mesa familiar! Este club simula con precisión las reglas de tablero clásicas de Rummy y Burako.
        </p>

        {/* 1. GENERAL MECHANICS */}
        <div className="bg-mahogany-dark/80 rounded-2xl p-4 border border-mahogany-light/20 shadow-md">
          <h2 className="text-sm font-black text-gold uppercase tracking-wider flex items-center gap-2 mb-2.5">
            <HelpCircle className="w-4 h-4" />
            <span>Mecánicas Comunes</span>
          </h2>
          <ul className="text-xs text-gray-200 space-y-2 list-disc list-inside leading-relaxed">
            <li>Cada ronda comienza repartiendo <span className="text-gold font-bold">11 fichas</span> a cada jugador.</li>
            <li>En tu turno, primero debes robar <span className="text-amber-400 font-bold">1 ficha de la pila</span> o del pozo.</li>
            <li>Durante la fase de juego, puedes bajar combinaciones válidas o acomodar fichas en los juegos que ya bajaste a la mesa.</li>
            <li>Para finalizar tu turno, debes elegir exactamente una ficha de tu atril y <span className="text-red-400 font-bold">descartarla en el pozo</span>.</li>
          </ul>
        </div>

        {/* 2. RUMMY CLASSIC RULES */}
        <div className="bg-black/25 rounded-2xl p-4 border border-white/5 shadow-md">
          <h2 className="text-sm font-black text-emerald-400 uppercase tracking-wider flex items-center gap-2 mb-2.5">
            <span>🔴 RUMMY CLÁSICO</span>
          </h2>
          <div className="space-y-3.5 text-xs text-gray-300 leading-relaxed">
            <div>
              <h3 className="font-extrabold text-white mb-1">Combinaciones válidas:</h3>
              <ul className="list-disc list-inside space-y-1 pl-1">
                <li><span className="text-gold font-bold">Escalera (Run):</span> Tres o más fichas del mismo color con números consecutivos (Ej: 🔴 4, 🔴 5, 🔴 6).</li>
                <li><span className="text-gold font-bold">Pierna (Set):</span> Tres o cuatro fichas del mismo número con diferentes colores (Ej: 🔴 8, 🔵 8, ⚫ 8).</li>
              </ul>
            </div>
            <div>
              <h3 className="font-extrabold text-white mb-1">Comodines (Wildcards):</h3>
              <p>Los <span className="text-purple-400 font-bold">Jokers 🃏</span> sirven para reemplazar cualquier ficha en la mesa. No puedes bajar más comodines que fichas normales en un mismo juego.</p>
            </div>
            <div>
              <h3 className="font-extrabold text-white mb-1">Condición de Cierre:</h3>
              <p>Para cerrar la ronda, debes quedarte sin fichas en la mano tras descartar la última ficha en el pozo.</p>
            </div>
          </div>
        </div>

        {/* 3. BURACO PREMIUM RULES */}
        <div className="bg-black/25 rounded-2xl p-4 border border-white/5 shadow-md">
          <h2 className="text-sm font-black text-amber-500 uppercase tracking-wider flex items-center gap-2 mb-2.5">
            <span>🔵 BURACO PROFESIONAL</span>
          </h2>
          <div className="space-y-3.5 text-xs text-gray-300 leading-relaxed">
            <div>
              <h3 className="font-extrabold text-white mb-1">Comodines Especiales:</h3>
              <p>Además de los <span className="text-purple-400 font-bold">Jokers 🃏</span>, todas las fichas de <span className="text-gold font-bold">Número 2</span> de cualquier color funcionan como comodines (comodines de Burako).</p>
            </div>
            <div>
              <h3 className="font-extrabold text-white mb-1">Mecánica del Pozo ("Robar el Pozo"):</h3>
              <p>Al robar en Burako, tienes la opción de <span className="text-amber-400 font-bold">tomar TODO el pozo de descarte</span> (no solo la de arriba). Es la clave para acumular fichas y armar canastas.</p>
            </div>
            <div>
              <h3 className="font-extrabold text-white mb-1">Mecánica de "Muerto":</h3>
              <p>Se colocan dos pilas adicionales de 11 fichas al costado de la mesa. El primer jugador que se queda sin fichas toma una de estas pilas (Muerto) para seguir jugando de manera ininterrumpida.</p>
            </div>
            <div>
              <h3 className="font-extrabold text-white mb-1">Canastas y Bonificaciones:</h3>
              <ul className="list-disc list-inside space-y-1 pl-1">
                <li><span className="text-gold font-bold">Canasta:</span> Un juego de mesa con 7 o más fichas.</li>
                <li><span className="text-emerald-400 font-bold">Canasta Pura (+200 pts):</span> Sin comodines ni 2s comodín.</li>
                <li><span className="text-purple-400 font-bold">Canasta Impura (+100 pts):</span> Contiene uno o más comodines.</li>
              </ul>
            </div>
          </div>
        </div>

        {/* 4. POINT SCORING SYSTEM */}
        <div className="bg-mahogany-dark/80 rounded-2xl p-4 border border-mahogany-light/20 shadow-md">
          <h2 className="text-sm font-black text-gold uppercase tracking-wider flex items-center gap-2 mb-2.5">
            <Trophy className="w-4 h-4" />
            <span>Puntuación de Fichas (Cálculo)</span>
          </h2>
          <div className="grid grid-cols-2 gap-4 text-xs">
            <div>
              <h3 className="font-extrabold text-emerald-400 mb-1 border-b border-white/5 pb-0.5">Rummy</h3>
              <ul className="space-y-1">
                <li>Número 1: 10 pts</li>
                <li>Fichas 2-10: Valor facial</li>
                <li>Fichas 11-13: 10 pts</li>
                <li>Comodín (Joker): 50 pts</li>
              </ul>
            </div>
            <div>
              <h3 className="font-extrabold text-amber-500 mb-1 border-b border-white/5 pb-0.5">Burako</h3>
              <ul className="space-y-1">
                <li>Número 1: 15 pts</li>
                <li>Número 2 (comodín): 20 pts</li>
                <li>Fichas 3-7: 5 pts</li>
                <li>Fichas 8-13: 10 pts</li>
                <li>Comodín (Joker): 50 pts</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
