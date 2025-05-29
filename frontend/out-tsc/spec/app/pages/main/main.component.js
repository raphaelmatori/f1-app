import { __decorate } from "tslib";
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SeasonListComponent } from '@app/shared/components/season-list/season-list.component';
let MainComponent = class MainComponent {
};
MainComponent = __decorate([
    Component({
        selector: 'app-main',
        standalone: true,
        imports: [CommonModule, SeasonListComponent],
        templateUrl: './main.component.html',
        styleUrls: ['./main.component.scss']
    })
], MainComponent);
export { MainComponent };
