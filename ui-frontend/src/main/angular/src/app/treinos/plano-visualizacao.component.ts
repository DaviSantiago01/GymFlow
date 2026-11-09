import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoAutenticacao, IAgregadoAutorizacoes } from '../../d5';
import { dataHora, mensagemDeErro, numero } from '../formatacao';
import {
  DetalhePlano,
  ROTULOS_DIA,
  ROTULOS_OBJETIVO,
  ROTULOS_SITUACAO,
  TreinosService,
} from './treinos.service';

interface AutorizacoesExecucao extends IAgregadoAutorizacoes {
  registrar: boolean;
}

@Component({
  selector: 'app-plano-visualizacao',
  imports: [RouterLink],
  templateUrl: './plano-visualizacao.component.html',
  styleUrl: '../telas.scss',
})
export class PlanoVisualizacaoComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly api = inject(TreinosService);
  private readonly autenticacao = inject(ServicoAutenticacao);
  private readonly destroy = inject(DestroyRef);
  readonly rotulosDia = ROTULOS_DIA;
  readonly rotulosObjetivo = ROTULOS_OBJETIVO;
  readonly rotulosSituacao = ROTULOS_SITUACAO;
  readonly numero = numero;
  readonly dataHora = dataHora;
  readonly detalhe = signal<DetalhePlano>(this.rota.snapshot.data['tela']);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  readonly aviso = signal('');
  readonly plano = computed(() => this.detalhe().plano);
  readonly rascunho = computed(() => this.plano().situacao === 'RASCUNHO');
  readonly editavel = computed(
    () => this.detalhe().gerenciavel && (this.rascunho() || this.plano().situacao === 'ATIVO'),
  );
  readonly podeRegistrar = computed(
    () =>
      this.plano().situacao === 'ATIVO' &&
      (this.autenticacao.autorizacoesDe<AutorizacoesExecucao>('execucaoDeExercicio')?.registrar ??
        false),
  );

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => {
      this.detalhe.set(dados['tela']);
      this.erro.set('');
      this.aviso.set('');
    });
  }

  executar(acao: 'ativar' | 'substituir' | 'encerrar') {
    if (this.ocupado()) return;
    if (acao === 'encerrar' && !window.confirm('Encerrar este plano de treino?')) return;
    this.ocupado.set(true);
    this.erro.set('');
    this.aviso.set('');
    this.api
      .executarAcao(this.plano().identificador, acao)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (detalhe) => {
          this.detalhe.set(detalhe);
          this.aviso.set(`Plano ${this.rotulosSituacao[detalhe.plano.situacao].toLowerCase()}.`);
        },
        error: (falha) =>
          this.erro.set(mensagemDeErro(falha, 'Não foi possível concluir a ação. Tente novamente.')),
      });
  }
}
