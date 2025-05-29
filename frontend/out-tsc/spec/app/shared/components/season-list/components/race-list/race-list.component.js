import { __decorate } from "tslib";
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SpinnerComponent } from '@app/shared/components/spinner/spinner.component';
import { RaceItemComponent } from '../race-item/race-item.component';
import { MESSAGES } from '../../constants/messages';
let RaceListComponent = class RaceListComponent {
    races;
    year;
    loading = false;
    isCurrentSeason = false;
    champion;
    messages = MESSAGES;
    trackByRound(_, race) {
        return race.round;
    }
    isChampionWinner(race) {
        if (!this.champion || !race.winner)
            return false;
        return this.champion.driverId === race.winner.driverId;
    }
};
__decorate([
    Input()
], RaceListComponent.prototype, "races", void 0);
__decorate([
    Input()
], RaceListComponent.prototype, "year", void 0);
__decorate([
    Input()
], RaceListComponent.prototype, "loading", void 0);
__decorate([
    Input()
], RaceListComponent.prototype, "isCurrentSeason", void 0);
__decorate([
    Input()
], RaceListComponent.prototype, "champion", void 0);
RaceListComponent = __decorate([
    Component({
        selector: 'app-race-list',
        standalone: true,
        imports: [CommonModule, SpinnerComponent, RaceItemComponent],
        changeDetection: ChangeDetectionStrategy.OnPush,
        templateUrl: './race-list.component.html',
    })
], RaceListComponent);
export { RaceListComponent };
