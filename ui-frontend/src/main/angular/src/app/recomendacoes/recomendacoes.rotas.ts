import { Routes } from '@angular/router';
import { guardaPerfis } from '../guarda-perfis';
import { resolverComparacao, resolverDetalhe, resolverPerfil } from './recomendacoes.service';

const aluno = guardaPerfis('USUARIO');
const personal = guardaPerfis('PERSONAL');
const alunoOuPersonal = guardaPerfis('USUARIO', 'PERSONAL');

export const rotasRecomendacoes: Routes = [
  {
    path: 'core/perfil-aluno/meu',
    canActivate: [aluno],
    loadComponent: () => import('./meu-perfil.component').then((m) => m.MeuPerfilComponent),
    resolve: { perfil: resolverPerfil },
    data: { aba: 'dados' },
  },
  {
    path: 'core/perfil-aluno/preferencias',
    canActivate: [aluno],
    loadComponent: () => import('./meu-perfil.component').then((m) => m.MeuPerfilComponent),
    resolve: { perfil: resolverPerfil },
    data: { aba: 'preferencias' },
  },
  {
    path: 'core/recomendacao/minhas',
    canActivate: [aluno],
    loadComponent: () => import('./recomendacoes.component').then((m) => m.RecomendacoesComponent),
  },
  {
    path: 'core/recomendacao/historico',
    canActivate: [alunoOuPersonal],
    loadComponent: () => import('./historico-recomendacoes.component').then((m) => m.HistoricoRecomendacoesComponent),
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/recomendacao/aprovacoes',
    canActivate: [personal],
    loadComponent: () => import('./aprovacoes.component').then((m) => m.AprovacoesComponent),
  },
  {
    path: 'core/recomendacao/:id/analise',
    canActivate: [personal],
    loadComponent: () => import('./analise-recomendacao.component').then((m) => m.AnaliseRecomendacaoComponent),
    resolve: { detalhe: resolverDetalhe },
  },
  {
    path: 'core/recomendacao/:id/versoes',
    canActivate: [alunoOuPersonal],
    loadComponent: () => import('./versoes-plano.component').then((m) => m.VersoesPlanoComponent),
    resolve: { comparacao: resolverComparacao },
  },
  {
    path: 'core/recomendacao/:id/visualizacao',
    canActivate: [aluno],
    loadComponent: () => import('./recomendacao.component').then((m) => m.RecomendacaoComponent),
    resolve: { detalhe: resolverDetalhe },
    runGuardsAndResolvers: 'always',
  },
  {
    path: 'core/aula-coletiva/para-voce',
    canActivate: [aluno],
    loadComponent: () => import('./aulas-coletivas.component').then((m) => m.AulasColetivasComponent),
  },
];
