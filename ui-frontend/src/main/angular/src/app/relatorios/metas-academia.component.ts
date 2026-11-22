import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, forkJoin } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import {
  Acompanhamento,
  INDICADORES,
  Indicador,
  MetaAcademia,
  RelatoriosService,
  data,
  hojeIso,
  nomeMes,
  percentual,
  valorIndicador,
} from './relatorios.service';

@Component({
  selector: 'app-metas-academia',
  imports: [FormsModule, RouterLink],
  templateUrl: './metas-academia.component.html',
  styleUrls: ['../analises.scss', '../analises-graficos.scss'],
})
export class MetasAcademiaComponent {
  private readonly api = inject(RelatoriosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);

  readonly indicadores = INDICADORES;
  readonly lista = Object.keys(INDICADORES) as Indicador[];
  readonly data = data;
  readonly nomeMes = nomeMes;
  readonly percentual = percentual;
  readonly valorIndicador = valorIndicador;
  readonly mesAtual = hojeIso().substring(0, 7);
  readonly meses = [this.mesAtual, mesAnterior(this.mesAtual), mesAnterior(mesAnterior(this.mesAtual))];

  readonly acompanhamento = signal<Acompanhamento | null>(null);
  readonly metas = signal<MetaAcademia[]>([]);
  readonly ocupado = signal(false);
  readonly erro = signal<string | null>(null);
  readonly substituindo = signal<MetaAcademia | null>(null);
  readonly atingidas = computed(() => this.acompanhamento()?.metas.filter((meta) => meta.atingida).length ?? 0);

  mes = this.mesAtual;
  indicador: Indicador = 'NOVAS_MATRICULAS';
  valorDesejado: number | null = null;
  inicio = '';
  fim = '';

  constructor() {
    const proximo = mesSeguinte(this.mesAtual);
    this.inicio = `${proximo}-01`;
    this.fim = ultimoDia(proximo);
    this.carregar();
  }

  carregar() {
    forkJoin({ acompanhamento: this.api.acompanhamento(this.mes), metas: this.api.listarMetas() })
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: ({ acompanhamento, metas }) => {
          this.acompanhamento.set(acompanhamento);
          this.metas.set(metas.resultado.projecoes);
        },
        error: (falha) => this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível carregar as metas.')),
      });
  }

  sentido(indicador: Indicador): string {
    return INDICADORES[indicador].menorMelhor ? 'Menor é melhor' : 'Maior é melhor';
  }

  editar(meta: MetaAcademia) {
    this.substituindo.set(meta);
    this.indicador = meta.indicador;
    this.valorDesejado = meta.valorDesejado;
    this.inicio = meta.inicio;
    this.fim = meta.fim;
    this.erro.set(null);
  }

  cancelarEdicao() {
    this.substituindo.set(null);
    this.valorDesejado = null;
  }

  /** RN015: o backend recusa uma segunda meta ativa para o mesmo indicador em período sobreposto. */
  salvar() {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set(null);
    const atual = this.substituindo();
    const pedido = atual
      ? this.api.substituirMeta(atual.identificador, { valorDesejado: this.valorDesejado, inicio: this.inicio, fim: this.fim })
      : this.api.cadastrarMeta({ indicador: this.indicador, valorDesejado: this.valorDesejado, inicio: this.inicio, fim: this.fim });
    pedido
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: () => {
          this.notificacoes.publicar('sucesso', atual ? 'Meta substituída.' : 'Meta cadastrada.');
          this.substituindo.set(null);
          this.valorDesejado = null;
          this.carregar();
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível salvar a meta.')),
      });
  }

  encerrar(meta: MetaAcademia) {
    this.api
      .encerrarMeta(meta.identificador)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: () => {
          this.notificacoes.publicar('sucesso', 'Meta encerrada.');
          this.carregar();
        },
        error: (falha) => this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível encerrar a meta.')),
      });
  }
}

function mesAnterior(anoMes: string): string {
  const [ano, mes] = anoMes.split('-').map(Number);
  const data = new Date(ano, mes - 2, 1);
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`;
}
function mesSeguinte(anoMes: string): string {
  const [ano, mes] = anoMes.split('-').map(Number);
  const data = new Date(ano, mes, 1);
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`;
}
function ultimoDia(anoMes: string): string {
  const [ano, mes] = anoMes.split('-').map(Number);
  return `${anoMes}-${String(new Date(ano, mes, 0).getDate()).padStart(2, '0')}`;
}
