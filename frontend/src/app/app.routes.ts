import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { MainComponent } from "./pages/main/main.component";
import { RaceWinnersComponent } from "./pages/race-winners/race-winners.component";

export const routes: Routes = [
    { path: "", component: MainComponent },
    { path: "race-winners/:year", component: RaceWinnersComponent },
    { path: "**", redirectTo: "" },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule],
})
export class AppRoutingModule {}