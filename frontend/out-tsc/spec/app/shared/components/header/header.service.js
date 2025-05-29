import { __decorate } from "tslib";
import { Injectable } from "@angular/core";
let HeaderService = class HeaderService {
    title = "F1 World Champions";
    setTitle(title) {
        this.title = title;
    }
    getTitle() {
        return this.title;
    }
};
HeaderService = __decorate([
    Injectable({
        providedIn: "root",
    })
], HeaderService);
export { HeaderService };
