import { Routes } from '@angular/router';
import { guardaAutenticacao } from '../d5';
import { resolverArmarios } from './armarios/armarios.service';
import { rotasTreinos } from './treinos/treinos.rotas';
import { rotasAvaliacoes } from './avaliacoes/avaliacoes.rotas';
const tela = () => import('./armarios/armarios.component').then((m) => m.ArmariosComponent);
export const routes: Routes = [
  {
    path: 'cadastro',
    loadComponent: () => import('./cadastro/cadastro.component').then((m) => m.CadastroComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.component').then((m) => m.LoginComponent),
  },
  { path: '', redirectTo: 'inicio', pathMatch: 'full' },
  {
    path: 'inicio',
    canActivate: [guardaAutenticacao],
    loadComponent: () => import('./inicio/inicio.component').then((m) => m.InicioComponent),
  },
  {
    path: 'core/armario/pesquisa',
    canActivate: [guardaAutenticacao],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'lista' },
  },
  {
    path: 'core/armario/:id/visualizacao',
    canActivate: [guardaAutenticacao],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'detalhe' },
  },
  {
    path: 'core/reserva-armario/ativa',
    canActivate: [guardaAutenticacao],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'ativa' },
  },
  {
    path: 'core/reserva-armario/pesquisa',
    canActivate: [guardaAutenticacao],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'historico' },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/reserva-armario/:id/confirmacao',
    canActivate: [guardaAutenticacao],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'confirmacao' },
  },
  {
    path: 'core/reserva-armario/:id/visualizacao',
    canActivate: [guardaAutenticacao],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'reserva' },
  },
  ...rotasTreinos,
  ...rotasAvaliacoes,
  { path: '**', redirectTo: 'inicio' },
];
