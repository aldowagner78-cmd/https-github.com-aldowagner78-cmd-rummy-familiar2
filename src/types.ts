export type TileColor = 'RED' | 'BLUE' | 'BLACK' | 'ORANGE' | 'JOKER';

export interface Tile {
  id: number;
  number: number; // 1..13, or 0/99 for Joker
  color: TileColor;
  isJoker: boolean;
  isTempSelected?: boolean;
}

export type GameMode = 'RUMMY' | 'BURACO';
export type TurnPhase = 'DRAW' | 'PLAY' | 'DISCARD';

export interface Meld {
  id: string;
  tiles: Tile[];
  type: 'RUN' | 'SET';
}

export interface Player {
  id: string;
  name: string;
  avatarUrl?: string;
  countryFlag: string;
  hand: Tile[];
  isHuman: boolean;
  isBoughtMuerto: boolean;
  totalScore: number;
  roundScore: number;
  isReady?: boolean;
}

export interface ChatMessage {
  id: string;
  senderName: string;
  senderFlag: string;
  message: string;
  timestamp: number;
}

export interface UserProfile {
  name: string;
  countryFlag: string;
  wins: number;
  losses: number;
  level: number;
  xp: number;
}

export interface MatchHistory {
  id: string;
  mode: GameMode;
  score: number;
  result: 'WIN' | 'LOSS';
  timestamp: number;
  opponents: string;
}

export interface GameSession {
  mode: GameMode;
  players: Player[];
  pool: Tile[];
  discardPile: Tile[];
  meldedGroups: Meld[];
  currentPlayerIndex: number;
  turnPhase: TurnPhase;
  muerto1: Tile[];
  muerto2: Tile[];
  muerto1Bought: boolean;
  muerto2Bought: boolean;
  winnerId: string | null;
  roundLog: string[];
}
