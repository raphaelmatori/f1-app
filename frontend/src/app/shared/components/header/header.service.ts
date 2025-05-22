import { Injectable } from "@angular/core";

@Injectable({
  providedIn: "root",
})
export class HeaderService {
  private title = "F1 World Champions";

  setTitle(title: string): void {
    this.title = title;
  }

  getTitle() {
    return this.title;
  }
}
