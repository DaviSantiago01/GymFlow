import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ResolveFn } from '@angular/router';
import { forkJoin, map, Observable } from 'rxjs';

export const criterios = [
  { chave: 'auxilio', nome: 'Auxílio nos exercícios' },
  { chave: 'pontualidade', nome: 'Pontualidade' },
  { chave: 'postura', nome: 'Postura profissional e respeito' },
  { chave: 'didatica', nome: 'Didática e comunicação' },
  { chave: 'adequacao', nome: 'Adequação da dificuldade' },
] as const;
export type Criterio = (typeof criterios)[number]['chave'];
export type Notas = Record<Criterio, number>;
export type Escolhas = Record<Criterio, number | null>;
export interface Estatisticas extends Escolhas {
  quantidade: number;
  geral: number | null;
}
export interface Professor {
  identificador: string;
  nome: string;
  especialidade: string;
  biografia: string;
  demonstracao: boolean;
  estatisticas: Estatisticas;
}
export interface Avaliacao {
  identificador: string;
  professor: string;
  professorNome: string;
  alunoNome: string;
  notas: Notas;
  comentario: string | null;
  criacao: string;
  atualizacao: string;
  propria: boolean;
  treino: string | null;
  treinoInicio: string | null;
  demonstracao: boolean;
}
export interface Minha {
  avaliacao: Avaliacao | null;
  jaAvaliou: boolean;
}
export interface Perfil {
  professor: Professor;

  recentes: Avaliacao[];
}
interface Pesquisa<T> {
  resultado: { projecoes: T[]; total: number };
}
export interface Treino {
  identificador: string;
  professor: string;
  professorNome: string;
  descricao: string;
  inicio: string;
  termino: string;
  demonstracao: boolean;
  avaliacao: string | null;
  jaAvaliou: boolean;
}
export type ModoAvaliacao =
  'treinos' | 'minhas' | 'professores' | 'perfil' | 'nova' | 'editar' | 'minha';
export interface TelaAvaliacao {
  treinos?: Treino[];
  treino?: Treino;
  demonstracaoDisponivel?: boolean;
  professores?: Professor[];
  avaliacoes?: Avaliacao[];
  perfil?: Perfil;
  avaliacao?: Avaliacao;
  total?: number;
}
export interface EnvioAvaliacao extends Notas {
  comentario: string;
}
export const escolhasVazias = (): Escolhas => ({
  auxilio: null,
  pontualidade: null,
  postura: null,
  didatica: null,
  adequacao: null,
});
export function validarAvaliacao(notas: Escolhas, comentario: string): Record<string, string> {
  const erros: Record<string, string> = {};
  for (const c of criterios) {
    const nota = notas[c.chave];
    if (nota === null || !Number.isInteger(nota) || nota < 0 || nota > 5)
      erros[c.chave] = 'Escolha uma nota inteira de 0 a 5.';
  }
  if (comentario.length > 500) erros['comentario'] = 'Use no máximo 500 caracteres.';
  return erros;
}
@Injectable({ providedIn: 'root' })
export class AvaliacoesService {
  private readonly http = inject(HttpClient);
  private readonly base = '/backend/core';
  carregar(
    modo: ModoAvaliacao,
    id: string,
    pagina: number,
    busca: string,
  ): Observable<TelaAvaliacao> {
    if (modo === 'treinos')
      return forkJoin({
        lista: this.http.get<Pesquisa<Treino>>(`${this.base}/treino/pesquisa`, {
          params: { indicePagina: pagina, tamanhoPagina: 9 },
        }),
        demonstracao: this.http.get<{ disponivel: boolean }>(`${this.base}/treino/demonstracao`),
      }).pipe(
        map(({ lista, demonstracao }) => ({
          treinos: lista.resultado.projecoes,
          total: lista.resultado.total,
          demonstracaoDisponivel: demonstracao.disponivel,
        })),
      );
    if (modo === 'nova')
      return this.http
        .get<Treino>(`${this.base}/treino/${id}/avaliacao`)
        .pipe(map((treino) => ({ treino })));
    if (modo === 'professores')
      return this.http
        .get<Pesquisa<Professor>>(`${this.base}/professor/pesquisa`, {
          params: { indicePagina: pagina, tamanhoPagina: 9, busca },
        })
        .pipe(map((r) => ({ professores: r.resultado.projecoes, total: r.resultado.total })));
    if (modo === 'minhas')
      return this.http
        .get<Pesquisa<Avaliacao>>(`${this.base}/avaliacao-professor/pesquisa`, {
          params: { indicePagina: pagina, tamanhoPagina: 9 },
        })
        .pipe(map((r) => ({ avaliacoes: r.resultado.projecoes, total: r.resultado.total })));
    if (modo === 'perfil')
      return this.http
        .get<Perfil>(`${this.base}/professor/${id}/visualizacao`)
        .pipe(map((perfil) => ({ perfil })));
    return this.http
      .get<Avaliacao>(`${this.base}/avaliacao-professor/${id}/visualizacao`)
      .pipe(map((avaliacao) => ({ avaliacao })));
  }
  salvar(treino: string, id: string | undefined, dados: EnvioAvaliacao) {
    return this.http.post<Avaliacao>(
      id
        ? `${this.base}/avaliacao-professor/${id}/editar`
        : `${this.base}/treino/${treino}/avaliar`,
      dados,
    );
  }
  carregarDemonstracao() {
    return this.http.post<void>(`${this.base}/treino/demonstracao`, null);
  }
  excluir(id: string) {
    return this.http.post<void>(`${this.base}/avaliacao-professor/${id}/remover`, null);
  }
}
export const resolverAvaliacoes: ResolveFn<TelaAvaliacao> = (rota) =>
  inject(AvaliacoesService).carregar(
    rota.data['modo'],
    rota.paramMap.get('id') ?? '',
    Math.max(0, Number(rota.queryParamMap.get('pagina')) || 0),
    rota.queryParamMap.get('busca') ?? '',
  );
