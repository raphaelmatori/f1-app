import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RaceListComponent } from './race-list.component';
import { mockDriver, mockRace } from '@app/shared/mocks/race.mock';
import { MESSAGES } from '../../constants/messages';
import { Component, Input } from '@angular/core';
import { Race } from '@app/shared/models/race.interface';
import { By } from '@angular/platform-browser';
import { Driver } from '@app/shared/models/driver.interface';
import { SpinnerComponent } from '@app/shared/components/spinner/spinner.component';
import {CommonModule} from "@angular/common";
import {DateFormatPipe} from "@app/shared/components/season-list/pipes/date-format.pipe";

@Component({
  selector: 'app-race-item',
  template: '<div class="race-item-stub">Race Item Stub</div>',
  standalone: true
})
class RaceItemStubComponent {
  @Input() race!: Race;
  @Input() isCurrentSeason = false;
  @Input() isChampionWinner = false;
}

describe('RaceListComponent', () => {
  let component: RaceListComponent;
  let fixture: ComponentFixture<RaceListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, DateFormatPipe
        ,  RaceListComponent, RaceItemStubComponent, SpinnerComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(RaceListComponent);
    component = fixture.componentInstance;
    component.year = 2024;
    component.races = [];
    component.loading = false;
    component.isCurrentSeason = false;
    component.champion = undefined;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show loading spinner when loading is true', () => {
    fixture.componentRef.setInput('loading', true);
    fixture.detectChanges();

    const spinner = fixture.debugElement.query(By.css('app-spinner'));

    expect(spinner).toBeTruthy();
    expect(spinner.componentInstance.loadingText).toEqual(MESSAGES.LOADING.RACE_DATA(2024));
  });

  it('should show no data message when races array is empty', () => {
    component.loading = false;
    component.races = [];
    fixture.detectChanges();
    const message = fixture.debugElement.query(By.css('.text-center'));
    expect(message.nativeElement.textContent).toContain(MESSAGES.ERROR.NO_RACE_DATA(2024));
  });

  it('should display race items when races are available',  () => {
    component.loading = false;

    fixture.componentRef.setInput('races', [mockRace]);
    fixture.detectChanges();

    const raceItems = fixture.debugElement.queryAll(By.css('app-race-item'));

    expect(raceItems.length).toEqual(1);
    
    const raceItem = raceItems[0].componentInstance as RaceItemStubComponent;
    expect(raceItem.race).toEqual(mockRace);
    expect(raceItem.isCurrentSeason).toBeFalse();
  });

  it('should identify champion winner correctly', () => {
    component.champion = mockDriver;
    const isChampion = component.isChampionWinner(mockRace);
    expect(isChampion).toBeTrue();
  });

  it('should return false for isChampionWinner when no champion is set', () => {
    component.champion = undefined;
    const isChampion = component.isChampionWinner(mockRace);
    expect(isChampion).toBeFalse();
  });

  it('should return false for isChampionWinner when race has no winner', () => {
    component.champion = mockDriver;
    const raceWithoutWinner = { ...mockRace, winner: undefined };
    const isChampion = component.isChampionWinner(raceWithoutWinner);
    expect(isChampion).toBeFalse();
  });

  it('should return false for isChampionWinner when winner IDs dont match', () => {
    const differentDriver: Driver = { ...mockDriver, driverId: 'different_id' };
    component.champion = differentDriver;
    const isChampion = component.isChampionWinner(mockRace);
    expect(isChampion).toBeFalse();
  });

  it('should track races by round', () => {
    const trackByResult = component.trackByRound(0, mockRace);
    expect(trackByResult).toEqual(mockRace.round);
  });

  it('should handle undefined races array', () => {
    component.races = undefined;
    fixture.detectChanges();
    const message = fixture.debugElement.query(By.css('.text-center'));
    expect(message.nativeElement.textContent).toContain(MESSAGES.ERROR.NO_RACE_DATA(2024));
  });
}); 