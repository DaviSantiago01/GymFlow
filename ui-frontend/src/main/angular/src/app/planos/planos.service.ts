import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ResolveFn } from '@angular/router';
import { IAgregadoAutorizacoes } from '../../d5';

export type SituacaoPlano = 'ATIVO' | 'INATIVO';
export type SituacaoAdesao = 'ATIVA' | 'ENCERRADA' | 'CANCELADA' | 'CONCLUIDA';
export type SituacaoParcela = 'PAGA' | 'ABERTA' | 'ATRASADA' | 'PREVISTA';
export type SituacaoMensalidade = 'ABERTA' | 'ATRASADA' | 'PAGA';
export type SituacaoPagamento = 'CONFIRMADO' | 'ESTORNADO';
export type FormaPagamento = 'DINHEIRO' | 'PIX' | 'CARTAO_DEBITO' | 'CARTAO_CREDITO' | 'BOLETO';

export const ROTULOS_SITUACAO_PLANO: Record<SituacaoPlano, string> = {
  ATIVO: 'Ativo',
  INATIVO: 'Inativo',
};
export const ROTULOS_SITUACAO_ADESAO: Record<SituacaoAdesao, string> = {
  ATIVA: 'Ativa',
  ENCERRADA: 'Encerrada por troca',
  CANCELADA: 'Cancelada',
  CONCLUIDA: 'Concluída',
};
export const ROTULOS_SITUACAO_PARCELA: Record<SituacaoParcela, string> = {
  PAGA: 'Paga',
  ABERTA: 'Em aberto',
  ATRASADA: 'Em atraso',
  PREVISTA: 'A vencer',
};
export const ROTULOS_SITUACAO_MENSALIDADE: Record<SituacaoMensalidade, string> = {
  ABERTA: 'Em aberto',
  ATRASADA: 'Em atraso',
  PAGA: 'Paga',
};
export const ROTULOS_SITUACAO_PAGAMENTO: Record<SituacaoPagamento, string> = {
  CONFIRMADO: 'Confirmado',
  ESTORNADO: 'Estornado',
};
export const ROTULOS_FORMA_PAGAMENTO: Record<FormaPagamento, string> = {
  DINHEIRO: 'Dinheiro',
  PIX: 'Pix',
  CARTAO_DEBITO: 'Cartão de débito',
  CARTAO_CREDITO: 'Cartão de crédito',
  BOLETO: 'Boleto',
};
export const FORMAS_PAGAMENTO = Object.keys(ROTULOS_FORMA_PAGAMENTO) as FormaPagamento[];

export function moeda(valor: number | null | undefined): string {
  if (valor === null || valor === undefined) return '—';
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

export function duracao(meses: number): string {
  return meses === 1 ? '1 mês' : `${meses} meses`;
}

/** Descreve a recorrência da adesão: quantas cobranças e quantos meses no total, ou cobrança contínua. */
export function recorrencia(quantidade: number | null, periodicidadeMeses: number): string {
  if (quantidade === null) return 'Contínua, até o cancelamento';
  const cobrancas = quantidade === 1 ? '1 cobrança' : `${quantidade} cobranças`;
  return `${cobrancas} (${duracao(quantidade * periodicidadeMeses)})`;
}

export interface Pesquisa<T> {
  resultado: { projecoes: T[]; total: number };
}
export interface AlunoOpcao {
  identificador: string;
  usuario: string;
  nome: string;
}
export interface Plano {
  identificador: string;
  nome: string;
  valor: number;
  duracaoMeses: number;
  situacao: SituacaoPlano;
  adesoesAtivas: number;
}
export interface PlanoEnvio {
  nome: string;
  valor: number | null;
  duracaoMeses: number | null;
}
export interface Adesao {
  identificador: string;
  aluno: string;
  alunoNome: string;
  plano: string;
  planoNome: string;
  valor: number;
  periodicidadeMeses: number;
  inicio: string;
  encerramento: string | null;
  situacao: SituacaoAdesao;
  quantidadePeriodos: number | null;
  terminoPrevisto: string | null;
}
export interface Parcela {
  numero: number;
  mensalidade: string | null;
  periodoInicio: string;
  periodoFim: string;
  vencimento: string;
  valor: number;
  situacao: SituacaoParcela;
}
export interface Cronograma {
  totalParcelas: number | null;
  terminoPrevisto: string | null;
  parcelas: Parcela[];
  pagas: number;
  abertas: number;
  atrasadas: number;
  previstas: number;
  restantes: number;
  valorRestante: number;
}
export interface Mensalidade {
  identificador: string;
  adesao: string;
  aluno: string;
  alunoNome: string;
  planoNome: string;
  periodoInicio: string;
  periodoFim: string;
  vencimento: string;
  valor: number;
  situacao: SituacaoMensalidade;
  diasAtraso: number;
  multa: number;
  juros: number;
  valorDevido: number;
}
export interface Pagamento {
  identificador: number;
  valor: number;
  forma: FormaPagamento;
  registro: string;
  situacao: SituacaoPagamento;
  estorno: string | null;
  motivoEstorno: string | null;
}
export interface FormularioAdesao {
  alunos: AlunoOpcao[];
  planos: Plano[];
}
export interface TelaAdesoes {
  pesquisa: Pesquisa<Adesao>;
}
export interface VisualizacaoAdesao {
  adesao: Adesao;
  mensalidades: Mensalidade[];
  planos: Plano[];
  cronograma: Cronograma;
}
export interface DetalheMensalidade {
  mensalidade: Mensalidade;
  pagamentos: Pagamento[];
}
export interface MeuPlano {
  adesao: Adesao | null;
  mensalidades: Mensalidade[];
  hoje: string;
  cronograma: Cronograma | null;
}
export interface AutorizacoesPlano extends IAgregadoAutorizacoes {
  cadastrar: boolean;
  editar: boolean;
  inativar: boolean;
}
export interface AutorizacoesAdesao extends IAgregadoAutorizacoes {
  aderir: boolean;
  trocar: boolean;
  cancelar: boolean;
  gerarMensalidades: boolean;
}
export interface AutorizacoesMensalidade extends IAgregadoAutorizacoes {
  registrarPagamento: boolean;
  estornarPagamento: boolean;
}

function parametros(valores: Record<string, string | number | boolean | null>): HttpParams {
  let resultado = new HttpParams();
  for (const [chave, valor] of Object.entries(valores)) {
    if (valor !== null && valor !== '') resultado = resultado.set(chave, valor);
  }
  return resultado;
}

@Injectable({ providedIn: 'root' })
export class PlanosService {
  private readonly http = inject(HttpClient);
  private readonly planos = '/backend/core/plano';
  private readonly adesoes = '/backend/core/adesao-plano';
  private readonly mensalidades = '/backend/core/mensalidade';

  listarPlanos(nome: string | null, situacao: string | null) {
    return this.http.get<Pesquisa<Plano>>(`${this.planos}/pesquisa`, {
      params: parametros({ ordenacao: 'NOME', tamanhoPagina: 1000, nome, situacao }),
    });
  }
  visualizarPlano(id: string) {
    return this.http.get<Plano>(`${this.planos}/${id}/visualizacao`);
  }
  cadastrarPlano(plano: PlanoEnvio) {
    return this.http.post<Plano>(`${this.planos}/cadastrar`, plano);
  }
  editarPlano(id: string, plano: PlanoEnvio) {
    return this.http.post<Plano>(`${this.planos}/${id}/editar`, plano);
  }
  inativarPlano(id: string) {
    return this.http.post<Plano>(`${this.planos}/${id}/inativar`, null);
  }

  formularioAdesao() {
    return this.http.get<FormularioAdesao>(`${this.adesoes}/inclusao`);
  }
  listarAdesoes(aluno: string | null, situacao: string | null) {
    return this.http.get<Pesquisa<Adesao>>(`${this.adesoes}/pesquisa`, {
      params: parametros({ ordenacao: 'INICIO', descendente: true, tamanhoPagina: 1000, aluno, situacao }),
    });
  }
  visualizarAdesao(id: string) {
    return this.http.get<VisualizacaoAdesao>(`${this.adesoes}/${id}/visualizacao`);
  }
  meuPlano() {
    return this.http.get<MeuPlano>(`${this.adesoes}/meu-plano`);
  }
  aderir(aluno: string, plano: string, quantidadePeriodos: number | null = null) {
    return this.http.post<Adesao>(`${this.adesoes}/aderir`, { aluno, plano, quantidadePeriodos });
  }
  trocar(id: string, plano: string, quantidadePeriodos: number | null = null) {
    return this.http.post<Adesao>(`${this.adesoes}/${id}/trocar`, { plano, quantidadePeriodos });
  }
  cancelar(id: string) {
    return this.http.post<Adesao>(`${this.adesoes}/${id}/cancelar`, null);
  }

  listarMensalidades(aluno: string | null, situacao: string | null) {
    return this.http.get<Pesquisa<Mensalidade>>(`${this.mensalidades}/pesquisa`, {
      params: parametros({ ordenacao: 'VENCIMENTO', tamanhoPagina: 1000, aluno, situacao }),
    });
  }
  visualizarMensalidade(id: string) {
    return this.http.get<DetalheMensalidade>(`${this.mensalidades}/${id}/visualizacao`);
  }
  gerarMensalidades() {
    return this.http.post<{ geradas: number }>(`${this.mensalidades}/gerar`, null);
  }
  pagar(id: string, forma: FormaPagamento) {
    return this.http.post<DetalheMensalidade>(`${this.mensalidades}/${id}/pagar`, { forma });
  }
  estornar(id: string, pagamento: number, motivo: string) {
    return this.http.post<DetalheMensalidade>(
      `${this.mensalidades}/${id}/pagamentos/${pagamento}/estornar`,
      { motivo },
    );
  }
}

export const resolverPlanos: ResolveFn<Pesquisa<Plano>> = (rota) =>
  inject(PlanosService).listarPlanos(rota.queryParamMap.get('nome'), rota.queryParamMap.get('situacao'));

export const resolverPlano: ResolveFn<Plano | null> = (rota) => {
  const id = rota.paramMap.get('id');
  return id ? inject(PlanosService).visualizarPlano(id) : null;
};

export const resolverAdesoes: ResolveFn<Pesquisa<Adesao>> = (rota) =>
  inject(PlanosService).listarAdesoes(rota.queryParamMap.get('aluno'), rota.queryParamMap.get('situacao'));

export const resolverFormularioAdesao: ResolveFn<FormularioAdesao> = () =>
  inject(PlanosService).formularioAdesao();

export const resolverAdesao: ResolveFn<VisualizacaoAdesao> = (rota) =>
  inject(PlanosService).visualizarAdesao(rota.paramMap.get('id')!);

export const resolverMensalidades: ResolveFn<Pesquisa<Mensalidade>> = (rota) =>
  inject(PlanosService).listarMensalidades(
    rota.queryParamMap.get('aluno'),
    rota.queryParamMap.get('situacao'),
  );

export const resolverMensalidade: ResolveFn<DetalheMensalidade> = (rota) =>
  inject(PlanosService).visualizarMensalidade(rota.paramMap.get('id')!);

export const resolverMeuPlano: ResolveFn<MeuPlano> = () => inject(PlanosService).meuPlano();
