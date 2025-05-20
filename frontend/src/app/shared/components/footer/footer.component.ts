import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <footer class="bg-gray-900 py-6 mt-10">
      <div class="container mx-auto px-4 text-center text-gray-400">
        <p>Data provided by the Ergast Developer API</p>
        <p class="mt-2 text-sm">© 2024 F1 Champions Explorer</p>
      </div>
    </footer>
  `,
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent {} 