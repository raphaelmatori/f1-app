import { HttpClientModule } from "@angular/common/http";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { RouterTestingModule } from "@angular/router/testing";
import { httpSeasonsMock } from "@app-mocks/http-seasons.mock";
import { F1Service } from "@app-shared/services/interfaces/f1.service.interface";
import { of } from "rxjs";

import { emptyPageMock } from "@app-mocks/http-empty-page.mock";
import { MainComponent } from "./main.component";

describe("MainComponent", () => {
  let component: MainComponent;
  let fixture: ComponentFixture<MainComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      declarations: [MainComponent],
      imports: [RouterTestingModule, HttpClientModule],
      providers: [
        {
          provide: F1Service,
          useValue: {
            getSeasonsFromYearUntilNow: () =>
              of({
                ...emptyPageMock,
                results: httpSeasonsMock.MRData.SeasonTable.Seasons,
              }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should select item on item click", () => {
    // Given
    spyOn(component["router"], "navigate").and.callFake(() =>
      Promise.resolve(true)
    );

    // When
    component.selectedItemHandler("123");

    // Then
    expect(component["router"].navigate).toHaveBeenCalledWith([
      "race-winners",
      "123",
    ]);
  });
});
