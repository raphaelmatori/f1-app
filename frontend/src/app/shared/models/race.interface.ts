import { Driver } from './driver.interface';
import { Constructor } from './constructor.interface';

export interface Race {
  season: string;
  round: string;
  url: string;
  raceName: string;
  date: string;
  circuitName: string;
  winner?: Driver;
  constructor?: Constructor;
  Circuit: {
    circuitId: string;
    url: string;
    circuitName: string;
    Location: {
      lat: string;
      long: string;
      locality: string;
      country: string;
    };
  };
  Results: {
    number: string;
    position: string;
    positionText: string;
    points: string;
    Driver: Driver;
    Constructor: Constructor;
    grid: string;
    laps: string;
    status: string;
    Time?: {
      millis: string;
      time: string;
    };
    FastestLap?: {
      rank: string;
      lap: string;
      Time: {
        time: string;
      };
      AverageSpeed: {
        units: string;
        speed: string;
      };
    };
  }[];
} 