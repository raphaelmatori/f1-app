import { Circuit } from './circuit.interface';
import { Result } from './result.interface';

export interface Race {
  season: number;
  round: number;
  raceName: string;
  date: string;
  time: string;
  circuit: Circuit;
  results: Result[];
  
  // Derived fields used in the UI
  winner?: Result['driver'];
  constructor?: Result['constructor'];
} 