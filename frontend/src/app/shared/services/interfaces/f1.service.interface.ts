import { Injectable } from "@angular/core";
import type { Driver } from "@app-models/driver.interface";
import { Observable } from "rxjs";

@Injectable()
export class F1Service {
  public getWorldChampions(): Observable<Map<number,Driver> | null> {
    return new Observable<Map<number,Driver>>();
  }

  public getAllRacesWinnersOfAYear(year: number): Observable<any> {
    void year;
    return new Observable<any>();
  }
}
