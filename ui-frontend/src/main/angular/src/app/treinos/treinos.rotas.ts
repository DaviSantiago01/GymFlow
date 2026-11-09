import { Routes } from '@angular/router';
import { guardaAutenticacao } from '../../d5';
import {
  resolverAlteracaoPlano,
  resolverAlunos,
  resolverEvolucao,
  resolverExecucoes,
  resolverInclusaoExecucao,
  resolverInclusaoPlano,
  resolverPlanos,
  resolverVisualizacaoPlano,
} from './treinos.service';

const formularioPlano = () =>
  import('./plano-formulario.component').then((m) => m.PlanoFormularioComponent);

export const rotasTreinos: Routes = [
  {
    path: 'core/aluno/pesquisa',
    canActivate: [guardaAutenticacao],
    loadComponent: () => import('./alunos.component').then((m) => m.AlunosComponent),
    resolve: { tela: resolverAlunos },
  },
  {
    path: 'core/plano-de-treino/pesquisa',
    canActivate: [guardaAutenticacao],
    loadComponent: () => import('./planos.component').then((m) => m.PlanosComponent),
    resolve: { tela: resolverPlanos },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/plano-de-treino/inclusao',
    canActivate: [guardaAutenticacao],
    loadComponent: formularioPlano,
    resolve: { tela: resolverInclusaoPlano },
  },
  {
    path: 'core/plano-de-treino/:id/alteracao',
    canActivate: [guardaAutenticacao],
    loadComponent: formularioPlano,
    resolve: { tela: resolverAlteracaoPlano },
  },
  {
    path: 'core/plano-de-treino/:id/visualizacao',
    canActivate: [guardaAutenticacao],
    loadComponent: () =>
      import('./plano-visualizacao.component').then((m) => m.PlanoVisualizacaoComponent),
    resolve: { tela: resolverVisualizacaoPlano },
  },
  {
    path: 'core/execucao-de-exercicio/inclusao',
    canActivate: [guardaAutenticacao],
    loadComponent: () =>
      import('./execucao-formulario.component').then((m) => m.ExecucaoFormularioComponent),
    resolve: { tela: resolverInclusaoExecucao },
  },
  {
    path: 'core/execucao-de-exercicio/pesquisa',
    canActivate: [guardaAutenticacao],
    loadComponent: () => import('./execucoes.component').then((m) => m.ExecucoesComponent),
    resolve: { tela: resolverExecucoes },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/execucao-de-exercicio/evolucao',
    canActivate: [guardaAutenticacao],
    loadComponent: () =>
      import('./evolucao-treino.component').then((m) => m.EvolucaoTreinoComponent),
    resolve: { tela: resolverEvolucao },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
];
