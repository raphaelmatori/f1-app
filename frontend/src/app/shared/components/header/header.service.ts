import { Injectable } from "@angular/core";
import { Router } from "@angular/router";

@Injectable({
  providedIn: "root",
})
export class HeaderService {
  private title = "F1 World Champions";
  constructor() {}

  setTitle(title: string): void {
    this.title = title;
  }

  getTitle() {
    return this.title;
  }
}
