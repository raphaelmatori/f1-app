import { HttpClientModule } from "@angular/common/http";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, convertToParamMap } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { httpAllRacesWinnersOfAYearMock } from "@app-mocks/http-all-races-winners-of-a-year.mock";
import { emptyPageMock } from "@app-mocks/http-empty-page.mock";
import { httpWorldChampionByYearMock } from "@app-mocks/http-world-champion-by-year.mock";
import { Driver } from "@app-models/driver.interface";
import { F1Service } from "@app-shared/services/interfaces/f1.service.interface";
import { of, throwError } from "rxjs";
import { RaceWinnersComponent } from "./race-winners.component";

describe("RaceWinnersComponent", () => {
  let component: RaceWinnersComponent;
  let fixture: ComponentFixture<RaceWinnersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      declarations: [RaceWinnersComponent],
      imports: [RouterTestingModule, HttpClientModule],
      providers: [
        {
          provide: F1Service,
          useValue: {
            getWorldChampionByYear: () => of(httpWorldChampionByYearMock),
            getAllRacesWinnersOfAYear: () =>
              of({
                ...emptyPageMock,
                results: httpAllRacesWinnersOfAYearMock.MRData.RaceTable.Races,
              }),
          },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(
              convertToParamMap({
                year: 2020,
              })
            ),
          },
        },
      ],
    }).compileComponents();
  });

  it("should create", () => {
    // Given
    fixture = TestBed.createComponent(RaceWinnersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    // Then
    expect(component).toBeTruthy();
  });

  it("should select item on item click", () => {
    // Given
    fixture = TestBed.createComponent(RaceWinnersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    spyOn(console, "log");

    // When
    component.selectedItemHandler("123");

    // Then
    expect(console.log).toHaveBeenCalledWith("123");
  });

  it("should set loading to false after fetching races data", () => {
    // Given
    fixture = TestBed.createComponent(RaceWinnersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.isLoading = true;

    // When
    component.fetchRacesData().subscribe();

    // Then
    expect(component.isLoading).toBe(false);
  });

  it("should throw an error when year is invalid", async () => {
    // Given
    await TestBed.configureTestingModule({
      declarations: [RaceWinnersComponent],
      imports: [RouterTestingModule, HttpClientModule],
      providers: [
        {
          provide: F1Service,
          useValue: {
            getWorldChampionByYear: () => of(httpWorldChampionByYearMock),
            getAllRacesWinnersOfAYear: () =>
              of({
                ...emptyPageMock,
                results: httpAllRacesWinnersOfAYearMock.MRData.RaceTable.Races,
              }),
          },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(
              convertToParamMap({
                year: "wrong",
              })
            ),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RaceWinnersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    // Then
    expect(component.isLoading).toBe(false);
  });

  it("should throw an error when getWorldChampionByYear fails", async () => {
    // Given
    await TestBed.configureTestingModule({
      declarations: [RaceWinnersComponent],
      imports: [RouterTestingModule, HttpClientModule],
      providers: [
        {
          provide: F1Service,
          useValue: {
            getWorldChampionByYear: () => {
              throw Error("error");
            },
            getAllRacesWinnersOfAYear: () =>
              of({
                ...emptyPageMock,
                results: httpAllRacesWinnersOfAYearMock.MRData.RaceTable.Races,
              }),
          },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(
              convertToParamMap({
                year: 2020,
              })
            ),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RaceWinnersComponent);
    component = fixture.componentInstance;

    spyOn(component["f1Service"], "getWorldChampionByYear").and.callFake(() => {
      return throwError(() => new Error("test"));
    });

    fixture.detectChanges();

    // Then
    expect(component.isLoading).toBe(false);
    expect(component.errorMessage).toBeTruthy();
  });

  it("should return false when shouldDisplayErrorMessage is called and errorMessage is null", () => {
    // Given
    fixture = TestBed.createComponent(RaceWinnersComponent);
    component = fixture.componentInstance;
    component.errorMessage = null;

    // When
    const shouldDisplayErrorMessage: boolean =
      component.shouldDisplayErrorMessage();

    // Then
    expect(shouldDisplayErrorMessage).toBe(false);
  });

  it("should return true when shouldDisplayErrorMessage is called and errorMessage is not null", () => {
    // Given
    fixture = TestBed.createComponent(RaceWinnersComponent);
    component = fixture.componentInstance;
    component.errorMessage = "any message here";

    // When
    const shouldDisplayErrorMessage = component.shouldDisplayErrorMessage();

    // Then
    expect(shouldDisplayErrorMessage).toBe(true);
  });

  it("should concatenate Driver's given name and family name", () => {
    // Given
    fixture = TestBed.createComponent(RaceWinnersComponent);
    component = fixture.componentInstance;
    const driver: Driver =
      httpAllRacesWinnersOfAYearMock.MRData.RaceTable.Races[0].Results[0]
        .Driver;

    // When
    const result = component.getDriverFullName(driver);

    // Then
    expect(result).toEqual("Valtteri Bottas");
  });

  it("should return an empty string when Driver is missing", () => {
    // Given
    fixture = TestBed.createComponent(RaceWinnersComponent);
    component = fixture.componentInstance;

    // When
    const result = component.getDriverFullName(null);

    // Then
    expect(result).toEqual("");
  });
});
