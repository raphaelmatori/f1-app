import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { F1Service } from '@app/shared/services/interfaces/f1.service.interface';
import { Driver } from '@app/shared/models/driver.interface';
import { Race } from '@app/shared/models/race.interface';
import { SpinnerComponent } from "@app/shared/components/spinner/spinner.component";
import { HttpClientModule } from "@angular/common/http";
import {countryFlags} from "@app/shared/constants/country-flags";

@Component({
  selector: 'app-season-list',
  standalone: true,
  imports: [CommonModule, SpinnerComponent, HttpClientModule],
  templateUrl: './season-list.component.html',
  styleUrls: ['./season-list.component.scss']
})
export class SeasonListComponent implements OnInit {
  seasons: number[] = [];
  champions: { [year: number]: Driver | undefined } = {};
  races: { [year: number]: Race[] } = {};
  expandedSeasons: { [year: number]: boolean } = {};
  loading = true;

  constructor(private f1Service: F1Service) {}

  ngOnInit(): void {
    this.fetchSeasonsAndChampions();
  }

  fetchSeasonsAndChampions() {
    const currentYear = new Date().getFullYear();
    this.seasons = Array.from(
      { length: currentYear - 2005 + 1 },
      (_, i) => currentYear - i
    );

    this.f1Service.getWorldChampions().subscribe(championsByYear => {
      if (championsByYear) {
        this.seasons.forEach(year => {
          this.champions[year] = championsByYear.get(year);
        });
      }
      this.loading = false;
    });
  }

  toggleRaceList(year: number) {
    this.expandedSeasons[year] = !this.expandedSeasons[year];
    if (this.expandedSeasons[year] && !this.races[year]) {
      this.loadRaces(year);
    }
  }

  loadRaces(year: number) {
    this.f1Service.getAllRacesWinnersOfAYear(year).subscribe(data => {
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