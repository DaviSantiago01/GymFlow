import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, ResolveFn, Router, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { EquipamentosMenuComponent } from './equipamentos-menu.component';
import {
  AREAS,
  GRAVIDADES,
  SITUACOES,
  SITUACOES_ORDEM,
  SITUACOES_PROBLEMA,
  TAMANHO_PAGINA,
  Equipamento,
  EquipamentosService,
  Ordem,
  Problema,
  Recurso,
  Registro,
  dia,
  rotulo,
} from './equipamentos.service';

interface Linha {
  id: string;
  celulas: string[];
  equipamento?: string;
}
interface Lista {
  linhas: Linha[];
  total: number;
  pagina: number;
  filtros: Record<string, string>;
}
interface Filtro {
  chave: string;
  nome: string;
  opcoes?: readonly string[];
}

const EQUIPAMENTO_FILTROS: Filtro[] = [
  { chave: 'termo', nome: 'Nome, patrimônio ou série' },
  { chave: 'area', nome: 'Área', opcoes: AREAS },
  { chave: 'tipo', nome: 'Tipo', opcoes: AREAS },
  { chave: 'situacao', nome: 'Situação', opcoes: SITUACOES },
];
const MODOS = {
  cadastro: {
    titulo: 'Equipamentos',
    descricao: 'Cadastre, consulte e atualize os equipamentos da academia.',
    colunas: ['Patrimônio', 'Equipamento', 'Área', 'Situação'],
    filtros: EQUIPAMENTO_FILTROS,
  },
  inspecoes: {
    titulo: 'Inspeções periódicas',
    descricao: 'Acompanhe os prazos e registre a inspeção pelo equipamento.',
    colunas: [
      'Patrimônio',
      'Equipamento',
      'Área',
      'Situação',
      'Última inspeção',
      'Próxima inspeção',
      'Prazo',
    ],
    filtros: EQUIPAMENTO_FILTROS,
  },
  historico: {
    titulo: 'Histórico de equipamentos',
    descricao: 'Consulte inspeções e mudanças de área ou situação.',
    colunas: [
      'Data',
      'Equipamento',
      'Área anterior',
      'Área atual',
      'Situação anterior',
      'Situação atual',
      'Resultado',
    ],
    filtros: [{ chave: 'equipamento', nome: 'Código do equipamento' }],
  },
  problemas: {
    titulo: 'Problemas em equipamentos',
    descricao: 'Para relatar um problema, abra o equipamento correspondente.',
    colunas: ['Problema', 'Equipamento', 'Data', 'Gravidade', 'Risco à segurança', 'Situação'],
    filtros: [
      { chave: 'equipamento', nome: 'Código do equipamento' },
      { chave: 'situacao', nome: 'Situação', opcoes: SITUACOES_PROBLEMA },
      { chave: 'gravidade', nome: 'Gravidade', opcoes: GRAVIDADES },
    ],
  },
  ordens: {
    titulo: 'Ordens de manutenção',
    descricao: 'Abra uma ordem pelo detalhe do problema e acompanhe o serviço aqui.',
    colunas: ['Ordem', 'Problema', 'Técnico', 'Abertura', 'Situação'],
    filtros: [
      { chave: 'problema', nome: 'Código do problema' },
      { chave: 'situacao', nome: 'Situação', opcoes: SITUACOES_ORDEM },
    ],
  },
} satisfies Record<
  string,
  { titulo: string; descricao: string; colunas: string[]; filtros: Filtro[] }
>;

export type ModoPesquisa = keyof typeof MODOS;

export const resolverPesquisaEquipamentos: ResolveFn<Lista> = (rota) => {
  const api = inject(EquipamentosService);
  const modo = rota.data['modo'] as ModoPesquisa;
  const recurso = rota.data['recurso'] as Recurso;
  const filtros: Record<string, string> = {};
  for (const filtro of MODOS[modo].filtros as Filtro[]) {
    const valor = rota.queryParamMap.get(filtro.chave)?.trim();
    if (valor && (!filtro.opcoes || filtro.opcoes.includes(valor))) filtros[filtro.chave] = valor;
  }
  const indice = Number(rota.queryParamMap.get('pagina'));
  const pagina = Number.isSafeInteger(indice) && indice > 0 ? indice : 0;
  const consulta = { ...filtros, indicePagina: pagina, descendente: recurso !== 'equipamento' };

  function pesquisar<T>(formatar: (objeto: T) => Linha) {
    return api.pesquisar<T>(recurso, consulta).pipe(
      map(({ resultado }) => ({
        linhas: resultado.projecoes.map(formatar),
        total: resultado.total,
        pagina,
        filtros,
      })),
    );
  }

  switch (recurso) {
    case 'equipamento':
      return pesquisar<Equipamento>((item) => {
        const celulas = [item.patrimonio, item.nome, rotulo(item.area), rotulo(item.situacao)];
        if (modo === 'inspecoes') {
          celulas.push(
            dia(item.ultimaInspecao),
            dia(item.proximaInspecao),
            item.situacao === 'INATIVO' ? '—' : rotulo(item.prazoInspecao),
          );
        }
        return { id: item.identificador, celulas };
      });
    case 'registro-equipamento':
      return pesquisar<Registro>((item) => ({
        id: item.identificador,
        equipamento: item.equipamento,
        celulas: [
          dia(item.data),
          item.equipamentoNome,
          rotulo(item.areaAnterior),
          rotulo(item.areaNova),
          rotulo(item.situacaoAnterior),
          rotulo(item.situacaoNova),
          rotulo(item.resultado),
        ],
      }));
    case 'problema-equipamento':
      return pesquisar<Problema>((item) => ({
        id: item.identificador,
        celulas: [
          item.titulo,
          item.equipamentoNome,
          dia(item.dataRelato),
          rotulo(item.gravidade),
          item.risco ? 'Sim' : 'Não',
          rotulo(item.situacao),
        ],
      }));
    case 'ordem-manutencao':
      return pesquisar<Ordem>((item) => ({
        id: item.identificador,
        celulas: [
          '#' + item.identificador,
          item.problemaTitulo,
          item.tecnico,
          dia(item.abertura),
          rotulo(item.situacao),
        ],
      }));
  }
};

@Component({
  selector: 'app-equipamentos-pesquisa',
  imports: [FormsModule, RouterLink, EquipamentosMenuComponent],
  templateUrl: './equipamentos-pesquisa.component.html',
  styleUrl: './equipamentos.scss',
})
export class EquipamentosPesquisaComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly dados = toSignal(this.rota.data, { requireSync: true });
  readonly modo = this.rota.snapshot.data['modo'] as ModoPesquisa;
  readonly recurso = this.rota.snapshot.data['recurso'] as Recurso;
  readonly descricao = MODOS[this.modo];
  readonly filtros: Filtro[] = this.descricao.filtros;
  readonly lista = computed(() => this.dados()['lista'] as Lista);
  readonly quantidadePaginas = computed(() =>
    Math.max(1, Math.ceil(this.lista().total / TAMANHO_PAGINA)),
  );
  readonly rotulo = rotulo;

  filtrar(valores: Record<string, string>) {
    const queryParams = Object.fromEntries(
      Object.entries(valores)
        .map(([chave, valor]) => [chave, valor.trim()])
        .filter(([, valor]) => valor),
    );
    void this.router.navigate([], { relativeTo: this.rota, queryParams });
  }
}
