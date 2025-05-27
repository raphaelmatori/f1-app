import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Race } from '@app/shared/models/race.interface';
import { SpinnerComponent } from '@app/shared/components/spinner/spinner.component';
import { RaceItemComponent } from '../race-item/race-item.component';
import { MESSAGES } from '../../constants/messages';
import { Driver } from '@app/shared/models/driver.interface';

@Component({
  selector: 'app-race-list',
  standalone: true,
  imports: [CommonModule, SpinnerComponent, RaceItemComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './race-list.component.html',
})
export class RaceListComponent {
  @Input() races?: Race[];
  @Input() year!: number;
  @Input() loading = false;
  @Input() isCurrentSeason = false;
  @Input() champion?: Driver;

  protected readonly messages = MESSAGES;

  trackByRound(_: number, race: Race): number {
    return race.round;
  }

  isChampionWinner(race: Race): boolean {
    if (!this.champion || !race.winner) return false;
    return this.champion.driverId === race.winner.driverId;
  }
} 