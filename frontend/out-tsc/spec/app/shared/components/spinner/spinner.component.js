import { __decorate } from "tslib";
import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
let SpinnerComponent = class SpinnerComponent {
    loadingText = "";
};
__decorate([
    Input()
], SpinnerComponent.prototype, "loadingText", void 0);
SpinnerComponent = __decorate([
    Component({
        imports: [CommonModule],
        selector: "app-spinner",
        templateUrl: "./spinner.component.html",
        styleUrls: ["./spinner.component.scss"],
        standalone: true
    })
], SpinnerComponent);
export { SpinnerComponent };
