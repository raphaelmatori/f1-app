import {
  Component,
  EventEmitter,
  Input,
  Output,
  TemplateRef,
} from "@angular/core";
import { Item } from "./models/item.interface";
import { CommonModule } from '@angular/common';

@Component({
  imports: [CommonModule],
  selector: "app-list",
  templateUrl: "./list.component.html",
  styleUrls: ["./list.component.scss"],
  standalone: true
})
export class ListComponent {
  @Input() items: Item[] = [];
  @Input() listItem!: TemplateRef<any>;

  @Output() itemClickCallback: EventEmitter<string> = new EventEmitter();

  itemClickHandler(id: string): void {
    this.itemClickCallback.emit(id);
  }
}
