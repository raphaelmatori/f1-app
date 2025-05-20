import { Injectable } from "@angular/core";
import { Driver } from "@app-models/driver.interface";
import { Paginate } from "@app-models/paginate.interface";
import { Observable } from "rxjs";

@Injectable()
export class F1Service {
  public getSeasons(offset: number, limit: number): Observable<Paginate> {
    return new Observable<Paginate>();
  }

  public getSeasonsBetweenYearInterval(
    initialYear: number,
    finalYear: number
  ): Observable<Paginate> {
    return new Observable<Paginate>();
  }

  public getSeasonsFromYearUntilNow(year: number): Observable<Paginate> {
    return new Observable<Paginate>();
  }

  public getWorldChampionByYear(year: number): Observable<Driver | null> {
    return new Observable<Driver>();
  }

  public getAllRacesWinnersOfAYear(year: number): Observable<Paginate> {
    return new Observable<Paginate>();
  }
}
