import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { F1Service } from '@app/shared/services/interfaces/f1.service.interface';
import { Race } from '@app/shared/models/race.interface';
import { SpinnerComponent } from "@app/shared/components/spinner/spinner.component";
import { HttpClientModule } from "@angular/common/http";
import { countryFlags } from "@app/shared/constants/country-flags";
import { MESSAGES } from './constants/messages';
import {
  CurrentSeasonCardComponent
} from "@app/shared/components/season-list/components/current-season-card/current-season-card.component";
import {
  PastSeasonCardComponent
} from "@app/shared/components/season-list/components/past-season-card/past-season-card.component";
import { Driver } from '@app/models/driver.interface';

@Component({
  selector: 'app-season-list',
  standalone: true,
  imports: [CommonModule, SpinnerComponent, CurrentSeasonCardComponent, PastSeasonCardComponent, HttpClientModule],
  templateUrl: './season-list.component.html',
  styleUrls: ['./season-list.component.scss']
})
export class SeasonListComponent implements OnInit {
  seasons: number[] = [];
  champions: Record<number, Driver | undefined> = {};
  races: Record<number, Race[]> = {};
  expandedSeasons: Record<number, boolean> = {};
  loading = true;
  error = '';
  currentYear = 0;

  constructor(private f1Service: F1Service) {
  }

  protected readonly messages = MESSAGES;

  ngOnInit(): void {
    this.fetchSeasonsAndChampions();
  }

  fetchSeasonsAndChampions() {
    const auxChampionList: Record<number, Driver | undefined> | Driver[]= [];
    this.f1Service.getWorldChampions().subscribe({
      next: (championsByYear) => {
        if (championsByYear) {
          const years = Array.from(championsByYear.keys());
          years.sort((a, b) => b - a); // Descending
          this.currentYear = years[0] + 1;

          years.forEach(year => {
            const driver = championsByYear.get(year);
            if(driver) {
              auxChampionList[year] = driver;
            }

          });
          this.seasons = years;
          this.champions = auxChampionList;
        }

        this.error = '';
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading F1 World Champions';
        this.loading = false;
        console.error('Error loading F1 Wrold Champions:', err);
      }
    });
  }

  toggleRaceList(year: number) {
    this.expandedSeasons[year] = !this.expandedSeasons[year];
    if (this.expandedSeasons[year] && !this.races[year]) {
      this.loadRaces(year);
    }
  }

  loadRaces(year: number) {
    this.f1Service.getAllRacesWinnersOfAYear(year).subscribe({
      next: (data) => {
        this.races[year] = data.map((race: Race) => ({
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

  isChampionWinner(year: number, winnerId: string): boolean {
    return this.champions[year]?.driverId === winnerId;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  protected readonly countryFlags = countryFlags;
}