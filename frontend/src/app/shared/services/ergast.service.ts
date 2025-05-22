import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Driver } from "@app-models/driver.interface";
import { environment } from "@env/environment";
import { map, Observable } from "rxjs";
import { F1Service } from "./interfaces/f1.service.interface";
import {Champion} from "@app/models/champion.interface";

@Injectable()
export class ErgastService extends F1Service {
  private readonly API_F1_SERIES = environment.apiF1Series;
  private readonly ENDPOINTS = environment.endpoints;

  constructor(private httpClient: HttpClient) {
    super();
  }

  public override getWorldChampions(): Observable<Map<number,Driver> | null> {
    return this.httpClient
        .get<any>(
            `${this.API_F1_SERIES}/${this.ENDPOINTS.worldChampions}`
        )
        .pipe(
            map(
                (response) => {
                    const mappedResponse = new Map<number, Driver>
                    response.map((champion: Champion) => mappedResponse.set(champion.year, champion))
                    return mappedResponse;
                }
            )
        );
  }

  public override getAllRacesWinnersOfAYear(
    year: number
  ): Observable<any> {
    return this.httpClient
      .get<any>(
        `${this.API_F1_SERIES}/${this.ENDPOINTS.allRacesWinnersOfAYear(year)}`
      )
      .pipe(
        map((response) => {
          return response;
        })
      );
  }
}
