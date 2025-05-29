import { __decorate } from "tslib";
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SpinnerComponent } from "@app/shared/components/spinner/spinner.component";
import { HttpClientModule } from "@angular/common/http";
import { countryFlags } from "@app/shared/constants/country-flags";
import { MESSAGES } from './constants/messages';
import { CurrentSeasonCardComponent } from "@app/shared/components/season-list/components/current-season-card/current-season-card.component";
import { PastSeasonCardComponent } from "@app/shared/components/season-list/components/past-season-card/past-season-card.component";
let SeasonListComponent = class SeasonListComponent {
    f1Service;
    seasons = [];
    champions = {};
    races = {};
    expandedSeasons = {};
    loading = true;
    error = '';
    currentYear = 0;
    constructor(f1Service) {
        this.f1Service = f1Service;
    }
    messages = MESSAGES;
    ngOnInit() {
        this.fetchSeasonsAndChampions();
    }
    fetchSeasonsAndChampions() {
        const auxChampionList = [];
        this.f1Service.getWorldChampions().subscribe({
            next: (championsByYear) => {
                if (championsByYear) {
                    const years = Array.from(championsByYear.keys());
                    years.sort((a, b) => b - a); // Descending
                    this.currentYear = years[0] + 1;
                    years.forEach(year => {
                        const driver = championsByYear.get(year);
                        if (driver) {
                            auxChampionList[year] = driver;
                        }
                    });
                    this.seasons = years;
                    this.champions = auxChampionList;
                }
                this.loading = false;
                this.error = '';
            },
            error: (err) => {
                this.loading = false;
                this.error = 'Error loading F1 World Champions';
                console.error('Error loading F1 Wrold Champions:', err);
            }
        });
    }
    toggleRaceList(year) {
        this.expandedSeasons[year] = !this.expandedSeasons[year];
        if (this.expandedSeasons[year] && !this.races[year]) {
            this.loadRaces(year);
        }
    }
    loadRaces(year) {
        this.f1Service.getAllRacesWinnersOfAYear(year).subscribe({
            next: (data) => {
                this.races[year] = data.map((race) => ({
                    season: race.season,
                    round: race.round,
                    raceName: race.raceName,
                    date: race.date,
                    time: race.time,
                    circuit: race.circuit,
                    results: race.results,
                    // Derive winner and constructor from first position
                    winner: race.results[0]?.driver,
                    constructor: race.results[0]?.constructor
                }));
            },
            error: (err) => {
                console.error(`Error loading races for ${year}:`, err);
                this.races[year] = [];
            }
        });
    }
    isChampionWinner(year, winnerId) {
        return this.champions[year]?.driverId === winnerId;
    }
    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    }
    countryFlags = countryFlags;
};
SeasonListComponent = __decorate([
    Component({
        selector: 'app-season-list',
        standalone: true,
        imports: [CommonModule, SpinnerComponent, CurrentSeasonCardComponent, PastSeasonCardComponent, HttpClientModule],
        templateUrl: './season-list.component.html',
        styleUrls: ['./season-list.component.scss']
    })
], SeasonListComponent);
export { SeasonListComponent };
