import { HttpClient } from "@angular/common/http";
import {
  HttpClientTestingModule,
  HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { httpAllRacesWinnersOfAYearMock } from "@app-mocks/http-all-races-winners-of-a-year.mock";
import { httpSeasonsMock } from "@app-mocks/http-seasons.mock";
import { httpWorldChampionByYearMock } from "@app-mocks/http-world-champion-by-year.mock";
import { environment } from "@env/environment";
import { ErgastService } from "./ergast.service";

describe("ErgastService", () => {
  const API_F1_SERIES = environment.apiF1Series;
  const ENDPOINTS = environment.endpoints;
  const CONFIG = environment.config;

  let service: ErgastService;
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErgastService],
    });

    service = TestBed.inject(ErgastService);
    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  it("should call getSeasons without passing pagination parameters ", () => {
    // Given
    const limit = 30;

    // When
    service.getSeasons().subscribe((page) => {
      expect(page.results.length).toBe(limit);
    });

    // Then
    const req = httpMock.expectOne(
      `${API_F1_SERIES}/${ENDPOINTS.seasons}?offset=0&limit=${limit}`
    );
    expect(req.request.method).toBe("GET");
    req.flush(httpSeasonsMock);
  });

  it("should call getSeasons passing pagination parameters ", () => {
    // Given
    const offset = 10;
    const limit = 20;

    // When
    service.getSeasons(offset, limit).subscribe((page) => {
      expect(page.results.length).toBe(limit);
    });

    // Then
    const req = httpMock.expectOne(
      `${API_F1_SERIES}/${ENDPOINTS.seasons}?offset=${offset}&limit=${limit}`
    );
    expect(req.request.method).toBe("GET");
  });

  it("should call getSeasonsBetweenYearInterval with a range between a given year interval", () => {
    // Given
    const initialYear = 2015;
    const finalYear = 2000;
    const offset = 50; // skip the first 50 -> from 1950 until 1999
    const limit = 16; // get 16 -> from 2000 until 2015

    spyOn(service, "getSeasons").and.callThrough();

    // When
    service.getSeasonsBetweenYearInterval(initialYear, finalYear).subscribe();

    //Then
    const req = httpMock.expectOne(
      `${API_F1_SERIES}/${ENDPOINTS.seasons}?offset=${offset}&limit=${limit}`
    );
    expect(req.request.method).toBe("GET");
    req.flush(httpSeasonsMock);

    expect(service.getSeasons).toHaveBeenCalledTimes(1);
  });

  it("should call getSeasonsFromYearUntilNow with a given year and return the season list from that year until now", () => {
    // Given
    const givenYear = 2015;
    const currentYear = new Date().getFullYear();
    const limit = currentYear - givenYear + 1;
    const offset = givenYear - CONFIG.initialYearForF1Series;
    spyOn(service, "getSeasonsBetweenYearInterval").and.callThrough();

    // When
    service.getSeasonsFromYearUntilNow(givenYear).subscribe();

    //Then
    const req = httpMock.expectOne(
      `${API_F1_SERIES}/${ENDPOINTS.seasons}?offset=${offset}&limit=${limit}`
    );
    expect(req.request.method).toBe("GET");
    req.flush(httpSeasonsMock);

    expect(service.getSeasonsBetweenYearInterval).toHaveBeenCalledTimes(1);
  });

  it("should throw an error when the given year is less than the initial year for f1 series", () => {
    // Given
    const givenYear = 1949;
    spyOn(service, "getSeasonsBetweenYearInterval").and.callThrough();

    // Then
    expect(function () {
      service.getSeasonsFromYearUntilNow(givenYear);
    }).toThrow();
  });

  it("should return the F1 World Champion for a given year", () => {
    // Given
    const givenYear = 2015;
    // When
    service.getWorldChampionByYear(givenYear).subscribe();
    // Then
    const req1 = httpMock.expectOne(
      `${API_F1_SERIES}/${ENDPOINTS.allRacesOfAYear(givenYear)}`
    );

    expect(req1.request.method).toBe("GET");
    req1.flush(httpAllRacesWinnersOfAYearMock);

    const req2 = httpMock.expectOne(
      `${API_F1_SERIES}/${ENDPOINTS.worldChampionByYear(givenYear)}`
    );

    expect(req2.request.method).toBe("GET");
    req2.flush(httpWorldChampionByYearMock);
  });

  it("should not return the F1 World Champion for a given year when the championship is still opened", () => {
    // Given
    const givenYear = 2015;
    // When
    service.getWorldChampionByYear(givenYear).subscribe();
    // Then
    const req1 = httpMock.expectOne(
      `${API_F1_SERIES}/${ENDPOINTS.allRacesOfAYear(givenYear)}`
    );

    expect(req1.request.method).toBe("GET");
    req1.flush({
      ...httpAllRacesWinnersOfAYearMock,
      MRData: {
        RaceTable: {
          Races: [
            {
              date: new Date().toISOString().slice(0, 10),
            },
          ],
        },
      },
    });
  });

  it("should get all winners of a race for a given year", () => {
    // Given
    const givenYear = 2015;
    // When
    service.getAllRacesWinnersOfAYear(givenYear).subscribe();
    // Then
    const req = httpMock.expectOne(
      `${API_F1_SERIES}/${ENDPOINTS.allRacesWinnersOfAYear(givenYear)}`
    );
    expect(req.request.method).toBe("GET");
    req.flush(httpAllRacesWinnersOfAYearMock);
  });
});
