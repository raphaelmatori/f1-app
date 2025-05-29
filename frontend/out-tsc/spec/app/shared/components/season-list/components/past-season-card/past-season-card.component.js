import { __decorate } from "tslib";
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RaceListComponent } from '../race-list/race-list.component';
import { countryFlags } from '@app/shared/constants/country-flags';
let PastSeasonCardComponent = class PastSeasonCardComponent {
    year;
    champion;
    races;
    isExpanded = false;
    toggleExpanded = new EventEmitter();
    countryFlags = countryFlags;
    toggleRaces() {
        this.toggleExpanded.emit();
    }
};
__decorate([
    Input()
], PastSeasonCardComponent.prototype, "year", void 0);
__decorate([
    Input()
], PastSeasonCardComponent.prototype, "champion", void 0);
__decorate([
    Input()
], PastSeasonCardComponent.prototype, "races", void 0);
__decorate([
    Input()
], PastSeasonCardComponent.prototype, "isExpanded", void 0);
__decorate([
    Output()
], PastSeasonCardComponent.prototype, "toggleExpanded", void 0);
PastSeasonCardComponent = __decorate([
    Component({
        selector: 'app-past-season-card',
        standalone: true,
        imports: [CommonModule, RaceListComponent],
        changeDetection: ChangeDetectionStrategy.OnPush,
        templateUrl: './past-season-card.component.html',
    })
], PastSeasonCardComponent);
export { PastSeasonCardComponent };
