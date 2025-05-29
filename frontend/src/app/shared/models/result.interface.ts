import { Driver } from './driver.interface';
import { Constructor } from './constructor.interface';
import { Time } from './time.interface';

export interface Result {
  position: string;
  points: string;
  grid: string;
  laps: string;
  status: string;
  driver: Driver;
  constructor: Constructor;
  time: Time | null;
} 