import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <footer class="bg-gray-900 py-6 mt-10 fixed-footer">
      <div class="container mx-auto px-4 text-center text-gray-400">
        <p class="text-base">Data provided by https://api.jolpi.ca</p>
        <p class="mt-2 text-sm">© 2025 F1 Champions Explorer</p>
      </div>
    </footer>
  `,
  styles: [`
    .fixed-footer {
      min-height: 100px;
      width: 100%;
      position: relative;
      bottom: 0;
    }
    .text-base {
      font-size: 1rem;
      line-height: 1.5rem;
    }
  `]
})
export class FooterComponent {} 