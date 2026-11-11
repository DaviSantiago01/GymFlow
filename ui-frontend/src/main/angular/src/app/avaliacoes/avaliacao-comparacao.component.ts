import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { data, numero } from '../formatacao';
import { Comparacao, ROTULOS_INDICADOR, ROTULOS_INTERPRETACAO } from './avaliacoes.service';

@Component({
  selector: 'app-avaliacao-comparacao',
  imports: [RouterLink],
  templateUrl: './avaliacao-comparacao.component.html',
  styleUrl: '../telas.scss',
})
export class AvaliacaoComparacaoComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly rotulosIndicador = ROTULOS_INDICADOR;
  readonly rotulosInterpretacao = ROTULOS_INTERPRETACAO;
  readonly numero = numero;
  readonly data = data;
  readonly comparacao = signal<Comparacao>(this.rota.snapshot.data['tela']);

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.comparacao.set(dados['tela']));
  }

  escolherReferencia(referencia: string) {
    this.router.navigate([], { relativeTo: this.rota, queryParams: { referencia } });
  }
}
