import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { CurrentSeasonCardComponent } from './current-season-card.component';
import { MESSAGES } from '../../constants/messages';
import { Component, Input } from '@angular/core';
import { Race } from '@app/shared/models/race.interface';
import { By } from '@angular/platform-browser';
import { ChangeDetectorRef } from '@angular/core';

// Create stub components to avoid importing all dependencies
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

@Component({
  selector: 'app-spinner',
  template: '',
  standalone: true
})
class SpinnerStubComponent {
  @Input() loadingText = '';
}

describe('CurrentSeasonCardComponent', () => {
  let component: CurrentSeasonCardComponent;
  let fixture: ComponentFixture<CurrentSeasonCardComponent>;
  let changeDetectorRef: ChangeDetectorRef;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CurrentSeasonCardComponent,
        RaceListStubComponent,
        SpinnerStubComponent
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CurrentSeasonCardComponent);
    component = fixture.componentInstance;
    changeDetectorRef = fixture.debugElement.injector.get(ChangeDetectorRef);
    
    // Set up initial component state
    component.year = 2024;
    component.races = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display current year and season in progress text', () => {
    const h2Element = fixture.debugElement.query(By.css('h2'));
    const spanElement = fixture.debugElement.query(By.css('h2 span'));
    
    expect(h2Element.nativeElement.textContent).toContain('2024');
    expect(spanElement.nativeElement.textContent).toContain(MESSAGES.LABELS.SEASON_IN_PROGRESS);
  });

  it('should emit toggleExpanded event when button is clicked', fakeAsync(() => {
    const emitSpy = spyOn(component.toggleExpanded, 'emit');
    const button = fixture.debugElement.query(By.css('button'));
    button.nativeElement.click();
    tick();
    expect(emitSpy).toHaveBeenCalled();
  }));

  it('should show race list when isExpanded is true', () => {
    // Set up component state
    component.isExpanded = true;
    component.races = [];
    component.year = 2024;
    
    // Force change detection
    changeDetectorRef.detectChanges();
    fixture.detectChanges();

    // Try different ways to find the race list component
    const raceListBySelector = fixture.debugElement.query(By.css('app-race-list'));
    const raceListByDirective = fixture.debugElement.query(By.directive(RaceListStubComponent));
    const raceListByClass = fixture.debugElement.query(By.css('.race-list-stub'));
    
    // Check if any of the queries found the component
    const raceListFound = raceListBySelector || raceListByDirective || raceListByClass;
    expect(raceListFound).toBeTruthy();
  });

  it('should not show race list when isExpanded is false', () => {
    component.isExpanded = false;
    changeDetectorRef.detectChanges();
    fixture.detectChanges();
    
    const raceList = fixture.debugElement.query(By.directive(RaceListStubComponent));
    expect(raceList).toBeFalsy();
  });

  it('should rotate arrow icon when expanded', () => {
    component.isExpanded = true;
    changeDetectorRef.detectChanges();
    fixture.detectChanges();
    
    const arrow = fixture.debugElement.query(By.css('.transform'));
    expect(arrow.nativeElement.classList.contains('rotate-180')).toBe(true);
  });
}); 