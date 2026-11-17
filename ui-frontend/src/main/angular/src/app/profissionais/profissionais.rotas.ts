import { Routes } from '@angular/router';
import { guardaAutenticacao } from '../../d5';
import { resolverProfissionais, resolverProfissional } from './profissionais.service';

const formulario = () =>
  import('./profissional-formulario.component').then((m) => m.ProfissionalFormularioComponent);

export const rotasProfissionais: Routes = [
  {
    path: 'core/profissional/pesquisa',
    canActivate: [guardaAutenticacao],
    loadComponent: () => import('./profissionais.component').then((m) => m.ProfissionaisComponent),
    resolve: { tela: resolverProfissionais },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/profissional/inclusao',
    canActivate: [guardaAutenticacao],
    loadComponent: formulario,
    resolve: { tela: resolverProfissional },
  },
  {
    path: 'core/profissional/:id/alteracao',
    canActivate: [guardaAutenticacao],
    loadComponent: formulario,
    resolve: { tela: resolverProfissional },
  },
  {
    path: 'core/profissional/:id/visualizacao',
    canActivate: [guardaAutenticacao],
    loadComponent: () => import('./profissional.component').then((m) => m.ProfissionalComponent),
    resolve: { tela: resolverProfissional },
  },
];
