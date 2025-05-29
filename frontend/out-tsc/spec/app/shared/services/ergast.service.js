import { __decorate } from "tslib";
import { Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { map } from "rxjs";
import { F1Service } from "./interfaces/f1.service.interface";
let ErgastService = class ErgastService extends F1Service {
    httpClient;
    API_F1_SERIES = environment.apiF1Series;
    ENDPOINTS = environment.endpoints;
    constructor(httpClient) {
        super();
        this.httpClient = httpClient;
    }
    getWorldChampions() {
        return this.httpClient
            .get(`${this.API_F1_SERIES}/${this.ENDPOINTS.worldChampions}`)
            .pipe(map((response) => {
            const mappedResponse = new Map;
            response.map((champion) => mappedResponse.set(champion.year, champion));
            return mappedResponse;
        }));
    }
    getAllRacesWinnersOfAYear(year) {
        return this.httpClient
            .get(`${this.API_F1_SERIES}/${this.ENDPOINTS.allRacesWinnersOfAYear(year)}`)
            .pipe(map((response) => {
            return response;
        }));
    }
};
ErgastService = __decorate([
    Injectable()
], ErgastService);
export { ErgastService };
