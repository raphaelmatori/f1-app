import { DriverStanding } from './driver-standing.interface';
export interface Standing {
  season: number;
  round: number;
  DriverStandings: DriverStanding[];
}
