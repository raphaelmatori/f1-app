import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { SeasonListComponent } from './season-list.component';
import { F1Service } from "@app/shared/services/interfaces/f1.service.interface";
import { provideHttpClientTesting } from "@angular/common/http/testing";
import { mockDriver, mockRace } from '@app/shared/mocks/race.mock';
import { of, throwError } from 'rxjs';
import { MESSAGES } from './constants/messages';
describe('SeasonListComponent', () => {
    let component;
    let fixture;
    let f1Service;
    beforeEach(async () => {
        const spy = jasmine.createSpyObj('F1Service', ['getAllRacesWinnersOfAYear']);
        spy.getAllRacesWinnersOfAYear.and.returnValue(of([mockRace]));
        await TestBed.configureTestingModule({
            imports: [SeasonListComponent],
            providers: [
                provideHttpClientTesting(),
                { provide: F1Service, useValue: spy }
            ]
        }).compileComponents();
        fixture = TestBed.createComponent(SeasonListComponent);
        component = fixture.componentInstance;
        f1Service = TestBed.inject(F1Service);
    });
    it('should create', () => {
        expect(component).toBeTruthy();
    });
    it('should toggle race list and load races if not loaded', fakeAsync(() => {
        component.races = {};
        component.expandedSeasons = {};
        const year = 2024;
        component.toggleRaceList(year);
        tick();
        expect(component.expandedSeasons[year]).toBeTrue();
        expect(f1Service.getAllRacesWinnersOfAYear).toHaveBeenCalledWith(year);
        expect(component.races[year]).toEqual([{
                ...mockRace,
                winner: mockRace.results[0]?.driver,
                constructor: mockRace.results[0]?.constructor
            }]);
    }));
    it('should toggle race list without loading if races already loaded', fakeAsync(() => {
        const year = 2024;
        component.races = { [year]: [mockRace] };
        component.expandedSeasons = { [year]: false };
        component.toggleRaceList(year);
        tick();
        expect(component.expandedSeasons[year]).toBeTrue();
        expect(f1Service.getAllRacesWinnersOfAYear).not.toHaveBeenCalled();
    }));
    it('should handle error when loading races', fakeAsync(() => {
        const year = 2024;
        const error = new Error('Test error');
        f1Service.getAllRacesWinnersOfAYear.and.returnValue(throwError(() => error));
        spyOn(console, 'error');
        component.loadRaces(year);
        tick();
        expect(console.error).toHaveBeenCalledWith(`Error loading races for ${year}:`, error);
        expect(component.races[year]).toEqual([]);
    }));
    it('should correctly identify champion winner', () => {
        const year = 2024;
        component.champions = { [year]: mockDriver };
        expect(component.isChampionWinner(year, mockDriver.driverId)).toBeTrue();
        expect(component.isChampionWinner(year, 'different_id')).toBeFalse();
    });
    it('should return false for isChampionWinner when no champion exists', () => {
        const year = 2024;
        component.champions = {};
        expect(component.isChampionWinner(year, mockDriver.driverId)).toBeFalse();
    });
    it('should initialize with correct default values', () => {
        expect(component.seasons).toEqual([]);
        expect(component.champions).toEqual({});
        expect(component.races).toEqual({});
        expect(component.expandedSeasons).toEqual({});
        expect(component.loading).toBeTrue();
        expect(component.error).toBe('');
        expect(component.currentYear).toBe(0);
    });
    it('should expose messages constant', () => {
        expect(component['messages']).toBe(MESSAGES);
    });
});
