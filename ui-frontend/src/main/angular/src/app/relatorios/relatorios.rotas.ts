import { Routes } from '@angular/router';
import { guardaPerfis } from '../guarda-perfis';
import { resolverFormulario, resolverHistorico, resolverPainel, resolverRelatorio } from './relatorios.service';

const gestor = guardaPerfis('ADMINISTRADOR');
const equipe = guardaPerfis('ADMINISTRADOR', 'PERSONAL');

export const rotasRelatorios: Routes = [
  {
    path: 'core/relatorio/painel',
    canActivate: [gestor],
    loadComponent: () => import('./painel.component').then((m) => m.PainelComponent),
    resolve: { painel: resolverPainel },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/relatorio/novo',
    canActivate: [equipe],
    loadComponent: () => import('./novo-relatorio.component').then((m) => m.NovoRelatorioComponent),
    resolve: { formulario: resolverFormulario },
  },
  {
    path: 'core/relatorio/historico',
    canActivate: [equipe],
    loadComponent: () => import('./historico-relatorios.component').then((m) => m.HistoricoRelatoriosComponent),
    resolve: { historico: resolverHistorico },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/relatorio/:id/visualizacao',
    canActivate: [equipe],
    loadComponent: () => import('./relatorio.component').then((m) => m.RelatorioComponent),
    resolve: { relatorio: resolverRelatorio },
    runGuardsAndResolvers: 'always',
  },
  {
    path: 'core/relatorio/:id/comparacao',
    canActivate: [equipe],
    loadComponent: () => import('./comparacao.component').then((m) => m.ComparacaoComponent),
    resolve: { relatorio: resolverRelatorio },
  },
  {
    path: 'core/meta-academia',
    canActivate: [gestor],
    loadComponent: () => import('./metas-academia.component').then((m) => m.MetasAcademiaComponent),
  },
];
