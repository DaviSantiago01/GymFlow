import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ResolveFn } from '@angular/router';
import { Observable, forkJoin, map, of, catchError } from 'rxjs';
export interface Pagina<T> {
  itens: T[];
  total: number;
  pagina: number;
  tamanho: number;
}
export interface Grupo {
  identificador: string;
  nome: string;
  organizador: string;
  organizadorNome: string;
  academia: string;
  souOrganizador: boolean;
  membros: number;
  proximo: string | null;
  encerramento: string | null;
}
export interface AlunoOpcao {
  usuario: string;
  identificador: string;
  nome: string;
}
export interface Contexto {
  aluno: string;
  nome: string;
  academia: string;
  agora: string;
}
export interface Membro {
  identificador: string;
  aluno: string;
  nome: string;
  entrada: string;
  saida: string | null;
  organizador: boolean;
}
export interface ConviteEntrada {
  identificador: string;
  grupo: string;
  grupoNome: string;
  organizadorNome: string;
  aluno: string;
  alunoNome: string;
  situacao: string;
  envio: string;
  resposta: string | null;
}
export interface Presenca {
  identificador: string;
  aluno: string;
  nome: string;
  situacao: string;
  criacao: string;
  resposta: string | null;
  cancelamento: string | null;
  motivo: string | null;
}
export interface TreinoResumo {
  identificador: string;
  grupo: string;
  grupoNome: string;
  inicio: string;
  prazo: string;
  cancelamento: string | null;
  convidados: number;
  confirmados: number;
  pendentes: number;
  meuConvite: Presenca | null;
  souOrganizador: boolean;
  membro: boolean;
  agora: string;
}
export interface GrupoDetalhe {
  grupo: Grupo;
  membros: Membro[];
  pendentes: ConviteEntrada[];
  treinos: Pagina<TreinoResumo>;
  agora: string;
}
export interface TreinoDetalhe {
  treino: TreinoResumo;
  presencas: Presenca[];
}
export type Modo = 'lista' | 'criar' | 'grupo' | 'propor' | 'treino' | 'convites' | 'historico';
export interface Tela {
  grupos?: Grupo[];
  total?: number;
  entradas?: Pagina<ConviteEntrada>;
  treinos?: Pagina<TreinoResumo>;
  contexto?: Contexto;
  detalhe?: GrupoDetalhe;
  treino?: TreinoDetalhe;
  erro?: string;
}
export function mensagemErro(e: unknown) {
  return e instanceof HttpErrorResponse
    ? (e.error?.mensagem ?? 'Não foi possível concluir. Tente novamente.')
    : 'Não foi possível carregar os dados.';
}
export function validarNome(nome: string) {
  return nome.trim().length < 1 || nome.trim().length > 60
    ? 'Informe um nome de 1 a 60 caracteres.'
    : '';
}
export function instanteBrasil(data: string, hora: string) {
  return new Date(`${data}T${hora}:00-03:00`).toISOString();
}
export const ANTECEDENCIA_CONFIRMACAO_MS = 3_600_000;
/** A confirmação encerra automaticamente uma hora antes do início do treino. */
export function prazoDeConfirmacao(inicio: string) {
  const i = Date.parse(inicio);
  return Number.isFinite(i) ? new Date(i - ANTECEDENCIA_CONFIRMACAO_MS).toISOString() : '';
}
export function validarAgenda(inicio: string, agora: number) {
  const i = Date.parse(inicio);
  return !Number.isFinite(i) || i - ANTECEDENCIA_CONFIRMACAO_MS <= agora
    ? 'O treino deve começar daqui a mais de uma hora, pois a confirmação encerra uma hora antes.'
    : '';
}
export function podeResponder(t: TreinoResumo, agora: number) {
  return (
    t.membro &&
    !t.cancelamento &&
    t.meuConvite?.situacao === 'PENDENTE' &&
    agora < Date.parse(t.prazo)
  );
}
export function podeCancelarPresenca(t: TreinoResumo, agora: number) {
  return (
    t.membro &&
    !t.cancelamento &&
    t.meuConvite?.situacao === 'CONFIRMADO' &&
    agora <= Date.parse(t.inicio) - 3600000
  );
}
export function rotuloSituacao(s: string) {
  return (
    (
      {
        PENDENTE: 'Aguardando resposta',
        CONFIRMADO: 'Presença confirmada',
        ACEITO: 'Aceito',
        RECUSADO: 'Recusado',
        EXPIRADO: 'Prazo encerrado',
        CANCELADO: 'Cancelado',
      } as Record<string, string>
    )[s] ?? s
  );
}
@Injectable({ providedIn: 'root' })
export class GruposService {
  private readonly http = inject(HttpClient);
  readonly base = '/backend/core/grupo-treino';
  carregar(
    modo: Modo,
    id: string,
    treino: string,
    pagina: number,
    historico = false,
  ): Observable<Tela> {
    if (modo === 'lista')
      return forkJoin({
        lista: this.http.get<{ resultado: { projecoes: Grupo[]; total: number } }>(
          `${this.base}/pesquisa`,
          { params: { indicePagina: pagina, tamanhoPagina: 12 } },
        ),
        entradas: this.http.get<Pagina<ConviteEntrada>>(`${this.base}/convites`, {
          params: { pagina },
        }),
      }).pipe(
        map(({ lista, entradas }) => ({
          grupos: lista.resultado.projecoes,
          total: lista.resultado.total,
          entradas,
        })),
      );
    if (modo === 'criar')
      return this.http
        .get<Contexto>(`${this.base}/criacao`)
        .pipe(map((contexto) => ({ contexto })));
    if (modo === 'grupo' || modo === 'propor')
      return this.http
        .get<GrupoDetalhe>(`${this.base}/${id}/${modo === 'propor' ? 'agenda' : 'visualizacao'}`, {
          params: { pagina, historico },
        })
        .pipe(map((detalhe) => ({ detalhe })));
    if (modo === 'treino')
      return this.http
        .get<TreinoDetalhe>(`${this.base}/${id}/treinos/${treino}/visualizacao`)
        .pipe(map((treino) => ({ treino })));
    if (modo === 'historico')
      return forkJoin({
        treinos: this.http.get<Pagina<TreinoResumo>>(`${this.base}/historico`, {
          params: { pagina, historico: true },
        }),
        entradas: this.http.get<Pagina<ConviteEntrada>>(`${this.base}/convites`, {
          params: { pagina, historico: true },
        }),
      });
    return this.http
      .get<Pagina<TreinoResumo>>(`${this.base}/convites-treino`, { params: { pagina } })
      .pipe(map((treinos) => ({ treinos })));
  }
  alunos(busca: string, pagina: number, grupo = '') {
    return this.http.get<Pagina<AlunoOpcao>>(`${this.base}/alunos`, {
      params: { busca, pagina, ...(grupo ? { grupo } : {}) },
    });
  }
  criar(nome: string, convidados: string[]) {
    return this.http.post<{ identificador: string }>(`${this.base}/criar`, { nome, convidados });
  }
  comando(caminho: string, dados: unknown = {}) {
    return this.http.post<void>(`${this.base}/${caminho}`, dados);
  }
}
export const resolverGrupos: ResolveFn<Tela> = (rota) =>
  inject(GruposService)
    .carregar(
      rota.data['modo'],
      rota.paramMap.get('id') ?? '',
      rota.paramMap.get('treino') ?? '',
      Math.max(0, Number(rota.queryParamMap.get('pagina')) || 0),
      rota.queryParamMap.get('historico') === 'true',
    )
    .pipe(catchError((e) => of({ erro: mensagemErro(e) })));
