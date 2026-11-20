import { resolverGrupos } from './grupos/grupos.service';
import { resolverAvaliacoesProfessor } from './avaliacoes-professor/avaliacoes-professor.service';
import { Routes } from '@angular/router';
import { guardaAutenticacao } from '../d5';
import { guardaPerfis } from './guarda-perfis';
import { resolverArmarios } from './armarios/armarios.service';
import { rotasTreinos } from './treinos/treinos.rotas';
import { rotasAvaliacoes } from './avaliacoes/avaliacoes.rotas';
import { rotasPlanos } from './planos/planos.rotas';
import { rotasProfissionais } from './profissionais/profissionais.rotas';
import { rotasAlunos } from './alunos/alunos.rotas';
const tela = () => import('./armarios/armarios.component').then((m) => m.ArmariosComponent);
const somenteAluno = guardaPerfis('USUARIO');
export const routes: Routes = [
  {
    path: 'core/aluno/cadastro',
    loadComponent: () => import('./cadastro/cadastro.component').then((m) => m.CadastroComponent),
  },
  // endereço antigo do cadastro, mantido para os links já divulgados
  { path: 'cadastro', redirectTo: 'core/aluno/cadastro', pathMatch: 'full' },
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
    canActivate: [somenteAluno],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'lista' },
  },
  {
    path: 'core/armario/:id/visualizacao',
    canActivate: [somenteAluno],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'detalhe' },
  },
  {
    path: 'core/reserva-armario/acompanhamento',
    canActivate: [somenteAluno],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'ativa' },
  },
  {
    path: 'core/reserva-armario/pesquisa',
    canActivate: [somenteAluno],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'historico' },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/reserva-armario/:id/confirmacao',
    canActivate: [somenteAluno],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'confirmacao' },
  },
  {
    path: 'core/reserva-armario/:id/visualizacao',
    canActivate: [somenteAluno],
    loadComponent: tela,
    resolve: { tela: resolverArmarios },
    data: { modo: 'reserva' },
  },
  ...rotasTreinos,
  ...rotasAvaliacoes,
  ...rotasPlanos,
  ...rotasProfissionais,
  ...rotasAlunos,
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
    canActivate: [somenteAluno],
    loadComponent: () =>
      import('./avaliacoes-professor/avaliacoes-professor.component').then((m) => m.AvaliacoesProfessorComponent),
    resolve: { tela: resolverAvaliacoesProfessor },
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
    canActivate: [somenteAluno],
    loadComponent: () => import('./grupos/grupos.component').then((m) => m.GruposComponent),
    resolve: { tela: resolverGrupos },
    data: { modo },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange' as const,
  })),
  {
    path: '',
    loadChildren: () => import('./equipamentos/equipamentos.rotas').then((m) => m.rotasEquipamentos),
  },
  { path: '**', redirectTo: 'inicio' },
];
