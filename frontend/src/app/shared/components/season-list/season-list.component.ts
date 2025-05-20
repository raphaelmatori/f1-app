import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { F1Service } from '@app/shared/services/interfaces/f1.service.interface';
import { Driver } from '@app/shared/models/driver.interface';
import { Race } from '@app/shared/models/race.interface';
import { Paginate } from '@app/shared/models/paginate.interface';
import {SpinnerComponent} from "@app/shared/components/spinner/spinner.component";

@Component({
  selector: 'app-season-list',
  standalone: true,
  imports: [CommonModule, SpinnerComponent],
  templateUrl: './season-list.component.html',
  styleUrls: ['./season-list.component.scss']
})
export class SeasonListComponent implements OnInit {
  private f1Service = inject(F1Service);
  seasons: number[] = [];
  champions: { [year: number]: Driver | null } = {};
  races: { [year: number]: Race[] } = {};
  expandedSeasons: { [year: number]: boolean } = {};
  loading = true;

  ngOnInit(): void {
    this.fetchSeasonsAndChampions();
  }

  fetchSeasonsAndChampions() {
    this.f1Service.getSeasonsFromYearUntilNow(2005).subscribe(data => {
      this.seasons = data.results.map((season: any) => parseInt(season.season)).reverse();
      this.seasons.forEach(year => {
        this.f1Service.getWorldChampionByYear(year).subscribe(champion => {
          this.champions[year] = champion;
        });
      });
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
      this.races[year] = data.results.map((race: any) => ({
        season: race.season,
        round: race.round,
        url: race.url,
        raceName: race.raceName,
        date: race.date,
        circuitName: race.Circuit.circuitName,
        winner: race.Results[0]?.Driver,
        constructor: race.Results[0]?.Constructor,
        Circuit: race.Circuit,
        Results: race.Results
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
} 