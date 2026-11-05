import { Routes } from '@angular/router';
import { guardaAutenticacao } from '../d5';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./login/login.component').then(m => m.LoginComponent),
  },
  {
    path: '',
    redirectTo: 'inicio',
    pathMatch: 'full',
  },
  // Adicione aqui as rotas de cada agregado, mantendo canActivate: [guardaAutenticacao].
  {
    path: 'inicio',
    canActivate: [guardaAutenticacao],
    loadComponent: () => import('./inicio/inicio.component').then(m => m.InicioComponent),
  },
];
