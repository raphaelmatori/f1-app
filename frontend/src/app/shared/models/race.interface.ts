import { Driver } from './driver.interface';
import { Constructor } from './constructor.interface';
import { Circuit } from './circuit.interface';

export interface Race {
  season: number;
  round: number;
  url: string;
  raceName: string;
  date: string;
  circuitName: string;
  winner?: Driver;
  constructor?: Constructor;
  Circuit: Circuit;
  Results: any[];
} 