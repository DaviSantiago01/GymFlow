import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { data, mensagemDeErro, numero } from '../formatacao';
import {
  AvaliacoesService,
  DetalheAvaliacao,
  ROTULOS_CLASSIFICACAO,
  ROTULOS_INDICADOR,
  ROTULOS_SITUACAO_AVALIACAO,
} from './avaliacoes.service';

@Component({
  selector: 'app-avaliacao-resultado',
  imports: [RouterLink],
  templateUrl: './avaliacao-resultado.component.html',
  styleUrl: '../telas.scss',
})
export class AvaliacaoResultadoComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(AvaliacoesService);
  private readonly destroy = inject(DestroyRef);
  readonly rotulosIndicador = ROTULOS_INDICADOR;
  readonly rotulosClassificacao = ROTULOS_CLASSIFICACAO;
  readonly rotulosSituacao = ROTULOS_SITUACAO_AVALIACAO;
  readonly numero = numero;
  readonly data = data;
  readonly detalhe = signal<DetalheAvaliacao>(this.rota.snapshot.data['tela']);
  readonly avaliacao = computed(() => this.detalhe().avaliacao);
  readonly valida = computed(() => this.avaliacao().situacao === 'VALIDA');
  readonly ocupado = signal(false);
  readonly erro = signal('');

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => {
      this.detalhe.set(dados['tela']);
      this.erro.set('');
    });
  }

  remover() {
    if (this.ocupado() || !window.confirm('Excluir esta avaliação física?')) return;
    this.ocupado.set(true);
    this.erro.set('');
    this.api
      .remover(this.avaliacao().identificador)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: () =>
          this.router.navigate(['/core/avaliacao-fisica/pesquisa'], {
            queryParams: { aluno: this.avaliacao().aluno },
          }),
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível excluir a avaliação.')),
      });
  }
}
