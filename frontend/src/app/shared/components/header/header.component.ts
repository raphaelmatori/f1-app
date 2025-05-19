import { Component } from "@angular/core";
import { HeaderService } from "./header.service";
import { CommonModule } from "@angular/common";

@Component({
  imports: [CommonModule],
  selector: "app-header",
  templateUrl: "./header.component.html",
  styleUrls: ["./header.component.scss"],
  standalone: true
})
export class HeaderComponent {
  constructor(public headerService: HeaderService) {}
}
