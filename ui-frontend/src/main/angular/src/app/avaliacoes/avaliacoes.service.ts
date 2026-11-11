import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ResolveFn } from '@angular/router';
import { map } from 'rxjs';

export type Indicador =
  | 'PESO'
  | 'IMC'
  | 'CINTURA'
  | 'ABDOMEN'
  | 'QUADRIL'
  | 'PEITORAL'
  | 'BRACO'
  | 'COXA'
  | 'PANTURRILHA';
export type ClassificacaoImc =
  | 'ABAIXO_DO_PESO'
  | 'PESO_NORMAL'
  | 'SOBREPESO'
  | 'OBESIDADE_GRAU_I'
  | 'OBESIDADE_GRAU_II'
  | 'OBESIDADE_GRAU_III';
export type SituacaoAvaliacao = 'VALIDA' | 'SUBSTITUIDA' | 'EXCLUIDA';
export type SituacaoMeta = 'ATIVA' | 'ATINGIDA' | 'ENCERRADA' | 'SUBSTITUIDA';
export type StatusCumprimento = 'DISTANTE' | 'PROXIMA' | 'ATINGIDA';
export type Tendencia = 'AUMENTANDO' | 'DIMINUINDO' | 'ESTAVEL';
export type Interpretacao = 'MELHORA' | 'PIORA' | 'ESTAVEL' | 'DEPENDE_DO_OBJETIVO';

export const ROTULOS_INDICADOR: Record<Indicador, string> = {
  PESO: 'Peso',
  IMC: 'IMC',
  CINTURA: 'Cintura',
  ABDOMEN: 'Abdômen',
  QUADRIL: 'Quadril',
  PEITORAL: 'Peitoral',
  BRACO: 'Braço',
  COXA: 'Coxa',
  PANTURRILHA: 'Panturrilha',
};
export const ROTULOS_CLASSIFICACAO: Record<ClassificacaoImc, string> = {
  ABAIXO_DO_PESO: 'Abaixo do peso',
  PESO_NORMAL: 'Peso normal',
  SOBREPESO: 'Sobrepeso',
  OBESIDADE_GRAU_I: 'Obesidade grau I',
  OBESIDADE_GRAU_II: 'Obesidade grau II',
  OBESIDADE_GRAU_III: 'Obesidade grau III',
};
export const ROTULOS_SITUACAO_AVALIACAO: Record<SituacaoAvaliacao, string> = {
  VALIDA: 'Válida',
  SUBSTITUIDA: 'Substituída',
  EXCLUIDA: 'Excluída',
};
export const ROTULOS_SITUACAO_META: Record<SituacaoMeta, string> = {
  ATIVA: 'Ativa',
  ATINGIDA: 'Atingida',
  ENCERRADA: 'Encerrada',
  SUBSTITUIDA: 'Substituída',
};
export const ROTULOS_STATUS: Record<StatusCumprimento, string> = {
  DISTANTE: 'Distante',
  PROXIMA: 'Próxima',
  ATINGIDA: 'Atingida',
};
export const ROTULOS_TENDENCIA: Record<Tendencia, string> = {
  AUMENTANDO: 'Aumentando',
  DIMINUINDO: 'Diminuindo',
  ESTAVEL: 'Estável',
};
export const ROTULOS_INTERPRETACAO: Record<Interpretacao, string> = {
  MELHORA: 'Melhora',
  PIORA: 'Piora',
  ESTAVEL: 'Estável',
  DEPENDE_DO_OBJETIVO: 'Depende do objetivo',
};

interface Pesquisa<T> {
  resultado: { projecoes: T[]; total: number };
}
export interface AlunoResumo {
  identificador: string;
  nome: string;
}
export interface MedidaCorporal {
  indicador: Indicador;
  valor: number;
}
export interface FormularioAvaliacao {
  alunos: AlunoResumo[];
  medidas: Indicador[];
  pesoMinimo: number;
  pesoMaximo: number;
  alturaMinima: number;
  alturaMaxima: number;
  medidaMinima: number;
  medidaMaxima: number;
}
export interface AvaliacaoEnvio {
  data: string;
  peso: number;
  altura: number;
  medidas: MedidaCorporal[];
}
export interface AvaliacaoAgregado extends AvaliacaoEnvio {
  identificador: string;
  aluno: string;
}
export interface TelaFormularioAvaliacao {
  formulario: FormularioAvaliacao;
  avaliacao: AvaliacaoAgregado | null;
}
export interface Avaliacao {
  identificador: string;
  aluno: string;
  alunoNome: string;
  personalNome: string;
  data: string;
  peso: number;
  altura: number;
  imc: number;
  classificacaoImc: ClassificacaoImc;
  situacao: SituacaoAvaliacao;
  versao: number;
}
export interface DetalheAvaliacao {
  avaliacao: Avaliacao;
  medidas: MedidaCorporal[];
  referencia: string | null;
  versaoAnterior: string | null;
  gerenciavel: boolean;
}
export interface ComparacaoIndicador {
  indicador: Indicador;
  valorReferencia: number;
  valorAtual: number;
  diferencaAbsoluta: number;
  diferencaPercentual: number;
  evolucaoPercentual: number;
  interpretacao: Interpretacao;
}
export interface Comparacao {
  atual: Avaliacao;
  referencia: Avaliacao | null;
  indicadores: ComparacaoIndicador[];
  disponiveis: Avaliacao[];
}
export interface EvolucaoIndicador {
  aluno: string;
  indicador: Indicador;
  indicadores: Indicador[];
  pontos: { data: string; valor: number }[];
  tendencia: Tendencia | null;
  interpretacao: Interpretacao | null;
}
export interface Meta {
  identificador: string;
  aluno: string;
  alunoNome: string;
  indicador: Indicador;
  valorInicial: number;
  valorDesejado: number;
  inicio: string;
  fim: string;
  situacao: SituacaoMeta;
  dataAtingida: string | null;
  valorAtual: number | null;
  percentualCumprimento: number | null;
  statusCumprimento: StatusCumprimento | null;
}
export interface SituacaoAtual {
  aluno: string;
  avaliacao: Avaliacao | null;
  indicadores: {
    indicador: Indicador;
    valor: number;
    tendencia: Tendencia | null;
    interpretacao: Interpretacao | null;
  }[];
  metas: Meta[];
}
export interface FormularioMeta {
  alunos: AlunoResumo[];
  indicadores: Indicador[];
}
export interface MetaAgregado {
  identificador: string;
  aluno: string;
  indicador: Indicador;
  valorDesejado: number;
  inicio: string;
  fim: string;
  situacao: SituacaoMeta;
}
export interface TelaFormularioMeta {
  formulario: FormularioMeta;
  meta: MetaAgregado | null;
}
export interface MetaEnvio {
  valorDesejado: number;
  inicio: string;
  fim: string;
}

function parametros(valores: Record<string, string | number | boolean | null>): HttpParams {
  let resultado = new HttpParams();
  for (const [chave, valor] of Object.entries(valores)) {
    if (valor !== null) resultado = resultado.set(chave, valor);
  }
  return resultado;
}

@Injectable({ providedIn: 'root' })
export class AvaliacoesService {
  private readonly http = inject(HttpClient);
  private readonly avaliacoes = '/backend/core/avaliacao-fisica';
  private readonly metas = '/backend/core/meta';

  formulario() {
    return this.http.get<FormularioAvaliacao>(`${this.avaliacoes}/inclusao`);
  }
  alteracao(id: string) {
    return this.http.get<{ agregado: AvaliacaoAgregado; formulario: FormularioAvaliacao }>(
      `${this.avaliacoes}/${id}/alteracao`,
    );
  }
  registrar(aluno: string, avaliacao: AvaliacaoEnvio) {
    return this.http.post<DetalheAvaliacao>(`${this.avaliacoes}/registrar`, { aluno, ...avaliacao });
  }
  corrigir(id: string, avaliacao: AvaliacaoEnvio) {
    return this.http.post<DetalheAvaliacao>(`${this.avaliacoes}/${id}/corrigir`, avaliacao);
  }
  remover(id: string) {
    return this.http.post<void>(`${this.avaliacoes}/${id}/remover`, null);
  }
  visualizar(id: string) {
    return this.http.get<DetalheAvaliacao>(`${this.avaliacoes}/${id}/visualizacao`);
  }
  listar(aluno: string | null, situacao: string | null) {
    return this.http.get<Pesquisa<Avaliacao>>(`${this.avaliacoes}/pesquisa`, {
      params: parametros({ ordenacao: 'DATA', descendente: true, tamanhoPagina: 1000, aluno, situacao }),
    });
  }
  comparar(id: string, referencia: string | null) {
    return this.http.get<Comparacao>(`${this.avaliacoes}/${id}/comparacao`, {
      params: parametros({ referencia }),
    });
  }
  evolucao(aluno: string | null, indicador: string | null) {
    return this.http.get<EvolucaoIndicador>(`${this.avaliacoes}/evolucao`, {
      params: parametros({ aluno, indicador }),
    });
  }
  situacao(aluno: string | null) {
    return this.http.get<SituacaoAtual>(`${this.avaliacoes}/situacao`, {
      params: parametros({ aluno }),
    });
  }
  formularioMeta() {
    return this.http.get<FormularioMeta>(`${this.metas}/inclusao`);
  }
  alteracaoMeta(id: string) {
    return this.http.get<{ agregado: MetaAgregado; formulario: FormularioMeta }>(
      `${this.metas}/${id}/alteracao`,
    );
  }
  listarMetas(aluno: string | null) {
    return this.http.get<Pesquisa<Meta>>(`${this.metas}/pesquisa`, {
      params: parametros({ ordenacao: 'FIM', tamanhoPagina: 1000, aluno }),
    });
  }
  criarMeta(aluno: string, indicador: Indicador, meta: MetaEnvio) {
    return this.http.post<Meta>(`${this.metas}/criar`, { aluno, indicador, ...meta });
  }
  alterarMeta(id: string, acao: 'ajustar' | 'substituir', meta: MetaEnvio) {
    return this.http.post<Meta>(`${this.metas}/${id}/${acao}`, meta);
  }
  encerrarMeta(id: string) {
    return this.http.post<Meta>(`${this.metas}/${id}/encerrar`, null);
  }
}

export const resolverInclusaoAvaliacao: ResolveFn<TelaFormularioAvaliacao> = () =>
  inject(AvaliacoesService)
    .formulario()
    .pipe(map((formulario) => ({ formulario, avaliacao: null })));

export const resolverAlteracaoAvaliacao: ResolveFn<TelaFormularioAvaliacao> = (rota) =>
  inject(AvaliacoesService)
    .alteracao(rota.paramMap.get('id')!)
    .pipe(map((recurso) => ({ formulario: recurso.formulario, avaliacao: recurso.agregado })));

export const resolverVisualizacaoAvaliacao: ResolveFn<DetalheAvaliacao> = (rota) =>
  inject(AvaliacoesService).visualizar(rota.paramMap.get('id')!);

export const resolverAvaliacoes: ResolveFn<Pesquisa<Avaliacao>> = (rota) =>
  inject(AvaliacoesService).listar(
    rota.queryParamMap.get('aluno'),
    rota.queryParamMap.get('todas') ? null : 'VALIDA',
  );

export const resolverComparacao: ResolveFn<Comparacao> = (rota) =>
  inject(AvaliacoesService).comparar(
    rota.paramMap.get('id')!,
    rota.queryParamMap.get('referencia'),
  );

export const resolverEvolucaoIndicador: ResolveFn<EvolucaoIndicador> = (rota) =>
  inject(AvaliacoesService).evolucao(
    rota.queryParamMap.get('aluno'),
    rota.queryParamMap.get('indicador'),
  );

export const resolverSituacao: ResolveFn<SituacaoAtual> = (rota) =>
  inject(AvaliacoesService).situacao(rota.queryParamMap.get('aluno'));

export const resolverMetas: ResolveFn<Pesquisa<Meta>> = (rota) =>
  inject(AvaliacoesService).listarMetas(rota.queryParamMap.get('aluno'));

export const resolverInclusaoMeta: ResolveFn<TelaFormularioMeta> = () =>
  inject(AvaliacoesService)
    .formularioMeta()
    .pipe(map((formulario) => ({ formulario, meta: null })));

export const resolverAlteracaoMeta: ResolveFn<TelaFormularioMeta> = (rota) =>
  inject(AvaliacoesService)
    .alteracaoMeta(rota.paramMap.get('id')!)
    .pipe(map((recurso) => ({ formulario: recurso.formulario, meta: recurso.agregado })));
