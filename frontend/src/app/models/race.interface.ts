import { Result } from "@app-models/result.interface";
import { Circuit } from "./circuit.interface";
export interface Race {
  season: string;
  round: string;
  url: string;
  raceName: string;
  Circuit: Circuit;
  Results: Result[];
}
