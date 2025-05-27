import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Driver } from '@app/shared/models/driver.interface';
import { Race } from '@app/shared/models/race.interface';
import { RaceListComponent } from '../race-list/race-list.component';
import { countryFlags } from '@app/shared/constants/country-flags';

@Component({
  selector: 'app-past-season-card',
  standalone: true,
  imports: [CommonModule, RaceListComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './past-season-card.component.html',
})
export class PastSeasonCardComponent {
  @Input() year!: number;
  @Input() champion?: Driver;
  @Input() races?: Race[];
  @Input() isExpanded = false;
  @Output() toggleExpanded = new EventEmitter<void>();

  protected readonly countryFlags = countryFlags;

  toggleRaces(): void {
    this.toggleExpanded.emit();
  }
} 