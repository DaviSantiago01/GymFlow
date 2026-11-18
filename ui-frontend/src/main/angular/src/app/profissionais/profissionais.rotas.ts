import { Routes } from '@angular/router';
import { guardaPerfis } from '../guarda-perfis';
import { resolverProfissionais, resolverProfissional } from './profissionais.service';

const formulario = () =>
  import('./profissional-formulario.component').then((m) => m.ProfissionalFormularioComponent);

export const rotasProfissionais: Routes = [
  {
    path: 'core/profissional/pesquisa',
    canActivate: [guardaPerfis('ADMINISTRADOR')],
    loadComponent: () => import('./profissionais.component').then((m) => m.ProfissionaisComponent),
    resolve: { tela: resolverProfissionais },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/profissional/inclusao',
    canActivate: [guardaPerfis('ADMINISTRADOR')],
    loadComponent: formulario,
    resolve: { tela: resolverProfissional },
  },
  {
    path: 'core/profissional/:id/alteracao',
    canActivate: [guardaPerfis('ADMINISTRADOR')],
    loadComponent: formulario,
    resolve: { tela: resolverProfissional },
  },
  {
    path: 'core/profissional/:id/visualizacao',
    canActivate: [guardaPerfis('ADMINISTRADOR')],
    loadComponent: () => import('./profissional.component').then((m) => m.ProfissionalComponent),
    resolve: { tela: resolverProfissional },
  },
];
