import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: "",
        loadComponent: () => import('./pages/main/main.component').then(m => m.MainComponent),
        data: { prerender: true }
    },
    { path: "**", redirectTo: "" }
];