import {Component, Input} from "@angular/core";
import { CommonModule } from "@angular/common";

@Component({
  imports: [CommonModule],
  selector: "app-spinner",
  templateUrl: "./spinner.component.html",
  styleUrls: ["./spinner.component.scss"],
  standalone: true
})
export class SpinnerComponent {

  @Input()
  loadingText = "";
}
