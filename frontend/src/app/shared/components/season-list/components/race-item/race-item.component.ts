import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Race } from '@app/shared/models/race.interface';
import { DateFormatPipe } from '../../pipes/date-format.pipe';
import { countryFlags } from '@app/shared/constants/country-flags';

@Component({
  selector: 'app-race-item',
  standalone: true,
  imports: [CommonModule, DateFormatPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './race-item.component.html',
  styleUrls: ['./race-item.component.scss']
})
export class RaceItemComponent {
  @Input() race!: Race;
  @Input() isCurrentSeason = false;
  @Input() isChampionWinner = false;

  protected readonly countryFlags = countryFlags;
} 