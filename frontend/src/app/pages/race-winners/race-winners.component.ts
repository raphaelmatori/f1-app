import {
  Component,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewChild,
} from "@angular/core";
import { ActivatedRoute, ParamMap, Router } from "@angular/router";
import { Driver } from "@app-models/driver.interface";
import { Paginate } from "@app-models/paginate.interface";
import { Race } from "@app-models/race.interface";
import { HeaderService } from "@app-shared/components/header/header.service";
import { Item } from "@app-shared/components/list/models/item.interface";
import { ERROR_MESSAGES } from "@app-shared/constants/error-messages";
import { F1Service } from "@app-shared/services/interfaces/f1.service.interface";
import {
  catchError,
  Observable,
  Subject,
  switchMap,
  take,
  takeUntil,
  tap,
  throwError,
} from "rxjs";
import { CommonModule } from "@angular/common";
import { ListComponent } from "@app/shared/components/list/list.component";
import { SpinnerComponent } from "@app/shared/components/spinner/spinner.component";

@Component({
  imports: [CommonModule, ListComponent, SpinnerComponent],
  selector: "app-race-winners",
  templateUrl: "./race-winners.component.html",
  styleUrls: ["./race-winners.component.scss"],
  standalone: true
})
export class RaceWinnersComponent implements OnInit, OnDestroy {
  @ViewChild("listItemContent", { static: true })
  itemContent!: TemplateRef<any>;

  isLoading = false;
  items: Item[] = [];
  yearBase: number = -1;
  unsubscribe$ = new Subject<void>();
  errorMessage: string | null = null;
  worldChampion: Driver | null = null;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private f1Service: F1Service,
    private headerService: HeaderService
  ) {}

  ngOnInit(): void {
    this.headerService.addGoBackTo("/");
    this.initData();
  }

  private initData() {
    this.isLoading = true;
    this.activatedRoute.paramMap
      .pipe(
        takeUntil(this.unsubscribe$),
        switchMap((params: ParamMap) => {
          const receivedYear = params.get("year");
          this.yearBase = parseInt(receivedYear as string);

          if (isNaN(this.yearBase)) {
            this.headerService.resetGoBackList();
            this.router.navigate(["/"]);
            this.isLoading = false;
            return throwError(() => new Error(ERROR_MESSAGES.YEAR_IS_NaN));
          }

          this.headerService.setTitle("Year: " + receivedYear);
          return this.fetchWorldChampionOfAYear();
        }),
        switchMap(() => this.fetchRacesData())
      )
      .subscribe();
  }

  private fetchWorldChampionOfAYear(): Observable<Driver | null> {
    return this.f1Service.getWorldChampionByYear(this.yearBase).pipe(
      take(1),
      tap((driver) => {
        this.worldChampion = driver;
      }),
      catchError((err) => {
        this.isLoading = false;
        this.errorMessage = ERROR_MESSAGES.API_FETCH_FAIL;
        return throwError(() => new Error(this.errorMessage!));
      })
    );
  }

  private addRaceToItemsList(races: Race[]): void {
    races.forEach((race) => {
      this.items.push(this.mapRaceToListItem(race));
    });
  }

  private mapRaceToListItem(race: Race) {
    const isWorldChampion =
      race.Results[0]?.Driver.driverId === this.worldChampion?.driverId;

    return {
      id: race.round.toString(),
      selected: isWorldChampion,
      title: race.raceName.toString(),
      additionalInfo: {
        winnerFullName: this.getDriverFullName(race.Results[0].Driver),
      },
    };
  }
  fetchRacesData(): Observable<Paginate> {
    return this.f1Service.getAllRacesWinnersOfAYear(this.yearBase).pipe(
      take(1),
      tap((page) => {
        this.addRaceToItemsList(page.results);
        this.isLoading = false;
      })
    );
  }

  getDriverFullName(driver: Driver | null): string {
    if (!driver) return "";
    return `${driver.givenName} ${driver.familyName}`;
  }

  selectedItemHandler(id: string) {
    console.log(id);
  }

  shouldDisplayErrorMessage(): boolean {
    return this.errorMessage !== null;
  }

  shouldDisplayResults(): boolean {
    return !this.isLoading && !this.errorMessage;
  }

  ngOnDestroy() {
    this.unsubscribe$.next();
  }
}
