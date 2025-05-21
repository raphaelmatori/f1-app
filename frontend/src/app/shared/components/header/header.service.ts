import { Injectable } from "@angular/core";
import { Router } from "@angular/router";

@Injectable({
  providedIn: "root",
})
export class HeaderService {
  private title = "F1 World Champions List";
  private goBackTo: string[] = [];
  constructor(private router: Router) {}

  addGoBackTo(url: string): void {
    this.goBackTo.push(url);
  }

  goBack() {
    if (this.goBackTo.length) {
      this.router.navigate([this.goBackTo.pop()]);
    }
    this.setTitle("");
  }

  setTitle(title: string): void {
    this.title = title;
  }

  getTitle() {
    return this.title;
  }

  resetGoBackList() {
    this.goBackTo = [];
  }

  isGoBackAvailable(): boolean {
    return this.goBackTo.length > 0;
  }
}
