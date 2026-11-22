import { Routes } from '@angular/router';
import { guardaPerfis } from '../guarda-perfis';

const tela = () => import('./aulas-desafios.component').then((m) => m.AulasDesafiosComponent);
const gestao = [guardaPerfis('ADMINISTRADOR', 'PERSONAL')];
const aluno = [guardaPerfis('USUARIO')];
export const rotasAulasDesafios: Routes = [
  { path: 'core/aulas/gestao', canActivate: gestao, loadComponent: tela, data: { modo: 'aulas-gestao' } },
  { path: 'core/aulas/cadastro', canActivate: gestao, loadComponent: tela, data: { modo: 'aulas-gestao' } },
  { path: 'core/aulas/grade', canActivate: aluno, loadComponent: tela, data: { modo: 'aulas-grade' } },
  { path: 'core/aulas/minhas', canActivate: aluno, loadComponent: tela, data: { modo: 'aulas-minhas' } },
  { path: 'core/aulas/:id/inscritos', canActivate: gestao, loadComponent: tela, data: { modo: 'aulas-inscritos' } },
  { path: 'core/desafios/avaliacoes', canActivate: gestao, loadComponent: tela, data: { modo: 'desafio-avaliacoes' } },
  { path: 'core/desafios/inscricao', canActivate: aluno, loadComponent: tela, data: { modo: 'desafio-inscricao' } },
  { path: 'core/desafios/:id/evolucao', canActivate: [guardaPerfis('ADMINISTRADOR', 'PERSONAL', 'USUARIO')], loadComponent: tela, data: { modo: 'desafio-evolucao' } },
  { path: 'core/desafios', canActivate: gestao, loadComponent: tela, data: { modo: 'desafios' } },
];
