import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ResolveFn } from '@angular/router';
import { forkJoin, map, Observable } from 'rxjs';

export interface Armario {
  identificador: string;
  localizacao: string;
  descricao: string;
  disponivel: boolean;
}
export interface Reserva {
  identificador: string;
  alunoNome: string;
  armario: string;
  localizacao: string;
  confirmacao: string;
  inicio: string;
  termino: string;
  encerramento: string | null;
  situacao: 'ATIVA' | 'CANCELADA' | 'ENCERRADA' | 'VENCIDA';
}
export interface Horario {
  hora: number;
  inicio: string;
  termino: string;
  disponivel: boolean;
  duracoesDisponiveis: number[];
}
export interface Agenda {
  data: string;
  hoje: string;
  fuso: string;
  horarios: Horario[];
}
export interface Temporal<T> {
  dados: T;
  horarioServidor: string;
}
interface Pesquisa<T> {
  resultado: { projecoes: T[]; total: number };
}
export type Modo = 'lista' | 'detalhe' | 'confirmacao' | 'ativa' | 'historico' | 'reserva';
export interface DadosTela {
  armarios?: Armario[];
  armario?: Armario;
  agenda?: Agenda;
  reserva: Reserva | null;
  historico?: Reserva[];
  total?: number;
  horarioServidor: string;
  recebidoEm: number;
}

@Injectable({ providedIn: 'root' })
export class ArmariosService {
  private readonly http = inject(HttpClient);
  private readonly base = '/backend/core';
  carregar(modo: Modo, id = '', pagina = 0, data = ''): Observable<DadosTela> {
    const ativa = this.http.get<Temporal<Reserva | null>>(`${this.base}/reserva-armario/ativa`);
    if (modo === 'lista')
      return forkJoin({
        lista: this.http.get<Pesquisa<Armario>>(`${this.base}/armario/pesquisa?tamanhoPagina=1000`),
        ativa,
      }).pipe(
        map(({ lista, ativa }) => ({
          armarios: lista.resultado.projecoes,
          reserva: ativa.dados,
          horarioServidor: ativa.horarioServidor,
          recebidoEm: performance.now(),
        })),
      );
    if (modo === 'detalhe')
      return forkJoin({
        detalhe: this.http.get<Temporal<Armario>>(`${this.base}/armario/${id}/visualizacao`),
        agenda: this.http.get<Temporal<Agenda>>(`${this.base}/armario/${id}/horarios`, {
          params: data ? { data } : {},
        }),
        ativa,
      }).pipe(
        map(({ detalhe, ativa, agenda }) => ({
          agenda: agenda.dados,
          armario: detalhe.dados,
          reserva: ativa.dados,
          horarioServidor: ativa.horarioServidor,
          recebidoEm: performance.now(),
        })),
      );
    if (modo === 'historico')
      return forkJoin({
        lista: this.http.get<Pesquisa<Reserva>>(
          `${this.base}/reserva-armario/pesquisa?indicePagina=${pagina}&tamanhoPagina=10`,
        ),
        ativa,
      }).pipe(
        map(({ lista, ativa }) => ({
          historico: lista.resultado.projecoes,
          total: lista.resultado.total,
          reserva: ativa.dados,
          horarioServidor: ativa.horarioServidor,
          recebidoEm: performance.now(),
        })),
      );
    const consulta =
      modo === 'ativa'
        ? ativa
        : this.http.get<Temporal<Reserva>>(
            `${this.base}/reserva-armario/${id}/${modo === 'confirmacao' ? 'confirmacao' : 'visualizacao'}`,
          );
    return consulta.pipe(
      map((resposta) => ({
        reserva: resposta.dados,
        horarioServidor: resposta.horarioServidor,
        recebidoEm: performance.now(),
      })),
    );
  }
  reservar(codigo: string, data: string, hora: number, duracaoHoras: number) {
    return this.http.post<Temporal<Reserva>>(`${this.base}/armario/${codigo}/reservar`, {
      data,
      hora,
      duracaoHoras,
    });
  }
  finalizar(id: string, acao: 'cancelar' | 'encerrar') {
    return this.http.post<Temporal<Reserva>>(`${this.base}/reserva-armario/${id}/${acao}`, null);
  }
}
export const resolverArmarios: ResolveFn<DadosTela> = (rota) =>
  inject(ArmariosService).carregar(
    rota.data['modo'],
    rota.paramMap.get('id') ?? '',
    Number(rota.queryParamMap.get('pagina') ?? 0),
  );
