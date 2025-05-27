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

  toggleRaceList(year: number) {
    this.expandedSeasons[year] = !this.expandedSeasons[year];
    if (this.expandedSeasons[year] && !this.races[year]) {
      this.loadRaces(year);
    }
  }

  loadRaces(year: number) {
    this.f1Service.getAllRacesWinnersOfAYear(year).subscribe({
      next: (data) => {
        this.races[year] = data.map((race: any) => ({
          season: race.season,
          round: race.round,
          url: race.url,
          raceName: race.raceName,
          date: race.date,
          circuitName: race.circuit.circuitName,
          winner: race.results[0]?.driver,
          constructor: race.results[0]?.constructor,
          Circuit: race.circuit,
          Results: race.results
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