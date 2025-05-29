import { __decorate } from "tslib";
import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
let HeaderComponent = class HeaderComponent {
    headerService;
    constructor(headerService) {
        this.headerService = headerService;
    }
};
HeaderComponent = __decorate([
    Component({
        imports: [CommonModule],
        selector: "app-header",
        templateUrl: "./header.component.html",
        styleUrls: ["./header.component.scss"],
        standalone: true
    })
], HeaderComponent);
export { HeaderComponent };
