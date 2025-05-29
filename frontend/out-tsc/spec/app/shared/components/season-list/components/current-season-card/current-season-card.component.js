import { __decorate } from "tslib";
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RaceListComponent } from '../race-list/race-list.component';
import { MESSAGES } from '../../constants/messages';
let CurrentSeasonCardComponent = class CurrentSeasonCardComponent {
    year;
    races;
    isExpanded = false;
    toggleExpanded = new EventEmitter();
    messages = MESSAGES;
    toggleRaces() {
        this.toggleExpanded.emit();
    }
};
__decorate([
    Input()
], CurrentSeasonCardComponent.prototype, "year", void 0);
__decorate([
    Input()
], CurrentSeasonCardComponent.prototype, "races", void 0);
__decorate([
    Input()
], CurrentSeasonCardComponent.prototype, "isExpanded", void 0);
__decorate([
    Output()
], CurrentSeasonCardComponent.prototype, "toggleExpanded", void 0);
CurrentSeasonCardComponent = __decorate([
    Component({
        selector: 'app-current-season-card',
        standalone: true,
        imports: [CommonModule, RaceListComponent],
        changeDetection: ChangeDetectionStrategy.OnPush,
        templateUrl: './current-season-card.component.html',
        styleUrls: ['./current-season-card.component.scss']
    })
], CurrentSeasonCardComponent);
export { CurrentSeasonCardComponent };
