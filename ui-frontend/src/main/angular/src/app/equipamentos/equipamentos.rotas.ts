import { inject } from '@angular/core';
import { CanActivateFn, RedirectCommand, ResolveFn, Router, Routes } from '@angular/router';
import { map } from 'rxjs';
import { ServicoAutenticacao } from '../../d5';
import { ModoPesquisa, resolverPesquisaEquipamentos } from './equipamentos-pesquisa.component';
import {
  Equipamento,
  EquipamentosService,
  Operacoes,
  Problema,
  Recurso,
} from './equipamentos.service';

const guardaEquipamentos: CanActivateFn = (_rota, estado) => {
  const autenticacao = inject(ServicoAutenticacao);
  const router = inject(Router);
  return autenticacao.verificarSeNecessario().pipe(
    map((autenticado) => {
      if (!autenticado)
        return router.createUrlTree(['/login'], { queryParams: { retorno: estado.url } });
      return autenticacao.autorizacoesDe('equipamento')?.buscar
        ? true
        : router.createUrlTree(['/inicio']);
    }),
  );
};
const guardas = [guardaEquipamentos];
const equipamento = () => import('./equipamento.component').then((m) => m.EquipamentoComponent);
const problema = () => import('./problema.component').then((m) => m.ProblemaComponent);
const ordem = () => import('./ordem.component').then((m) => m.OrdemComponent);
const resolverItem: ResolveFn<unknown> = (rota) =>
  inject(EquipamentosService).visualizar(rota.data['recurso'], rota.paramMap.get('id')!);
const resolverOperacoes: ResolveFn<Operacoes> = (rota) =>
  inject(EquipamentosService).operacoes(rota.paramMap.get('id')!);
const resolverEquipamento: ResolveFn<Equipamento> = (rota) => {
  const id = rota.queryParamMap.get('equipamento');
  return id
    ? inject(EquipamentosService).visualizar<Equipamento>('equipamento', id)
    : new RedirectCommand(inject(Router).parseUrl('/core/equipamento/pesquisa'));
};
const resolverProblema: ResolveFn<Problema> = (rota) => {
  const id = rota.queryParamMap.get('problema');
  return id
    ? inject(EquipamentosService).visualizar<Problema>('problema-equipamento', id)
    : new RedirectCommand(inject(Router).parseUrl('/core/problema-equipamento/pesquisa'));
};

const pesquisas: { path: string; recurso: Recurso; modo: ModoPesquisa }[] = [
  { path: 'equipamento/pesquisa', recurso: 'equipamento', modo: 'cadastro' },
  { path: 'equipamento/inspecoes', recurso: 'equipamento', modo: 'inspecoes' },
  { path: 'registro-equipamento/pesquisa', recurso: 'registro-equipamento', modo: 'historico' },
  { path: 'problema-equipamento/pesquisa', recurso: 'problema-equipamento', modo: 'problemas' },
  { path: 'ordem-manutencao/pesquisa', recurso: 'ordem-manutencao', modo: 'ordens' },
];

export const rotasEquipamentos: Routes = [
  ...pesquisas.map(({ path, ...data }) => ({
    path: 'core/' + path,
    canActivate: guardas,
    loadComponent: () =>
      import('./equipamentos-pesquisa.component').then((m) => m.EquipamentosPesquisaComponent),
    resolve: { lista: resolverPesquisaEquipamentos },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange' as const,
    data,
  })),
  { path: 'core/equipamento/inclusao', canActivate: guardas, loadComponent: equipamento },
  {
    path: 'core/equipamento/:id/visualizacao',
    canActivate: guardas,
    loadComponent: equipamento,
    resolve: { item: resolverOperacoes },
  },
  {
    path: 'core/registro-equipamento/:id/visualizacao',
    canActivate: guardas,
    loadComponent: () =>
      import('./registro-equipamento.component').then((m) => m.RegistroEquipamentoComponent),
    resolve: { item: resolverItem },
    data: { recurso: 'registro-equipamento' },
  },
  {
    path: 'core/problema-equipamento/inclusao',
    canActivate: guardas,
    loadComponent: problema,
    resolve: { equipamento: resolverEquipamento },
  },
  {
    path: 'core/problema-equipamento/:id/visualizacao',
    canActivate: guardas,
    loadComponent: problema,
    resolve: { item: resolverItem },
    data: { recurso: 'problema-equipamento' },
  },
  {
    path: 'core/ordem-manutencao/inclusao',
    canActivate: guardas,
    loadComponent: ordem,
    resolve: { problema: resolverProblema },
  },
  {
    path: 'core/ordem-manutencao/:id/visualizacao',
    canActivate: guardas,
    loadComponent: ordem,
    resolve: { item: resolverItem },
    data: { recurso: 'ordem-manutencao' },
  },
];
