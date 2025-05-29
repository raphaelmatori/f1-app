import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PastSeasonCardComponent } from './past-season-card.component';
import { Component, Input } from '@angular/core';
import { Race } from '@app/shared/models/race.interface';
import { By } from '@angular/platform-browser';
import { mockDriver, mockRace } from '@app/shared/mocks/race.mock';
import { countryFlags } from '@app/shared/constants/country-flags';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-race-list',
  template: '<div class="race-list-stub">Race List Stub</div>',
  standalone: true
})
class RaceListStubComponent {
  @Input() races?: Race[];
  @Input() year!: number;
  @Input() loading = false;
  @Input() isCurrentSeason = false;
}

describe('PastSeasonCardComponent', () => {
  let component: PastSeasonCardComponent;
  let fixture: ComponentFixture<PastSeasonCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, PastSeasonCardComponent, RaceListStubComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(PastSeasonCardComponent);
    component = fixture.componentInstance;
    component.year = 2023;
    component.champion = mockDriver;
    component.races = [mockRace];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit toggleExpanded event when button is clicked', fakeAsync(() => {
    const emitSpy = spyOn(component.toggleExpanded, 'emit');
    const button = fixture.debugElement.query(By.css('button'));
    
    button.nativeElement.click();
    tick();
    
    expect(emitSpy).toHaveBeenCalled();
  }));

  it('should show race list when isExpanded is true', () => {
    fixture.componentRef.setInput('isExpanded', true);
    fixture.detectChanges();

    const raceList = fixture.debugElement.query(By.css('app-race-list'));
    expect(raceList).toBeTruthy();
  });

  it('should not show race list when isExpanded is false', () => {
    fixture.componentRef.setInput('isExpanded', false);
    fixture.detectChanges();

    const raceList = fixture.debugElement.query(By.css('app-race-list'));
    expect(raceList).toBeFalsy();
  });

  it('should rotate arrow icon when expanded', () => {
    fixture.componentRef.setInput('isExpanded', true);
    fixture.detectChanges();

    const arrow = fixture.debugElement.query(By.css('.transform'));
    expect(arrow.nativeElement.classList.contains('rotate-180')).toBeTrue();
  });

  it('should expose countryFlags constant', () => {
    expect(component['countryFlags']).toBe(countryFlags);
  });
}); 