import { resolverGrupos } from './grupos/grupos.service';
import { resolverAvaliacoes } from './avaliacoes/avaliacoes.service';
import { Routes } from '@angular/router';
import { guardaAutenticacao } from '../d5';
import { resolverArmarios } from './armarios/armarios.service';
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
  ...[
    { path: 'core/treino/pesquisa', modo: 'treinos' },
    { path: 'core/treino/:id/avaliacao', modo: 'nova' },
    { path: 'core/avaliacao-professor/pesquisa', modo: 'minhas' },
    { path: 'core/professor/pesquisa', modo: 'professores' },
    { path: 'core/professor/:id/visualizacao', modo: 'perfil' },

    { path: 'core/avaliacao-professor/:id/visualizacao', modo: 'minha' },
    { path: 'core/avaliacao-professor/:id/edicao', modo: 'editar' },
  ].map(({ path, modo }) => ({
    path,
    canActivate: [guardaAutenticacao],
    loadComponent: () =>
      import('./avaliacoes/avaliacoes.component').then((m) => m.AvaliacoesComponent),
    resolve: { tela: resolverAvaliacoes },
    data: { modo },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange' as const,
  })),
  ...[
    { path: 'core/grupo-treino/pesquisa', modo: 'lista' },
    { path: 'core/grupo-treino/criacao', modo: 'criar' },
    { path: 'core/grupo-treino/convites-treino', modo: 'convites' },
    { path: 'core/grupo-treino/historico', modo: 'historico' },
    { path: 'core/grupo-treino/:id/agenda', modo: 'propor' },
    { path: 'core/grupo-treino/:id/visualizacao', modo: 'grupo' },
    { path: 'core/grupo-treino/:id/treinos/:treino/visualizacao', modo: 'treino' },
    { path: 'core/grupo-treino/:id/treinos/:treino/convite', modo: 'treino' },
  ].map(({ path, modo }) => ({
    path,
    canActivate: [guardaAutenticacao],
    loadComponent: () => import('./grupos/grupos.component').then((m) => m.GruposComponent),
    resolve: { tela: resolverGrupos },
    data: { modo },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange' as const,
  })),
  { path: '**', redirectTo: 'inicio' },
];
