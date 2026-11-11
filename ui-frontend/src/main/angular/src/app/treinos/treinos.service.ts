import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ResolveFn } from '@angular/router';
import { map } from 'rxjs';
import { IAgregadoAutorizacoes } from '../../d5';

/** Autorizações da execução de exercício, com a ação adicional registrar. */
export interface AutorizacoesExecucao extends IAgregadoAutorizacoes {
  registrar: boolean;
}

export type DiaSemana =
  | 'SEGUNDA'
  | 'TERCA'
  | 'QUARTA'
  | 'QUINTA'
  | 'SEXTA'
  | 'SABADO'
  | 'DOMINGO';
export type Objetivo = 'FORCA' | 'HIPERTROFIA' | 'RESISTENCIA';
export type SituacaoPlano = 'RASCUNHO' | 'ATIVO' | 'ENCERRADO' | 'SUBSTITUIDO';

export const ROTULOS_DIA: Record<DiaSemana, string> = {
  SEGUNDA: 'Segunda',
  TERCA: 'Terça',
  QUARTA: 'Quarta',
  QUINTA: 'Quinta',
  SEXTA: 'Sexta',
  SABADO: 'Sábado',
  DOMINGO: 'Domingo',
};
export const ROTULOS_OBJETIVO: Record<Objetivo, string> = {
  FORCA: 'Força',
  HIPERTROFIA: 'Hipertrofia',
  RESISTENCIA: 'Resistência',
};
export const ROTULOS_SITUACAO: Record<SituacaoPlano, string> = {
  RASCUNHO: 'Rascunho',
  ATIVO: 'Ativo',
  ENCERRADO: 'Encerrado',
  SUBSTITUIDO: 'Substituído',
};

export interface Pesquisa<T> {
  resultado: { projecoes: T[]; total: number };
}
export interface Aluno {
  identificador: string;
  usuario: string;
  nome: string;
}
export interface Exercicio {
  identificador: string;
  nome: string;
  categoria: string;
  cargaMaxima: number;
  incrementoCarga: number | null;
}
export interface FaixaObjetivo {
  objetivo: Objetivo;
  seriesMinimas: number;
  seriesMaximas: number;
  repeticoesMinimas: number;
  repeticoesMaximas: number;
}
export interface FormularioPlano {
  alunos: Aluno[];
  exercicios: Exercicio[];
  objetivos: FaixaObjetivo[];
  dias: DiaSemana[];
  intervaloMinimoSegundos: number;
  intervaloMaximoSegundos: number;
}
export interface SerieEnvio {
  exercicio: string;
  quantidade: number;
  repeticoesMinimas: number;
  repeticoesMaximas: number;
  carga: number;
  intervaloSegundos: number;
}
export interface TreinoEnvio {
  nome: string;
  dia: DiaSemana;
  objetivo: Objetivo;
  series: SerieEnvio[];
}
export interface PlanoEnvio {
  nome: string;
  frequenciaSemanal: number;
  treinos: TreinoEnvio[];
}
export interface Plano {
  identificador: string;
  aluno: string;
  alunoNome: string;
  personal: string;
  personalNome: string;
  nome: string;
  frequenciaSemanal: number;
  situacao: SituacaoPlano;
  versao: number;
  criacao: string;
  encerramento: string | null;
}
export interface SerieDetalhe extends SerieEnvio {
  exercicioNome: string;
}
export interface TreinoDetalhe {
  nome: string;
  dia: DiaSemana;
  objetivo: Objetivo;
  series: SerieDetalhe[];
}
export interface DetalhePlano {
  plano: Plano;
  gerenciavel: boolean;
  planoAnterior: string | null;
  treinos: TreinoDetalhe[];
}
export interface PlanoAgregado extends PlanoEnvio {
  identificador: string;
  aluno: string;
  situacao: SituacaoPlano;
  versao: number;
}
export interface TelaFormularioPlano {
  formulario: FormularioPlano;
  plano: PlanoAgregado | null;
}
export interface Execucao {
  identificador: string;
  aluno: string;
  alunoNome: string;
  plano: string;
  planoNome: string;
  dia: DiaSemana;
  exercicio: string;
  exercicioNome: string;
  data: string;
  series: number;
  repeticoes: number;
  carga: number;
}
export interface Sugestao {
  cargaAtual: number;
  cargaSugerida: number;
  progressaoMaximaAtingida: boolean;
}
export interface ExecucaoRegistrada {
  execucao: Execucao;
  sugestao: Sugestao | null;
}
export interface ExecucaoEnvio {
  plano: string;
  dia: DiaSemana;
  exercicio: string;
  series: number;
  repeticoes: number;
  carga: number;
}
export interface Evolucao {
  aluno: string;
  exercicio: string | null;
  exercicios: { identificador: string; nome: string }[];
  execucoes: Execucao[];
  maiorCarga: number | null;
  sugestao: Sugestao | null;
}

function parametros(valores: Record<string, string | number | boolean | null>): HttpParams {
  let resultado = new HttpParams();
  for (const [chave, valor] of Object.entries(valores)) {
    if (valor !== null) resultado = resultado.set(chave, valor);
  }
  return resultado;
}

@Injectable({ providedIn: 'root' })
export class TreinosService {
  private readonly http = inject(HttpClient);
  private readonly planos = '/backend/core/plano-de-treino';
  private readonly execucoes = '/backend/core/execucao-de-exercicio';

  listarAlunos() {
    return this.http.get<Pesquisa<Aluno>>('/backend/core/aluno/pesquisa', {
      params: parametros({ ordenacao: 'NOME', tamanhoPagina: 1000 }),
    });
  }
  listarPlanos(aluno: string | null) {
    return this.http.get<Pesquisa<Plano>>(`${this.planos}/pesquisa`, {
      params: parametros({ ordenacao: 'CRIACAO', descendente: true, tamanhoPagina: 1000, aluno }),
    });
  }
  formulario() {
    return this.http.get<FormularioPlano>(`${this.planos}/inclusao`);
  }
  alteracao(id: string) {
    return this.http.get<{ agregado: PlanoAgregado; formulario: FormularioPlano }>(
      `${this.planos}/${id}/alteracao`,
    );
  }
  visualizar(id: string) {
    return this.http.get<DetalhePlano>(`${this.planos}/${id}/visualizacao`);
  }
  criar(aluno: string, plano: PlanoEnvio) {
    return this.http.post<DetalhePlano>(`${this.planos}/criar`, { aluno, ...plano });
  }
  revisar(id: string, plano: PlanoEnvio) {
    return this.http.post<DetalhePlano>(`${this.planos}/${id}/revisar`, plano);
  }
  executarAcao(id: string, acao: 'ativar' | 'substituir' | 'encerrar') {
    return this.http.post<DetalhePlano>(`${this.planos}/${id}/${acao}`, null);
  }
  planoAtivo() {
    return this.http.get<DetalhePlano>(`${this.execucoes}/inclusao`);
  }
  registrarExecucao(execucao: ExecucaoEnvio) {
    return this.http.post<ExecucaoRegistrada>(`${this.execucoes}/registrar`, execucao);
  }
  listarExecucoes(aluno: string | null) {
    return this.http.get<Pesquisa<Execucao>>(`${this.execucoes}/pesquisa`, {
      params: parametros({ ordenacao: 'DATA', descendente: true, tamanhoPagina: 1000, aluno }),
    });
  }
  evolucao(aluno: string | null, exercicio: string | null) {
    return this.http.get<Evolucao>(`${this.execucoes}/evolucao`, {
      params: parametros({ aluno, exercicio }),
    });
  }
}

export const resolverAlunos: ResolveFn<Pesquisa<Aluno>> = () => inject(TreinosService).listarAlunos();

export const resolverPlanos: ResolveFn<Pesquisa<Plano>> = (rota) =>
  inject(TreinosService).listarPlanos(rota.queryParamMap.get('aluno'));

export const resolverInclusaoPlano: ResolveFn<TelaFormularioPlano> = () =>
  inject(TreinosService)
    .formulario()
    .pipe(map((formulario) => ({ formulario, plano: null })));

export const resolverAlteracaoPlano: ResolveFn<TelaFormularioPlano> = (rota) =>
  inject(TreinosService)
    .alteracao(rota.paramMap.get('id')!)
    .pipe(map((recurso) => ({ formulario: recurso.formulario, plano: recurso.agregado })));

export const resolverVisualizacaoPlano: ResolveFn<DetalhePlano> = (rota) =>
  inject(TreinosService).visualizar(rota.paramMap.get('id')!);

export const resolverInclusaoExecucao: ResolveFn<DetalhePlano> = () =>
  inject(TreinosService).planoAtivo();

export const resolverExecucoes: ResolveFn<Pesquisa<Execucao>> = (rota) =>
  inject(TreinosService).listarExecucoes(rota.queryParamMap.get('aluno'));

export const resolverEvolucao: ResolveFn<Evolucao> = (rota) =>
  inject(TreinosService).evolucao(
    rota.queryParamMap.get('aluno'),
    rota.queryParamMap.get('exercicio'),
  );
