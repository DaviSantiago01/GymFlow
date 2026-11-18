import { Routes } from '@angular/router';
import { guardaPerfis } from '../guarda-perfis';
import {
  resolverAlteracaoAvaliacao,
  resolverAlteracaoMeta,
  resolverAvaliacoes,
  resolverComparacao,
  resolverEvolucaoIndicador,
  resolverInclusaoAvaliacao,
  resolverInclusaoMeta,
  resolverMetas,
  resolverSituacao,
  resolverVisualizacaoAvaliacao,
} from './avaliacoes.service';

const formularioAvaliacao = () =>
  import('./avaliacao-formulario.component').then((m) => m.AvaliacaoFormularioComponent);
const formularioMeta = () =>
  import('./meta-formulario.component').then((m) => m.MetaFormularioComponent);

export const rotasAvaliacoes: Routes = [
  {
    path: 'core/avaliacao-fisica/inclusao',
    canActivate: [guardaPerfis('PERSONAL')],
    loadComponent: formularioAvaliacao,
    resolve: { tela: resolverInclusaoAvaliacao },
  },
  {
    path: 'core/avaliacao-fisica/pesquisa',
    canActivate: [guardaPerfis('PERSONAL', 'USUARIO')],
    loadComponent: () => import('./avaliacoes.component').then((m) => m.AvaliacoesComponent),
    resolve: { tela: resolverAvaliacoes },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/avaliacao-fisica/evolucao',
    canActivate: [guardaPerfis('PERSONAL', 'USUARIO')],
    loadComponent: () =>
      import('./avaliacao-evolucao.component').then((m) => m.AvaliacaoEvolucaoComponent),
    resolve: { tela: resolverEvolucaoIndicador },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/avaliacao-fisica/situacao',
    canActivate: [guardaPerfis('PERSONAL', 'USUARIO')],
    loadComponent: () =>
      import('./avaliacao-situacao.component').then((m) => m.AvaliacaoSituacaoComponent),
    resolve: { tela: resolverSituacao },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/avaliacao-fisica/:id/visualizacao',
    canActivate: [guardaPerfis('PERSONAL', 'USUARIO')],
    loadComponent: () =>
      import('./avaliacao-resultado.component').then((m) => m.AvaliacaoResultadoComponent),
    resolve: { tela: resolverVisualizacaoAvaliacao },
  },
  {
    path: 'core/avaliacao-fisica/:id/alteracao',
    canActivate: [guardaPerfis('PERSONAL')],
    loadComponent: formularioAvaliacao,
    resolve: { tela: resolverAlteracaoAvaliacao },
  },
  {
    path: 'core/avaliacao-fisica/:id/comparacao',
    canActivate: [guardaPerfis('PERSONAL', 'USUARIO')],
    loadComponent: () =>
      import('./avaliacao-comparacao.component').then((m) => m.AvaliacaoComparacaoComponent),
    resolve: { tela: resolverComparacao },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/meta/pesquisa',
    canActivate: [guardaPerfis('PERSONAL', 'USUARIO')],
    loadComponent: () => import('./metas.component').then((m) => m.MetasComponent),
    resolve: { tela: resolverMetas },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/meta/inclusao',
    canActivate: [guardaPerfis('PERSONAL')],
    loadComponent: formularioMeta,
    resolve: { tela: resolverInclusaoMeta },
  },
  {
    path: 'core/meta/:id/alteracao',
    canActivate: [guardaPerfis('PERSONAL')],
    loadComponent: formularioMeta,
    resolve: { tela: resolverAlteracaoMeta },
  },
];
