import { Routes } from '@angular/router';
import { guardaPerfis } from '../guarda-perfis';
import { resolverAlunos, resolverFicha, resolverPresentes } from './alunos.service';

const somenteGestor = guardaPerfis('ADMINISTRADOR');
const formulario = () => import('./aluno-formulario.component').then((m) => m.AlunoFormularioComponent);

/** F005 (gerenciamento de alunos) e F006 (recepção, entrada e frequência). */
export const rotasAlunos: Routes = [
  {
    path: 'core/aluno/gestao',
    canActivate: [somenteGestor],
    loadComponent: () => import('./alunos.component').then((m) => m.AlunosComponent),
    resolve: { tela: resolverAlunos },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
  },
  {
    path: 'core/aluno/inclusao',
    canActivate: [somenteGestor],
    loadComponent: formulario,
    resolve: { tela: resolverFicha },
  },
  {
    path: 'core/aluno/:id/alteracao',
    canActivate: [somenteGestor],
    loadComponent: formulario,
    resolve: { tela: resolverFicha },
  },
  {
    path: 'core/aluno/:id/ficha',
    canActivate: [somenteGestor],
    loadComponent: () => import('./aluno-ficha.component').then((m) => m.AlunoFichaComponent),
    resolve: { tela: resolverFicha },
  },
  {
    path: 'core/registro-acesso/recepcao',
    canActivate: [somenteGestor],
    loadComponent: () => import('./recepcao.component').then((m) => m.RecepcaoComponent),
    resolve: { tela: resolverPresentes },
  },
];
