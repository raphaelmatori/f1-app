import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Driver } from "@app-models/driver.interface";
import { Paginate } from "@app-models/paginate.interface";
import { environment } from "@env/environment";
import { map, Observable, switchMap } from "rxjs";
import { F1Service } from "./interfaces/f1.service.interface";

@Injectable()
export class ErgastService extends F1Service {
  private readonly API_F1_SERIES = environment.apiF1Series;
  private readonly ENDPOINTS = environment.endpoints;
  private readonly CONFIG = environment.config;

  private readonly INITIAL_YEAR_FOR_F1_SERIES = 1950;
  private readonly CURRENT_YEAR = new Date().getFullYear();

  private readonly YEAR_OUT_OF_RANGE_ERROR_MESSAGE = `The 'year' parameter must be in the interval ${this.INITIAL_YEAR_FOR_F1_SERIES} ~ ${this.CURRENT_YEAR}`;

  constructor(private httpClient: HttpClient) {
    super();
  }

  public override getSeasons(
    offset: number = 0,
    limit: number = this.CONFIG.pagination.pageLimit
  ): Observable<Paginate> {
    return this.httpClient
      .get<any>(
        `${this.API_F1_SERIES}/${this.ENDPOINTS.seasons}?offset=${offset}&limit=${limit}`
      )
      .pipe(
        map((response) => {
          return {
            limit: +response?.MRData?.limit,
            offset: +response?.MRData?.offset,
            total: +response?.MRData?.total,
            results: response?.MRData?.SeasonTable?.Seasons,
          };
        })
      );
  }

  public override getSeasonsBetweenYearInterval(
    initialYear: number,
    finalYear: number
  ): Observable<Paginate> {
    if (initialYear > finalYear) {
      let aux = initialYear;
      initialYear = finalYear;
      finalYear = aux;
    }

    // This calculation assumes that there will be one and only one, F1 competition each year.
    const limit = finalYear - initialYear + 1;
    const offset = initialYear - this.INITIAL_YEAR_FOR_F1_SERIES;

    return this.getSeasons(offset, limit);
  }

  public override getSeasonsFromYearUntilNow(
    year: number
  ): Observable<Paginate> {
    if (year < this.INITIAL_YEAR_FOR_F1_SERIES || year > this.CURRENT_YEAR) {
      throw new Error(this.YEAR_OUT_OF_RANGE_ERROR_MESSAGE);
    }
    return this.getSeasonsBetweenYearInterval(year, this.CURRENT_YEAR);
  }

  public override getWorldChampionByYear(
    year: number
  ): Observable<Driver | null> {
    return this.httpClient
      .get<any>(`${this.API_F1_SERIES}/${this.ENDPOINTS.allRacesOfAYear(year)}`)
      .pipe(
        switchMap((response) => {
          const races = response?.MRData?.RaceTable?.Races;
          const lastRaceHasAlreadyTakenPlace =
            races[races.length - 1].date <
            new Date().toISOString().slice(0, 10);

          if (!lastRaceHasAlreadyTakenPlace) {
            return new Observable<null>((observer) => observer.next(null));
          }

          return this.httpClient
            .get<any>(
              `${this.API_F1_SERIES}/${this.ENDPOINTS.worldChampionByYear(
                year
              )}`
            )
            .pipe(
              map(
                (response) =>
                  response?.MRData?.StandingsTable?.StandingsLists[0]
                    ?.DriverStandings[0]?.Driver
              )
            );
        })
      );
  }

  public override getAllRacesWinnersOfAYear(
    year: number
  ): Observable<Paginate> {
    return this.httpClient
      .get<any>(
        `${this.API_F1_SERIES}/${this.ENDPOINTS.allRacesWinnersOfAYear(year)}`
      )
      .pipe(
        map((response) => {
          return {
            limit: response?.MRData?.limit,
            offset: response?.MRData?.offset,
            total: response?.MRData?.total,
            results: response?.MRData?.RaceTable?.Races,
          };
        })
      );
  }
}
