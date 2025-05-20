import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: "", renderMode: RenderMode.Prerender },
  { path: "race-winners/:year", renderMode: RenderMode.Prerender,
    async getPrerenderParams() {
      const years = [];
      for (let y = 2005; y <= new Date().getFullYear(); y++) {
        years.push(`${y}`);
      }
      return years.map(year =>  ({ year }) );
    },
  },
  { path: "**", renderMode: RenderMode.Prerender },
];
