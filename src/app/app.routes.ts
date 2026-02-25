import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'board' },
  {
    path: 'board',
    loadComponent: () =>
      import('./pages/board/board').then((m) => m.Board),
  },
  { path: '**', redirectTo: 'board' },
];
