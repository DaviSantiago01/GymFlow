import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ResolveFn } from '@angular/router';
import { IAgregadoAutorizacoes } from '../../d5';

export type Categoria =
  | 'ACOMPANHAMENTO_FISICO'
  | 'FREQUENCIA_HORARIOS'
  | 'AJUSTES_TREINO'
  | 'AULAS_COLETIVAS'
  | 'PLANOS_SERVICOS';
export type Criterio =
  | 'REAVALIACAO_FISICA'
  | 'REENGAJAMENTO'
  | 'SUBSTITUICAO_EXERCICIO'
  | 'REVISAO_PLANO'
  | 'AULA_COMPATIVEL'
  | 'OFERTA_PLANO';
export type Situacao = 'AGUARDANDO_APROVACAO' | 'PENDENTE' | 'APROVADA' | 'ACEITA' | 'RECUSADA' | 'REJEITADA' | 'EXPIRADA';
export type DiaSemana = 'SEGUNDA' | 'TERCA' | 'QUARTA' | 'QUINTA' | 'SEXTA' | 'SABADO' | 'DOMINGO';

export const CATEGORIAS: Record<Categoria, { nome: string; icone: string; descricao: string }> = {
  ACOMPANHAMENTO_FISICO: {
    nome: 'Acompanhamento físico',
    icone: '♥',
    descricao: 'Lembretes de reavaliação física e do andamento das suas metas.',
  },
  FREQUENCIA_HORARIOS: {
    nome: 'Frequência e horários',
    icone: '◷',
    descricao: 'Avisos quando sua frequência cair e horários com menos movimento.',
  },
  AJUSTES_TREINO: { nome: 'Ajustes no treino', icone: '⚒', descricao: 'Mudanças no seu treino aprovadas pelo seu personal.' },
  AULAS_COLETIVAS: {
    nome: 'Aulas coletivas',
    icone: '☺',
    descricao: 'Aulas compatíveis com seu objetivo, nível e disponibilidade.',
  },
  PLANOS_SERVICOS: { nome: 'Planos e serviços', icone: '$', descricao: 'Ofertas de planos, pacotes e serviços da academia.' },
};

export const SITUACOES: Record<Situacao, { nome: string; classe: string }> = {
  AGUARDANDO_APROVACAO: { nome: 'Aguardando o personal', classe: 'selo selo-alerta' },
  PENDENTE: { nome: 'Pendente', classe: 'selo selo-alerta' },
  APROVADA: { nome: 'Aprovada', classe: 'selo selo-bom' },
  ACEITA: { nome: 'Aceita', classe: 'selo selo-bom' },
  RECUSADA: { nome: 'Recusada', classe: 'selo' },
  REJEITADA: { nome: 'Rejeitada', classe: 'selo selo-ruim' },
  EXPIRADA: { nome: 'Expirada', classe: 'selo' },
};

export const DIAS: { valor: DiaSemana; curto: string; plural: string }[] = [
  { valor: 'SEGUNDA', curto: 'Seg', plural: 'segundas' },
  { valor: 'TERCA', curto: 'Ter', plural: 'terças' },
  { valor: 'QUARTA', curto: 'Qua', plural: 'quartas' },
  { valor: 'QUINTA', curto: 'Qui', plural: 'quintas' },
  { valor: 'SEXTA', curto: 'Sex', plural: 'sextas' },
  { valor: 'SABADO', curto: 'Sáb', plural: 'sábados' },
  { valor: 'DOMINGO', curto: 'Dom', plural: 'domingos' },
];

export interface Recomendacao {
  identificador: string;
  aluno: string;
  alunoNome: string;
  categoria: Categoria;
  criterio: Criterio;
  titulo: string;
  descricao: string;
  justificativa: string;
  situacao: Situacao;
  geracao: string;
  validade: string;
  resposta: string | null;
  respondente: string | null;
  motivo: string | null;
  ajuste: string | null;
  personal: string | null;
  personalNome: string | null;
  planoTreino: string | null;
  planoTreinoNome: string | null;
  planoTreinoVersao: number | null;
  exercicioAtual: string | null;
  exercicioSugerido: string | null;
  aula: string | null;
  planoOfertado: string | null;
  planoGerado: string | null;
  planoGeradoVersao: number | null;
  ciencia: string | null;
}
export interface Taxa {
  categoria: Categoria;
  aceitas: number;
  respondidas: number;
  percentual: number;
  prioridadeReduzida: boolean;
  reduzidaDesde: string | null;
}
export interface ResumoAluno {
  aluno: string;
  nome: string;
  matriculaAtiva: boolean;
  plano: {
    identificador: string;
    nome: string;
    versao: number;
    personal: string;
    personalNome: string;
    frequenciaSemanal: number;
    dias: DiaSemana[];
  } | null;
}
export interface Visiveis {
  recomendacoes: Recomendacao[];
  taxas: Taxa[];
  aluno: ResumoAluno;
  maximoExibidas: number;
}
export interface Restricao {
  regiao: string;
  descricao: string;
  registro: string;
}
export interface Perfil {
  aluno: string;
  objetivo: string | null;
  nivel: string | null;
  dias: DiaSemana[];
  inicio: string | null;
  fim: string | null;
  restricoes: Restricao[];
  categoriasDesativadas: Categoria[];
  completo: boolean;
  pendentes: string[];
  atualizacao: string;
  resumo: ResumoAluno;
  objetivos: { valor: string; descricao: string }[];
  niveis: { valor: string; descricao: string }[];
  regioes: { valor: string; descricao: string }[];
  categorias: { categoria: Categoria; descricao: string; prioridade: number; opcional: boolean }[];
  maximoExibidas: number;
  carenciaRecusaDias: number;
}
export interface AutorizacoesRecomendacao extends IAgregadoAutorizacoes {
  gerar: boolean;
  responder: boolean;
  aprovar: boolean;
}
export interface SerieDetalhe {
  exercicio: string;
  exercicioNome: string;
  quantidade: number;
  repeticoesMinimas: number;
  repeticoesMaximas: number;
  carga: number;
  intervaloSegundos: number;
}
export interface Detalhe {
  recomendacao: Recomendacao;
  evidencias: { rotulo: string; valor: number; destaque: boolean }[];
  sugestao: {
    exercicioAtual: string;
    exercicioSugerido: string;
    series: number;
    repeticoesMinimas: number;
    repeticoesMaximas: number;
    carga: number;
    intervaloSegundos: number;
    treino: string | null;
    serieAtual: SerieDetalhe | null;
    faixa: {
      objetivo: string;
      seriesMinimas: number;
      seriesMaximas: number;
      repeticoesMinimas: number;
      repeticoesMaximas: number;
    } | null;
  } | null;
  alternativas: { identificador: string; nome: string }[];
  perfil: { objetivo: string | null; nivel: string | null; restricoes: Restricao[] };
  aluno: ResumoAluno;
  autorizacoes: AutorizacoesRecomendacao;
  carenciaRecusaDias: number;
}
export interface TreinoDetalhe {
  nome: string;
  dia: DiaSemana;
  objetivo: string;
  series: SerieDetalhe[];
}
export interface PlanoDetalhe {
  plano: { identificador: string; nome: string; versao: number; alunoNome: string; personalNome: string; criacao: string };
  planoAnterior: string | null;
  treinos: TreinoDetalhe[];
}
export interface Comparacao {
  recomendacao: Recomendacao;
  anterior: PlanoDetalhe;
  atual: PlanoDetalhe;
  versoes: {
    identificador: string;
    versao: number;
    criacao: string;
    situacao: string;
    execucoes: number;
    recomendacao: string | null;
    recomendacaoTitulo: string | null;
    aprovadaPor: string | null;
  }[];
}
export interface Aula {
  identificador: string;
  nome: string;
  modalidade: string;
  instrutor: string;
  sala: string;
  dia: DiaSemana;
  inicio: string;
  fim: string;
  capacidade: number;
  inscritos: number;
  vagas: number;
  ocupacao: number;
  nivel: string;
  impacto: string;
  baixoImpacto: boolean;
  inscrito: boolean;
  recomendacao: string | null;
  motivo: string | null;
}
export interface Aulas {
  perfilCompleto: boolean;
  pendentes: string[];
  recomendadas: Aula[];
  indisponiveis: Aula[];
  inscricoes: Aula[];
  todas: Aula[];
}
export interface Pesquisa<T> {
  resultado: { projecoes: T[]; total: number };
}

@Injectable({ providedIn: 'root' })
export class RecomendacoesService {
  private readonly http = inject(HttpClient);
  private readonly base = '/backend/core/recomendacao';
  private readonly perfil = '/backend/core/perfil-aluno';
  private readonly aulas = '/backend/core/aula-coletiva';

  gerar() {
    return this.http.post<{ geradas: number; aviso: string | null }>(`${this.base}/gerar`, null);
  }
  minhas() {
    return this.http.get<Visiveis>(`${this.base}/minhas`);
  }
  pesquisar(filtros: Record<string, string | number | boolean | null>) {
    return this.http.get<Pesquisa<Recomendacao>>(`${this.base}/pesquisa`, {
      params: parametros({ ordenacao: 'GERACAO', descendente: true, tamanhoPagina: 200, ...filtros }),
    });
  }
  taxas(aluno: string) {
    return this.http.get<Visiveis['taxas']>(`${this.base}/taxas`, { params: parametros({ aluno }) });
  }
  resumoDoAluno(aluno: string) {
    return this.http.get<ResumoAluno>(`${this.base}/aluno/${aluno}`);
  }
  detalhe(id: string) {
    return this.http.get<Detalhe>(`${this.base}/${id}/detalhe`);
  }
  comparacao(id: string) {
    return this.http.get<Comparacao>(`${this.base}/${id}/comparacao`);
  }
  aceitar(id: string) {
    return this.http.post<Recomendacao>(`${this.base}/${id}/aceitar`, null);
  }
  recusar(id: string, motivo: string | null) {
    return this.http.post<Recomendacao>(`${this.base}/${id}/recusar`, { motivo });
  }
  ciente(id: string) {
    return this.http.post<Recomendacao>(`${this.base}/${id}/ciente`, null);
  }
  aprovar(
    id: string,
    ajuste: {
      exercicioSugerido: string | null;
      series: number | null;
      repeticoesMinimas: number | null;
      repeticoesMaximas: number | null;
      carga: number | null;
      intervaloSegundos: number | null;
      observacao: string | null;
    },
  ) {
    return this.http.post<Recomendacao>(`${this.base}/${id}/aprovar`, ajuste);
  }
  rejeitar(id: string, motivo: string | null) {
    return this.http.post<Recomendacao>(`${this.base}/${id}/rejeitar`, { motivo });
  }

  meuPerfil() {
    return this.http.get<Perfil>(`${this.perfil}/meu`);
  }
  salvarDadosDeTreino(dados: { objetivo: string | null; nivel: string | null; dias: string[]; inicio: string | null; fim: string | null }) {
    return this.http.post<Perfil>(`${this.perfil}/dados-treino`, dados);
  }
  registrarRestricao(regiao: string, descricao: string) {
    return this.http.post<Perfil>(`${this.perfil}/restricoes`, { regiao, descricao });
  }
  removerRestricao(regiao: string) {
    return this.http.post<Perfil>(`${this.perfil}/restricoes/${regiao}/remover`, null);
  }
  salvarPreferencias(categoriasDesativadas: Categoria[]) {
    return this.http.post<Perfil>(`${this.perfil}/preferencias`, { categoriasDesativadas });
  }

  aulasParaMim() {
    return this.http.get<Aulas>(`${this.aulas}/para-voce`);
  }
  inscrever(aula: string) {
    return this.http.post<Aula>(`${this.aulas}/${aula}/inscrever`, null);
  }
}

function parametros(valores: Record<string, string | number | boolean | null>): HttpParams {
  let resultado = new HttpParams();
  for (const [chave, valor] of Object.entries(valores)) {
    if (valor !== null && valor !== '') resultado = resultado.set(chave, valor);
  }
  return resultado;
}

export const resolverPerfil: ResolveFn<Perfil> = () => inject(RecomendacoesService).meuPerfil();
export const resolverDetalhe: ResolveFn<Detalhe> = (rota) =>
  inject(RecomendacoesService).detalhe(rota.paramMap.get('id')!);
export const resolverComparacao: ResolveFn<Comparacao> = (rota) =>
  inject(RecomendacoesService).comparacao(rota.paramMap.get('id')!);

// ------------------------------------------------------------ formatação

export function data(valor: string | null | undefined): string {
  if (!valor) return '—';
  if (valor.length > 10) {
    return new Date(valor).toLocaleDateString('pt-BR', { timeZone: 'America/Sao_Paulo' });
  }
  const [ano, mes, dia] = valor.split('-');
  return `${dia}/${mes}/${ano}`;
}
export function hora(valor: string | null | undefined): string {
  if (!valor) return '—';
  const [h, m] = valor.split(':');
  return m === '00' ? `${Number(h)}h` : `${Number(h)}h${m}`;
}
export function diaCurto(dia: DiaSemana): string {
  return DIAS.find((item) => item.valor === dia)?.curto ?? dia;
}
export function hojeIso(): string {
  const agora = new Date(new Date().toLocaleString('en-US', { timeZone: 'America/Sao_Paulo' }));
  return `${agora.getFullYear()}-${String(agora.getMonth() + 1).padStart(2, '0')}-${String(agora.getDate()).padStart(2, '0')}`;
}
export function numero(valor: number | null | undefined): string {
  return valor === null || valor === undefined ? '—' : valor.toLocaleString('pt-BR', { maximumFractionDigits: 2 });
}
