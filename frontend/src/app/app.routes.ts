import { Routes } from '@angular/router';

// a route is a mapping between a URL path and a component that should be displayed when the user navigates to that path.
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'board' },
  {
    path: 'board',
    loadComponent: () =>
      import('./pages/board/board').then((m) => m.Board),
  },
  { path: '**', redirectTo: 'board' },
];
