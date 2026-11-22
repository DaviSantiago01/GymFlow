import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ResolveFn } from '@angular/router';
import { IAgregadoAutorizacoes } from '../../d5';

export type TipoRelatorio =
  | 'DESEMPENHO_GERAL'
  | 'MATRICULAS_EVASAO'
  | 'FREQUENCIA_OCUPACAO'
  | 'RECEITA_INADIMPLENCIA'
  | 'DESEMPENHO_PERSONAIS'
  | 'RISCO_EVASAO'
  | 'ADESAO_TREINOS'
  | 'METAS_ACADEMIA';
export type Agrupamento = 'DIA' | 'SEMANA' | 'MES';
export type Indicador =
  | 'ALUNOS_ATIVOS'
  | 'NOVAS_MATRICULAS'
  | 'REATIVACOES'
  | 'FREQUENCIA_MEDIA'
  | 'TAXA_EVASAO'
  | 'TAXA_RENOVACAO'
  | 'RECEITA_LIQUIDA'
  | 'TAXA_INADIMPLENCIA'
  | 'ALUNOS_EM_RISCO';
export type Resultado = 'MELHOROU' | 'PIOROU' | 'ESTAVEL' | 'SEM_BASE';
export type Classificacao = 'PICO' | 'NORMAL' | 'BAIXA' | 'FECHADO';
export type DiaSemana = 'SEGUNDA' | 'TERCA' | 'QUARTA' | 'QUINTA' | 'SEXTA' | 'SABADO' | 'DOMINGO';

export interface TipoInfo {
  titulo: string;
  descricao: string;
  icone: string;
  filtros: ('PLANO' | 'MODALIDADE' | 'PERSONAL' | 'AULA_COLETIVA')[];
  personal: boolean;
}

export const TIPOS: Record<TipoRelatorio, TipoInfo> = {
  DESEMPENHO_GERAL: {
    titulo: 'Desempenho geral',
    descricao: 'Alunos ativos, matrículas, evasão, renovação, frequência e receita do período.',
    icone: '▦',
    filtros: ['PLANO'],
    personal: false,
  },
  MATRICULAS_EVASAO: {
    titulo: 'Matrículas e evasão',
    descricao: 'Novas matrículas, reativações, cancelamentos, trancamentos e renovações.',
    icone: '⇄',
    filtros: ['PLANO'],
    personal: false,
  },
  FREQUENCIA_OCUPACAO: {
    titulo: 'Frequência e ocupação',
    descricao: 'Presenças por aluno, horários de pico e ocupação das aulas coletivas e do estacionamento.',
    icone: '◷',
    filtros: ['PLANO', 'MODALIDADE', 'AULA_COLETIVA'],
    personal: false,
  },
  RECEITA_INADIMPLENCIA: {
    titulo: 'Receita e inadimplência',
    descricao: 'Pagamentos confirmados, estornos, mensalidades vencidas e valor em aberto.',
    icone: '$',
    filtros: ['PLANO'],
    personal: false,
  },
  DESEMPENHO_PERSONAIS: {
    titulo: 'Desempenho dos personais',
    descricao: 'Alunos com plano ativo, adesão aos treinos e metas atingidas, por personal.',
    icone: '☺',
    filtros: ['PERSONAL'],
    personal: false,
  },
  RISCO_EVASAO: {
    titulo: 'Alunos em risco de evasão',
    descricao: 'Alunos sem entrada recente ou com queda de frequência em relação ao período anterior.',
    icone: '⚠',
    filtros: ['PLANO', 'PERSONAL'],
    personal: true,
  },
  ADESAO_TREINOS: {
    titulo: 'Adesão aos treinos',
    descricao: 'Execuções registradas, adesão aos planos de treino e metas atingidas pelos alunos.',
    icone: '✓',
    filtros: ['PERSONAL'],
    personal: true,
  },
  METAS_ACADEMIA: {
    titulo: 'Metas da academia',
    descricao: 'Cumprimento de cada meta definida pela academia para o período.',
    icone: '◎',
    filtros: [],
    personal: false,
  },
};

export const INDICADORES: Record<Indicador, { nome: string; unidade: 'QUANTIDADE' | 'DECIMAL' | 'PERCENTUAL' | 'MOEDA'; menorMelhor: boolean }> = {
  ALUNOS_ATIVOS: { nome: 'Alunos ativos', unidade: 'QUANTIDADE', menorMelhor: false },
  NOVAS_MATRICULAS: { nome: 'Novas matrículas', unidade: 'QUANTIDADE', menorMelhor: false },
  REATIVACOES: { nome: 'Reativações', unidade: 'QUANTIDADE', menorMelhor: false },
  FREQUENCIA_MEDIA: { nome: 'Frequência média por semana', unidade: 'DECIMAL', menorMelhor: false },
  TAXA_EVASAO: { nome: 'Taxa de evasão', unidade: 'PERCENTUAL', menorMelhor: true },
  TAXA_RENOVACAO: { nome: 'Taxa de renovação', unidade: 'PERCENTUAL', menorMelhor: false },
  RECEITA_LIQUIDA: { nome: 'Receita líquida', unidade: 'MOEDA', menorMelhor: false },
  TAXA_INADIMPLENCIA: { nome: 'Taxa de inadimplência', unidade: 'PERCENTUAL', menorMelhor: true },
  ALUNOS_EM_RISCO: { nome: 'Alunos em risco de evasão', unidade: 'QUANTIDADE', menorMelhor: true },
};

export const DIAS: { valor: DiaSemana; curto: string }[] = [
  { valor: 'SEGUNDA', curto: 'Seg' },
  { valor: 'TERCA', curto: 'Ter' },
  { valor: 'QUARTA', curto: 'Qua' },
  { valor: 'QUINTA', curto: 'Qui' },
  { valor: 'SEXTA', curto: 'Sex' },
  { valor: 'SABADO', curto: 'Sáb' },
  { valor: 'DOMINGO', curto: 'Dom' },
];

export interface Variacao {
  absoluta: number;
  percentual: number | null;
  resultado: Resultado;
}
export interface ValorIndicador {
  indicador: Indicador;
  valor: number;
  anterior: number | null;
  variacao: Variacao | null;
}
export interface Resumo {
  alunosAtivos: number;
  presencas: number;
  pagamentos: number;
  mensalidades: number;
  planosTreino: number;
}
export interface ResultadoGrupo {
  rotulo: string;
  inicio: string;
  fim: string;
  frequencia: number;
  receita: number;
  presencas: number;
  novasMatriculas: number;
}
export interface Movimento {
  inicio: number;
  novasMatriculas: number;
  reativacoes: number;
  renovacoes: number;
  evasoes: number;
  saldoTrancamentos: number;
  fim: number;
}
export interface ResultadoPlano {
  plano: string;
  nome: string;
  alunosAtivos: number;
  novasMatriculas: number;
  taxaEvasao: number;
  receita: number;
  participacao: number;
}
export interface CelulaOcupacao {
  dia: DiaSemana;
  inicio: string;
  media: number;
  percentual: number;
  classificacao: Classificacao;
}
export interface OcupacaoAula {
  aula: string;
  nome: string;
  modalidade: string;
  dia: DiaSemana;
  inicio: string;
  inscritos: number;
  capacidade: number;
  percentual: number;
  classificacao: Classificacao;
}
export interface Ocupacao {
  capacidadeSala: number;
  frequenciaMedia: number;
  presencas: number;
  sala: CelulaOcupacao[];
  aulas: OcupacaoAula[];
  estacionamento: {
    vagas: number;
    percentualMedio: number;
    horarioPico: string | null;
    percentualPico: number;
    faixas: { inicio: string; percentual: number }[];
  };
}
export interface Financeiro {
  receitaBruta: number;
  pagamentos: number;
  estornos: number;
  quantidadeEstornos: number;
  receitaLiquida: number;
  totalComVencimento: number;
  valorEmAberto: number;
  taxaInadimplencia: number;
  faixasAtraso: { rotulo: string; valor: number; alunos: number }[];
  atrasadas: {
    aluno: string;
    nome: string;
    plano: string;
    vencimento: string;
    diasAtraso: number;
    valor: number;
  }[];
}
export interface DesempenhoPersonal {
  personal: string;
  nome: string;
  alunosComPlano: number;
  diasPrevistos: number;
  diasComTreino: number;
  adesao: number;
  variacaoAdesao: number | null;
  metasAtingidas: number;
  alunosEmRisco: number;
}
export interface AlunoEmRisco {
  aluno: string;
  nome: string;
  plano: string;
  personal: string | null;
  ultimaEntrada: string | null;
  diasSemEntrar: number;
  frequenciaAnterior: number;
  frequenciaAtual: number;
  motivo: 'SEM_ENTRADA' | 'QUEDA_FREQUENCIA';
  queda: number | null;
}
export interface CumprimentoMeta {
  meta: string;
  indicador: Indicador;
  desejado: number;
  realizado: number;
  percentual: number;
  atingida: boolean;
  inicio: string;
  fim: string;
}
export interface Conteudo {
  resumo: Resumo;
  indicadores: ValorIndicador[];
  grupos: ResultadoGrupo[];
  movimento: Movimento | null;
  planos: ResultadoPlano[];
  ocupacao: Ocupacao | null;
  financeiro: Financeiro | null;
  personais: DesempenhoPersonal[];
  alunosEmRisco: AlunoEmRisco[];
  metas: CumprimentoMeta[];
}
export interface AutorizacoesRelatorio extends IAgregadoAutorizacoes {
  gerar: boolean;
  exportar: boolean;
  gerarNovaVersao: boolean;
  painel: boolean;
  tipos: TipoRelatorio[];
}
export interface Relatorio {
  identificador: string;
  tipo: TipoRelatorio;
  titulo: string;
  inicio: string;
  fim: string;
  dias: number;
  anteriorInicio: string;
  anteriorFim: string;
  agrupamento: Agrupamento;
  comparar: boolean;
  descricaoFiltros: string;
  geracao: string;
  usuario: string;
  usuarioNome: string;
  perfil: 'ADMINISTRADOR' | 'PERSONAL' | 'USUARIO';
  versao: number;
  anterior: string | null;
  situacao: 'ATUAL' | 'SUBSTITUIDO';
  conteudo: Conteudo;
  alteracoes: string[];
  contatos: { aluno: string; cpf: string; telefone: string }[];
  autorizacoes: AutorizacoesRelatorio;
}
export interface ItemHistorico {
  identificador: string;
  tipo: TipoRelatorio;
  titulo: string;
  inicio: string;
  fim: string;
  agrupamento: Agrupamento;
  descricaoFiltros: string;
  geracao: string;
  usuario: string;
  usuarioNome: string;
  perfil: string;
  versao: number;
  situacao: 'ATUAL' | 'SUBSTITUIDO';
}
export interface Opcao {
  identificador: string;
  nome: string;
}
export interface FormularioRelatorio {
  tipos: TipoRelatorio[];
  planos: Opcao[];
  personais: Opcao[];
  aulas: Opcao[];
  modalidades: string[];
  hoje: string;
  periodoMaximoDias: number;
  agrupamentoDiarioMaximoDias: number;
  personalRestrito: boolean;
}
export interface Requisicao {
  tipo: TipoRelatorio;
  inicio: string;
  fim: string;
  agrupamento: Agrupamento;
  plano: string | null;
  modalidade: string | null;
  personal: string | null;
  aula: string | null;
  comparar: boolean;
}
export interface Painel {
  periodo: { inicio: string; fim: string };
  anterior: { inicio: string; fim: string };
  conteudo: Conteudo;
  alunosAtivos: { mes: string; valor: number }[];
  metas: CumprimentoMeta[];
}
export interface Exportacao {
  titulo: string;
  cabecalho: string[];
  tabelas: { titulo: string; colunas: string[]; linhas: string[][] }[];
}
export interface MetaAcademia {
  identificador: string;
  indicador: Indicador;
  descricao: string;
  sentido: 'MAIOR_MELHOR' | 'MENOR_MELHOR';
  valorDesejado: number;
  inicio: string;
  fim: string;
  situacao: 'ATIVA' | 'ENCERRADA' | 'SUBSTITUIDA';
  criacao: string;
  responsavel: string;
}
export interface Acompanhamento {
  mes: string;
  inicio: string;
  fim: string;
  apuradoAte: string;
  encerrado: boolean;
  metas: CumprimentoMeta[];
}
export interface Pesquisa<T> {
  resultado: { projecoes: T[]; total: number };
}

@Injectable({ providedIn: 'root' })
export class RelatoriosService {
  private readonly http = inject(HttpClient);
  private readonly base = '/backend/core/relatorio';
  private readonly metas = '/backend/core/meta-academia';

  painel(mes: string | null) {
    return this.http.get<Painel>(`${this.base}/painel`, { params: parametros({ mes }) });
  }
  formulario() {
    return this.http.get<FormularioRelatorio>(`${this.base}/inclusao`);
  }
  verificar(requisicao: Requisicao) {
    return this.http.post<Resumo>(`${this.base}/verificar`, requisicao);
  }
  gerar(requisicao: Requisicao) {
    return this.http.post<Relatorio>(`${this.base}/gerar`, requisicao);
  }
  visualizar(id: string) {
    return this.http.get<Relatorio>(`${this.base}/${id}/visualizacao`);
  }
  novaVersao(id: string) {
    return this.http.post<Relatorio>(`${this.base}/${id}/nova-versao`, null);
  }
  exportacao(id: string) {
    return this.http.get<Exportacao>(`${this.base}/${id}/exportacao`);
  }
  arquivo(id: string, formato: 'PDF' | 'PLANILHA') {
    return this.http.get(`${this.base}/${id}/exportar`, {
      params: parametros({ formato }),
      responseType: 'blob',
      observe: 'response',
    });
  }
  historico(tipo: string | null, usuario: string | null, dias: number | null, termo: string | null) {
    return this.http.get<Pesquisa<ItemHistorico>>(`${this.base}/pesquisa`, {
      params: parametros({ ordenacao: 'GERACAO', descendente: true, tamanhoPagina: 200, tipo, usuario, dias, termo }),
    });
  }

  listarMetas() {
    return this.http.get<Pesquisa<MetaAcademia>>(`${this.metas}/pesquisa`, {
      params: parametros({ ordenacao: 'INICIO', descendente: true, tamanhoPagina: 200 }),
    });
  }
  acompanhamento(mes: string | null) {
    return this.http.get<Acompanhamento>(`${this.metas}/acompanhamento`, { params: parametros({ mes }) });
  }
  cadastrarMeta(meta: { indicador: Indicador; valorDesejado: number | null; inicio: string; fim: string }) {
    return this.http.post<MetaAcademia>(`${this.metas}/cadastrar`, meta);
  }
  encerrarMeta(id: string) {
    return this.http.post<MetaAcademia>(`${this.metas}/${id}/encerrar`, null);
  }
  substituirMeta(id: string, meta: { valorDesejado: number | null; inicio: string; fim: string }) {
    return this.http.post<MetaAcademia>(`${this.metas}/${id}/substituir`, meta);
  }
}

function parametros(valores: Record<string, string | number | boolean | null>): HttpParams {
  let resultado = new HttpParams();
  for (const [chave, valor] of Object.entries(valores)) {
    if (valor !== null && valor !== '') resultado = resultado.set(chave, valor);
  }
  return resultado;
}

export const resolverPainel: ResolveFn<Painel> = (rota) =>
  inject(RelatoriosService).painel(rota.queryParamMap.get('mes'));
export const resolverFormulario: ResolveFn<FormularioRelatorio> = () => inject(RelatoriosService).formulario();
export const resolverRelatorio: ResolveFn<Relatorio> = (rota) =>
  inject(RelatoriosService).visualizar(rota.paramMap.get('id')!);
export const resolverHistorico: ResolveFn<Pesquisa<ItemHistorico>> = (rota) => {
  const dias = rota.queryParamMap.get('dias');
  return inject(RelatoriosService).historico(
    rota.queryParamMap.get('tipo'),
    rota.queryParamMap.get('usuario'),
    dias === null ? 60 : dias === 'todos' ? null : Number(dias),
    rota.queryParamMap.get('termo'),
  );
};

// ------------------------------------------------------------ formatação

export function numero(valor: number | null | undefined, casas = 0): string {
  if (valor === null || valor === undefined) return '—';
  return valor.toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas });
}
export function decimal(valor: number | null | undefined): string {
  return numero(valor, 1);
}
export function percentual(valor: number | null | undefined): string {
  return valor === null || valor === undefined ? '—' : `${decimal(valor)}%`;
}
export function moeda(valor: number | null | undefined): string {
  if (valor === null || valor === undefined) return '—';
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}
export function moedaCurta(valor: number): string {
  if (Math.abs(valor) >= 1000) return `R$ ${decimal(valor / 1000)} mil`;
  return moeda(valor);
}
export function valorIndicador(indicador: Indicador, valor: number | null | undefined): string {
  switch (INDICADORES[indicador].unidade) {
    case 'QUANTIDADE':
      return numero(valor);
    case 'DECIMAL':
      return decimal(valor);
    case 'PERCENTUAL':
      return percentual(valor);
    case 'MOEDA':
      return moeda(valor);
  }
}
export function sinal(valor: number): string {
  return valor > 0 ? '+' : valor < 0 ? '−' : '';
}
/** Em taxas, a variação absoluta é em pontos percentuais; nas demais, na unidade do indicador. */
export function variacaoAbsoluta(indicador: Indicador, variacao: Variacao | null, anterior: number | null): string {
  if (!variacao || anterior === null) return '—';
  const unidade = INDICADORES[indicador].unidade;
  const abs = Math.abs(variacao.absoluta);
  if (unidade === 'PERCENTUAL') return `${sinal(variacao.absoluta)}${decimal(abs)} p.p.`;
  if (unidade === 'MOEDA') return `${sinal(variacao.absoluta)}${moeda(abs)}`;
  if (unidade === 'DECIMAL') return `${sinal(variacao.absoluta)}${decimal(abs)}`;
  return `${sinal(variacao.absoluta)}${numero(abs)}`;
}
/** Texto curto para os cartões: p.p. nas taxas e percentual nos demais indicadores. */
export function variacaoCurta(indicador: Indicador, variacao: Variacao | null): string {
  if (!variacao || variacao.resultado === 'SEM_BASE') return 'sem base';
  if (INDICADORES[indicador].unidade === 'PERCENTUAL')
    return `${sinal(variacao.absoluta)}${decimal(Math.abs(variacao.absoluta))} p.p.`;
  if (variacao.percentual === null) return `${sinal(variacao.absoluta)}${numero(Math.abs(variacao.absoluta))}`;
  return `${sinal(variacao.percentual)}${decimal(Math.abs(variacao.percentual))}%`;
}
export function data(valor: string | null | undefined): string {
  if (!valor) return '—';
  const [ano, mes, dia] = valor.substring(0, 10).split('-');
  return `${dia}/${mes}/${ano}`;
}
export function dataHora(valor: string): string {
  return new Date(valor).toLocaleString('pt-BR', {
    timeZone: 'America/Sao_Paulo',
    dateStyle: 'short',
    timeStyle: 'short',
  });
}
export function hora(valor: string): string {
  const [h, m] = valor.split(':');
  return m === '00' ? `${Number(h)}h` : `${Number(h)}h${m}`;
}
export function diaCurto(dia: DiaSemana): string {
  return DIAS.find((item) => item.valor === dia)?.curto ?? dia;
}
const MESES = ['janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho', 'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'];
export function nomeMes(anoMes: string, comAno = true): string {
  const [ano, mes] = anoMes.split('-').map(Number);
  const nome = MESES[mes - 1];
  return comAno ? `${nome} de ${ano}` : nome;
}
export function hojeIso(): string {
  const agora = new Date();
  const local = new Date(agora.toLocaleString('en-US', { timeZone: 'America/Sao_Paulo' }));
  return isoLocal(local);
}
export function isoLocal(dataLocal: Date): string {
  const ano = dataLocal.getFullYear();
  const mes = String(dataLocal.getMonth() + 1).padStart(2, '0');
  const dia = String(dataLocal.getDate()).padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}
export function somarDias(iso: string, dias: number): string {
  const [ano, mes, dia] = iso.split('-').map(Number);
  return isoLocal(new Date(ano, mes - 1, dia + dias));
}
export function diasEntre(inicio: string, fim: string): number {
  const [a1, m1, d1] = inicio.split('-').map(Number);
  const [a2, m2, d2] = fim.split('-').map(Number);
  return Math.round((Date.UTC(a2, m2 - 1, d2) - Date.UTC(a1, m1 - 1, d1)) / 86400000) + 1;
}
export function salvarArquivo(conteudo: Blob, nome: string) {
  const url = URL.createObjectURL(conteudo);
  const link = document.createElement('a');
  link.href = url;
  link.download = nome;
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
