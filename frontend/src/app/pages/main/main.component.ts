import { Component, OnInit, TemplateRef, ViewChild } from "@angular/core";
import { Router } from "@angular/router";
import { Item } from "@app-shared/components/list/models/item.interface";
import { F1Service } from "@app-shared/services/interfaces/f1.service.interface";
import { Season } from "@app/models/season.interface";
import { take } from "rxjs";
import { CommonModule } from "@angular/common";
import { ListComponent } from "@app/shared/components/list/list.component";
import { SpinnerComponent } from "@app/shared/components/spinner/spinner.component";

@Component({
  imports: [CommonModule, ListComponent, SpinnerComponent],
  selector: "app-main",
  templateUrl: "./main.component.html",
  styleUrls: ["./main.component.scss"],
  standalone: true
})
export class MainComponent implements OnInit {
  @ViewChild("listItemContent", { static: true })
  itemContent!: TemplateRef<any>;

  isLoading = false;
  items: Item[] = [];

  constructor(private router: Router, private f1Service: F1Service) {}

  ngOnInit(): void {
    this.fetchSeasonsData();
  }

  private addSeasonsToItemsList(seasons: Season[]): void {
    seasons.forEach((season) => {
      this.items.unshift({
        id: season.season.toString(),
        title: season.season.toString(),
      });
    });
  }

  fetchSeasonsData() {
    this.isLoading = true;
    this.f1Service
      .getSeasonsFromYearUntilNow(2005)
      .pipe(take(1))
      .subscribe((page) => {
        this.addSeasonsToItemsList(page.results);
        this.isLoading = false;
      });
  }

  selectedItemHandler(id: string) {
    this.router.navigate(["race-winners", id]);
  }
}
