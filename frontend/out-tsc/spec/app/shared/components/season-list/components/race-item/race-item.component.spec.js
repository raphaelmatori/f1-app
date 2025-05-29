/// <reference types="jasmine" />
import { TestBed } from '@angular/core/testing';
import { RaceItemComponent } from './race-item.component';
import { mockRace } from '@app/shared/mocks/race.mock';
import { DateFormatPipe } from '../../pipes/date-format.pipe';
import { By } from '@angular/platform-browser';
import { countryFlags } from '@app/shared/constants/country-flags';
import { ChangeDetectorRef } from '@angular/core';
describe('RaceItemComponent', () => {
    let component;
    let fixture;
    let changeDetectorRef;
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RaceItemComponent, DateFormatPipe]
        }).compileComponents();
        fixture = TestBed.createComponent(RaceItemComponent);
        component = fixture.componentInstance;
        changeDetectorRef = fixture.debugElement.injector.get(ChangeDetectorRef);
        component.race = mockRace;
        component.isCurrentSeason = false;
        component.isChampionWinner = false;
        fixture.detectChanges();
    });
    it('should create', () => {
        expect(component).toBeTruthy();
    });
    it('should display race round', () => {
        const roundElement = fixture.debugElement.query(By.css('.col-span-1'));
        expect(roundElement.nativeElement.textContent.trim()).toContain(mockRace.round.toString());
    });
    it('should display race name and circuit', () => {
        const raceInfoElement = fixture.debugElement.query(By.css('.col-span-5'));
        expect(raceInfoElement.nativeElement.textContent).toContain(mockRace.raceName);
        expect(raceInfoElement.nativeElement.textContent).toContain(mockRace.circuit.circuitName);
    });
    it('should display winner information', () => {
        const winnerElement = fixture.debugElement.query(By.css('.col-span-4'));
        expect(winnerElement.nativeElement.textContent).toContain(mockRace.winner?.givenName);
        expect(winnerElement.nativeElement.textContent).toContain(mockRace.winner?.familyName);
        expect(winnerElement.nativeElement.textContent).toContain(mockRace.winner?.nationality);
    });
    it('should display formatted date', () => {
        const dateElement = fixture.debugElement.query(By.css('.col-span-2'));
        const formattedDate = new DateFormatPipe().transform(mockRace.date);
        expect(dateElement.nativeElement.textContent.trim()).toContain(formattedDate);
    });
    it('should apply champion-race class when isChampionWinner is true', () => {
        component.isChampionWinner = true;
        changeDetectorRef.detectChanges();
        fixture.detectChanges();
        const container = fixture.debugElement.query(By.css('.grid'));
        expect(container.nativeElement.classList.contains('champion-race')).toBeTrue();
    });
    it('should not apply champion-race class when isChampionWinner is false', () => {
        component.isChampionWinner = false;
        changeDetectorRef.detectChanges();
        fixture.detectChanges();
        const container = fixture.debugElement.query(By.css('.grid'));
        expect(container.nativeElement.classList.contains('champion-race')).toBeFalse();
    });
    it('should display country flag for winner nationality', () => {
        const winnerElement = fixture.debugElement.query(By.css('.col-span-4'));
        const nationality = mockRace.winner?.nationality || '';
        expect(winnerElement.nativeElement.textContent).toContain(countryFlags[nationality]);
    });
    it('should handle missing winner information gracefully', () => {
        fixture.componentRef.setInput('race', { ...mockRace, winner: undefined });
        changeDetectorRef.detectChanges();
        fixture.detectChanges();
        const winnerElement = fixture.debugElement.query(By.css('.col-span-4'));
        expect(winnerElement.nativeElement).toBeTruthy();
    });
    it('should handle missing date gracefully', () => {
        fixture.componentRef.setInput('race', { ...mockRace, date: undefined });
        changeDetectorRef.detectChanges();
        fixture.detectChanges();
        const dateElement = fixture.debugElement.query(By.css('.col-span-2'));
        expect(dateElement.nativeElement).toBeTruthy();
    });
    it('should handle missing round gracefully', () => {
        fixture.componentRef.setInput('race', { ...mockRace, round: undefined });
        changeDetectorRef.detectChanges();
        fixture.detectChanges();
        const roundElement = fixture.debugElement.query(By.css('.col-span-1'));
        expect(roundElement.nativeElement).toBeTruthy();
    });
    it('should handle missing race name gracefully', () => {
        fixture.componentRef.setInput('race', { ...mockRace, raceName: undefined });
        changeDetectorRef.detectChanges();
        fixture.detectChanges();
        const nameElement = fixture.debugElement.query(By.css('.col-span-5 .font-semibold'));
        expect(nameElement.nativeElement).toBeTruthy();
    });
    it('should handle unknown nationality gracefully', () => {
        fixture.componentRef.setInput('race', {
            ...mockRace,
            winner: { ...mockRace.winner, nationality: 'Unknown' }
        });
        changeDetectorRef.detectChanges();
        fixture.detectChanges();
        const winnerElement = fixture.debugElement.query(By.css('.col-span-4'));
        expect(winnerElement.nativeElement).toBeTruthy();
    });
});
