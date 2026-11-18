import { Routes } from '@angular/router';
import { guardaPerfis } from '../guarda-perfis';
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
    canActivate: [guardaPerfis('PERSONAL')],
    loadComponent: () => import('./alunos.component').then((m) => m.AlunosComponent),
    resolve: { tela: resolverAlunos },
  },
  {
    path: 'core/plano-de-treino/pesquisa',
    canActivate: [guardaPerfis('PERSONAL', 'USUARIO')],
    loadComponent: () => import('./planos.component').then((m) => m.PlanosComponent),
    resolve: { tela: resolverPlanos },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/plano-de-treino/inclusao',
    canActivate: [guardaPerfis('PERSONAL')],
    loadComponent: formularioPlano,
    resolve: { tela: resolverInclusaoPlano },
  },
  {
    path: 'core/plano-de-treino/:id/alteracao',
    canActivate: [guardaPerfis('PERSONAL')],
    loadComponent: formularioPlano,
    resolve: { tela: resolverAlteracaoPlano },
  },
  {
    path: 'core/plano-de-treino/:id/visualizacao',
    canActivate: [guardaPerfis('PERSONAL', 'USUARIO')],
    loadComponent: () =>
      import('./plano-visualizacao.component').then((m) => m.PlanoVisualizacaoComponent),
    resolve: { tela: resolverVisualizacaoPlano },
  },
  {
    path: 'core/execucao-de-exercicio/inclusao',
    canActivate: [guardaPerfis('USUARIO')],
    loadComponent: () =>
      import('./execucao-formulario.component').then((m) => m.ExecucaoFormularioComponent),
    resolve: { tela: resolverInclusaoExecucao },
  },
  {
    path: 'core/execucao-de-exercicio/pesquisa',
    canActivate: [guardaPerfis('PERSONAL', 'USUARIO')],
    loadComponent: () => import('./execucoes.component').then((m) => m.ExecucoesComponent),
    resolve: { tela: resolverExecucoes },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/execucao-de-exercicio/evolucao',
    canActivate: [guardaPerfis('PERSONAL', 'USUARIO')],
    loadComponent: () =>
      import('./evolucao-treino.component').then((m) => m.EvolucaoTreinoComponent),
    resolve: { tela: resolverEvolucao },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
];
