import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SeasonListComponent } from '@app/shared/components/season-list/season-list.component';
import { F1Service } from '@app/shared/services/interfaces/f1.service.interface';
import { of } from 'rxjs';
import { Driver } from '@app/shared/models/driver.interface';
import { SpinnerComponent } from '@app/shared/components/spinner/spinner.component';

describe('SeasonListComponent', () => {
  let component: SeasonListComponent;
  let fixture: ComponentFixture<SeasonListComponent>;
  let f1Service: jasmine.SpyObj<F1Service>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('F1Service', [
      'getSeasonsFromYearUntilNow',
      'getWorldChampions',
      'getAllRacesWinnersOfAYear'
    ]);

    // Set up default return values for the spy methods
    spy.getSeasonsFromYearUntilNow.and.returnValue(of({
      limit: 30,
      offset: 0,
      total: 2,
      results: [
        { season: '2023' },
        { season: '2022' }
      ]
    }));

    const mockChampions = new Map<number, Driver>();
    mockChampions.set(2023, {
      driverId: 'test-driver',
      permanentNumber: '1',
      code: 'TEST',
      url: '',
      givenName: 'Test',
      familyName: 'Driver',
      dateOfBirth: '',
      nationality: ''
    });
    spy.getWorldChampions.and.returnValue(of(mockChampions));

    spy.getAllRacesWinnersOfAYear.and.returnValue(of([
      {
        season: '2023',
        round: 1,
        url: 'http://example.com/race1',
        raceName: 'Test Race 1',
        date: '2023-01-01',
        circuit: {
          circuitName: 'Test Circuit 1'
        },
        results: [{
          driver: {
            driverId: 'test-driver-1',
            givenName: 'Test',
            familyName: 'Driver 1'
          },
          constructor: {
            name: 'Test Constructor'
          }
        }]
      }
    ]));

    await TestBed.configureTestingModule({
      imports: [
        SeasonListComponent,
        SpinnerComponent
      ],
      providers: [
        { provide: F1Service, useValue: spy }
      ]
    })
    .compileComponents();

    f1Service = TestBed.inject(F1Service) as jasmine.SpyObj<F1Service>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SeasonListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle race list and load races', () => {
    const year = 2023;
    component.toggleRaceList(year);

    expect(component.expandedSeasons[year]).toBeTrue();
    expect(f1Service.getAllRacesWinnersOfAYear).toHaveBeenCalledWith(year);
  });

  it('should check if driver is champion winner', () => {
    const year = 2023;
    const winnerId = 'test-driver';
    const mockChampion: Driver = {
      driverId: 'test-driver',
      permanentNumber: '1',
      code: 'TEST',
      url: '',
      givenName: 'Test',
      familyName: 'Driver',
      dateOfBirth: '',
      nationality: ''
    };

    component.champions[year] = mockChampion;
    const result = component.isChampionWinner(year, winnerId);

    expect(result).toBeTrue();
  });

  it('should format date correctly', () => {
    const dateString = '2023-01-01';
    const formattedDate = component.formatDate(dateString);
    expect(formattedDate).toBe('Jan 1');
  });
}); 