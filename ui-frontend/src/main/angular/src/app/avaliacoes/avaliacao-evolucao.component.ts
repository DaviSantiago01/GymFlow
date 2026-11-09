import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { data, numero } from '../formatacao';
import {
  EvolucaoIndicador,
  ROTULOS_INDICADOR,
  ROTULOS_INTERPRETACAO,
  ROTULOS_TENDENCIA,
} from './avaliacoes.service';

@Component({
  selector: 'app-avaliacao-evolucao',
  imports: [RouterLink],
  templateUrl: './avaliacao-evolucao.component.html',
  styleUrl: '../telas.scss',
})
export class AvaliacaoEvolucaoComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly rotulosIndicador = ROTULOS_INDICADOR;
  readonly rotulosTendencia = ROTULOS_TENDENCIA;
  readonly rotulosInterpretacao = ROTULOS_INTERPRETACAO;
  readonly numero = numero;
  readonly data = data;
  readonly evolucao = signal<EvolucaoIndicador>(this.rota.snapshot.data['tela']);
  readonly aluno = signal<string | null>(null);
  readonly maiorValor = computed(() => {
    let maior = 0;
    for (const ponto of this.evolucao().pontos) {
      if (ponto.valor > maior) maior = ponto.valor;
    }
    return maior;
  });

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.evolucao.set(dados['tela']));
    this.rota.queryParamMap
      .pipe(takeUntilDestroyed())
      .subscribe((parametros) => this.aluno.set(parametros.get('aluno')));
  }

  altura(valor: number) {
    return this.maiorValor() === 0 ? 0 : (valor / this.maiorValor()) * 85;
  }

  escolherIndicador(indicador: string) {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { indicador },
      queryParamsHandling: 'merge',
    });
  }
}
