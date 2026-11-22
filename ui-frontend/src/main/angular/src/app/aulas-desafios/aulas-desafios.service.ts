import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

export interface Aula {
  identificador: number; titulo: string; modalidade: string; descricao: string | null; data: string;
  inicio: string; fim: string; limite: number; ocupacao: number; status: string; professor: number; professorNome: string;
  inscrito: boolean;
}
export interface Desafio {
  identificador: number; titulo: string; indicador: string; inicio: string; fim: string; status: string; participantes: number;
}
export interface Professor { identificador: number; nome: string }
export interface Avaliacao {
  identificador: number; desafio: number; aluno: number; alunoNome: string; profissional: number; profissionalNome: string;
  tipo: string; peso: number; cintura: number; gordura: number; registro: string; bloqueada: boolean;
}

@Injectable({ providedIn: 'root' })
export class AulasDesafiosService {
  private readonly http = inject(HttpClient);
  professores() { return this.http.get<Professor[]>('/backend/core/aulas-desafios/professores'); }
  alunos() { return this.http.get<{ identificador: number; nome: string }[]>('/backend/core/aulas-desafios/alunos'); }
  aulas(data?: string, modalidade?: string, busca?: string) {
    let params = new HttpParams();
    if (data) params = params.set('data', data);
    if (modalidade) params = params.set('modalidade', modalidade);
    if (busca) params = params.set('busca', busca);
    return this.http.get<Aula[]>('/backend/core/aulas-desafios/aulas', { params });
  }
  aula(id: number) { return this.http.get<Aula>(`/backend/core/aulas-desafios/aulas/${id}`); }
  salvarAula(dados: unknown, id?: number) {
    return id ? this.http.put<Aula>(`/backend/core/aulas-desafios/aulas/${id}`, dados) : this.http.post<Aula>('/backend/core/aulas-desafios/aulas', dados);
  }
  cancelarAula(id: number) { return this.http.post<Aula>(`/backend/core/aulas-desafios/aulas/${id}/cancelar`, {}); }
  inscrever(id: number) { return this.http.post('/backend/core/aulas-desafios/aulas/' + id + '/inscricao', {}); }
  minhasAulas() { return this.http.get<Record<string, unknown>[]>('/backend/core/aulas-desafios/meus-agendamentos'); }
  cancelarInscricao(id: number) { return this.http.post('/backend/core/aulas-desafios/inscricoes/' + id + '/cancelar', {}); }
  cancelarInscricaoGestao(id: number) { return this.http.post('/backend/core/aulas-desafios/inscricoes/' + id + '/cancelar/gestao', {}); }
  inscritos(id: number) { return this.http.get<Record<string, unknown>[]>('/backend/core/aulas-desafios/aulas/' + id + '/inscritos'); }
  notificacoes() { return this.http.get<Record<string, unknown>[]>('/backend/core/aulas-desafios/notificacoes'); }
  desafios() { return this.http.get<Desafio[]>('/backend/core/aulas-desafios/desafios'); }
  criarDesafio(dados: unknown) { return this.http.post<Desafio>('/backend/core/aulas-desafios/desafios', dados); }
  avaliacoes(id: number) { return this.http.get<Avaliacao[]>(`/backend/core/aulas-desafios/desafios/${id}/avaliacoes`); }
  salvarAvaliacao(dados: unknown) { return this.http.post<Avaliacao>('/backend/core/aulas-desafios/avaliacoes', dados); }
  editarAvaliacao(id: number, dados: unknown) { return this.http.put<Avaliacao>(`/backend/core/aulas-desafios/avaliacoes/${id}`, dados); }
  excluirAvaliacao(id: number) { return this.http.delete<void>(`/backend/core/aulas-desafios/avaliacoes/${id}`); }
  inscreverDesafio(dados: unknown) { return this.http.post('/backend/core/aulas-desafios/desafios/inscricao', dados); }
  evolucao(id: number) { return this.http.get<Record<string, unknown>[]>(`/backend/core/aulas-desafios/desafios/${id}/evolucao`); }
  encerrarDesafio(id: number) { return this.http.post(`/backend/core/aulas-desafios/desafios/${id}/encerrar`, {}); }
}
