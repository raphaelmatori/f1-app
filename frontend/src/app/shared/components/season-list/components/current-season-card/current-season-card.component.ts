import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Race } from '@app/shared/models/race.interface';
import { RaceListComponent } from '../race-list/race-list.component';
import { MESSAGES } from '../../constants/messages';

@Component({
  selector: 'app-current-season-card',
  standalone: true,
  imports: [CommonModule, RaceListComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './current-season-card.component.html',
  styleUrls: ['./current-season-card.component.scss']
})
export class CurrentSeasonCardComponent {
  @Input() year!: number;
  @Input() races?: Race[];
  @Input() isExpanded = false;
  @Output() toggleExpanded = new EventEmitter<void>();

  protected readonly messages = MESSAGES;

  toggleRaces(): void {
    this.toggleExpanded.emit();
  }
} 