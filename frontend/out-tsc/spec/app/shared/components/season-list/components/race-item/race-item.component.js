import { __decorate } from "tslib";
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DateFormatPipe } from '../../pipes/date-format.pipe';
import { countryFlags } from '@app/shared/constants/country-flags';
let RaceItemComponent = class RaceItemComponent {
    race;
    isCurrentSeason = false;
    isChampionWinner = false;
    countryFlags = countryFlags;
};
__decorate([
    Input()
], RaceItemComponent.prototype, "race", void 0);
__decorate([
    Input()
], RaceItemComponent.prototype, "isCurrentSeason", void 0);
__decorate([
    Input()
], RaceItemComponent.prototype, "isChampionWinner", void 0);
RaceItemComponent = __decorate([
    Component({
        selector: 'app-race-item',
        standalone: true,
        imports: [CommonModule, DateFormatPipe],
        changeDetection: ChangeDetectionStrategy.OnPush,
        templateUrl: './race-item.component.html',
        styleUrls: ['./race-item.component.scss']
    })
], RaceItemComponent);
export { RaceItemComponent };
