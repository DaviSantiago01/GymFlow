import { HttpClient } from '@angular/common/http';
import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { data, mensagemDeErro } from '../formatacao';

export const AREAS = ['CARDIO', 'MUSCULACAO', 'FUNCIONAL'] as const;
export const SITUACOES = ['DISPONIVEL', 'INTERDITADO', 'EM_MANUTENCAO', 'INATIVO'] as const;
export const RESULTADOS = ['APROVADO', 'APROVADO_COM_OBSERVACAO', 'REPROVADO'] as const;
export const GRAVIDADES = ['BAIXA', 'MEDIA', 'ALTA', 'CRITICA'] as const;
export const SITUACOES_PROBLEMA = ['ABERTO', 'RESOLVIDO', 'CANCELADO'] as const;
export const SITUACOES_ORDEM = ['ABERTA', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'] as const;

const rotulos: Record<string, string> = {
  CARDIO: 'Cardio',
  MUSCULACAO: 'Musculação',
  FUNCIONAL: 'Funcional',
  DISPONIVEL: 'Disponível',
  INTERDITADO: 'Interditado',
  EM_MANUTENCAO: 'Em manutenção',
  INATIVO: 'Inativo',
  APROVADO: 'Aprovado',
  APROVADO_COM_OBSERVACAO: 'Aprovado com observação',
  REPROVADO: 'Reprovado',
  BAIXA: 'Baixa',
  MEDIA: 'Média',
  ALTA: 'Alta',
  CRITICA: 'Crítica',
  ABERTO: 'Em aberto',
  RESOLVIDO: 'Resolvido',
  CANCELADO: 'Cancelado',
  ABERTA: 'Aberta',
  EM_ANDAMENTO: 'Em andamento',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada',
  EM_DIA: 'Em dia',
  PROXIMA: 'Próxima do prazo',
  ATRASADA: 'Atrasada',
};

export function rotulo(valor: string | null | undefined): string {
  return valor ? (rotulos[valor] ?? valor) : '—';
}
export function dia(valor: string | null | undefined): string {
  return valor ? data(valor) : '—';
}
export function hoje(): string {
  return new Intl.DateTimeFormat('sv-SE', {
    timeZone: 'America/Sao_Paulo',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format();
}

export type Recurso =
  'equipamento' | 'registro-equipamento' | 'problema-equipamento' | 'ordem-manutencao';
export interface EquipamentoEnvio {
  patrimonio: string;
  serie: string | null;
  nome: string;
  tipo: string;
  marca: string;
  modelo: string;
  aquisicao: string;
  area: string;
  periodicidade: number | null;
}
export interface Equipamento extends EquipamentoEnvio {
  identificador: string;
  situacao: string;
  ultimaInspecao: string | null;
  proximaInspecao: string;
  prazoInspecao: string;
}
export interface Operacoes {
  equipamento: Equipamento;
  podeExcluir: boolean;
  hoje: string;
}
export interface Registro {
  identificador: string;
  equipamento: string;
  equipamentoNome: string;
  data: string;
  responsavel: string;
  areaAnterior: string;
  areaNova: string;
  situacaoAnterior: string;
  situacaoNova: string;
  resultado: string | null;
  observacao: string | null;
}
export interface ProblemaEnvio {
  titulo: string;
  relato: string;
  gravidade: string;
  risco: boolean;
  evidencia: string | null;
  dataRelato: string;
}
export interface Problema extends ProblemaEnvio {
  identificador: string;
  equipamento: string;
  equipamentoNome: string;
  situacao: string;
  responsavel: string;
  dataEncerramento: string | null;
  desfecho: string | null;
}
export interface OrdemEnvio {
  tecnico: string;
  diagnostico: string;
  custoEstimado: number | null;
}
export interface Ordem extends OrdemEnvio {
  identificador: string;
  equipamento: string;
  problema: string;
  problemaTitulo: string;
  abertura: string;
  responsavel: string;
  situacao: string;
  inicio: string | null;
  termino: string | null;
  custoReal: number | null;
  servicoExecutado: string | null;
  justificativa: string | null;
}

interface Pesquisa<T> {
  resultado: { projecoes: T[]; total: number };
}
export const TAMANHO_PAGINA = 15;

@Injectable({ providedIn: 'root' })
export class EquipamentosService {
  private readonly http = inject(HttpClient);

  pesquisar<T>(recurso: Recurso, filtros: Record<string, string | number | boolean>) {
    return this.http.get<Pesquisa<T>>('/backend/core/' + recurso + '/pesquisa', {
      params: { tamanhoPagina: TAMANHO_PAGINA, ...filtros },
    });
  }

  visualizar<T>(recurso: Recurso, id: string) {
    return this.http.get<T>(
      '/backend/core/' + recurso + '/' + encodeURIComponent(id) + '/visualizacao',
    );
  }

  operacoes(id: string) {
    return this.http.get<Operacoes>(
      '/backend/core/equipamento/' + encodeURIComponent(id) + '/operacoes',
    );
  }

  excluir(id: string) {
    return this.comando<void>('equipamento', 'excluir', {}, id);
  }

  comando<T>(recurso: Recurso, acao: string, corpo: object, id?: string) {
    const caminho = id ? encodeURIComponent(id) + '/' + acao : acao;
    return this.http.post<T>('/backend/core/' + recurso + '/' + caminho, corpo);
  }
}

export function usarAcaoEquipamento() {
  const destroy = inject(DestroyRef);
  const notificacoes = inject(ServicoNotificacao);
  const ocupado = signal(false);
  const erro = signal('');

  function executar<T>(
    requisicao: Observable<T>,
    mensagem: string,
    concluir: (resultado: T) => void,
  ) {
    if (ocupado()) return;
    ocupado.set(true);
    erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(destroy),
        finalize(() => ocupado.set(false)),
      )
      .subscribe({
        next: (resultado) => {
          notificacoes.publicar('sucesso', mensagem);
          concluir(resultado);
        },
        error: (falha) => erro.set(mensagemDeErro(falha, 'Não foi possível concluir a operação.')),
      });
  }

  return { ocupado, erro, executar };
}
