import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from "@app/shared/components/header/header.component";
import {ErgastService} from "@app/shared/services/ergast.service";
import {F1Service} from "@app/shared/services/interfaces/f1.service.interface";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent],
  providers: [
    { provide: F1Service, useClass: ErgastService }
  ],
  templateUrl: './app.component.html',
  standalone: true,
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'f1-app';
}
