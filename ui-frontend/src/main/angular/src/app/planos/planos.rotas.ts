import { Routes } from '@angular/router';
import { guardaPerfis } from '../guarda-perfis';
import {
  resolverAdesao,
  resolverAdesoes,
  resolverFormularioAdesao,
  resolverMensalidade,
  resolverMensalidades,
  resolverMeuPlano,
  resolverPlano,
  resolverPlanos,
} from './planos.service';

const formularioPlano = () =>
  import('./plano-formulario.component').then((m) => m.PlanoFormularioComponent);

export const rotasPlanos: Routes = [
  {
    path: 'core/plano/pesquisa',
    canActivate: [guardaPerfis('ADMINISTRADOR')],
    loadComponent: () => import('./planos.component').then((m) => m.PlanosComponent),
    resolve: { tela: resolverPlanos },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/plano/inclusao',
    canActivate: [guardaPerfis('ADMINISTRADOR')],
    loadComponent: formularioPlano,
    resolve: { tela: resolverPlano },
  },
  {
    path: 'core/plano/:id/alteracao',
    canActivate: [guardaPerfis('ADMINISTRADOR')],
    loadComponent: formularioPlano,
    resolve: { tela: resolverPlano },
  },
  {
    path: 'core/adesao-plano/pesquisa',
    canActivate: [guardaPerfis('ADMINISTRADOR')],
    loadComponent: () => import('./adesoes.component').then((m) => m.AdesoesComponent),
    resolve: { tela: resolverAdesoes, formulario: resolverFormularioAdesao },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/adesao-plano/meu-plano',
    canActivate: [guardaPerfis('USUARIO')],
    loadComponent: () => import('./meu-plano.component').then((m) => m.MeuPlanoComponent),
    resolve: { tela: resolverMeuPlano },
  },
  {
    path: 'core/adesao-plano/:id/visualizacao',
    canActivate: [guardaPerfis('ADMINISTRADOR', 'USUARIO')],
    loadComponent: () => import('./adesao.component').then((m) => m.AdesaoComponent),
    resolve: { tela: resolverAdesao },
    runGuardsAndResolvers: 'always',
  },
  {
    path: 'core/mensalidade/pesquisa',
    canActivate: [guardaPerfis('ADMINISTRADOR')],
    loadComponent: () => import('./mensalidades.component').then((m) => m.MensalidadesComponent),
    resolve: { tela: resolverMensalidades },
    runGuardsAndResolvers: 'always',
  },
  {
    path: 'core/mensalidade/:id/visualizacao',
    canActivate: [guardaPerfis('ADMINISTRADOR', 'USUARIO')],
    loadComponent: () => import('./mensalidade.component').then((m) => m.MensalidadeComponent),
    resolve: { tela: resolverMensalidade },
  },
];
