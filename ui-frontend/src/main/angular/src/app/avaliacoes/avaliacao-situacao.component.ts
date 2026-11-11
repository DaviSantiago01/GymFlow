import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { data, numero } from '../formatacao';
import {
  ROTULOS_CLASSIFICACAO,
  ROTULOS_INDICADOR,
  ROTULOS_INTERPRETACAO,
  ROTULOS_SITUACAO_META,
  ROTULOS_STATUS,
  ROTULOS_TENDENCIA,
  SituacaoAtual,
} from './avaliacoes.service';

@Component({
  selector: 'app-avaliacao-situacao',
  imports: [RouterLink],
  templateUrl: './avaliacao-situacao.component.html',
  styleUrl: '../telas.scss',
})
export class AvaliacaoSituacaoComponent {
  private readonly rota = inject(ActivatedRoute);
  readonly rotulosClassificacao = ROTULOS_CLASSIFICACAO;
  readonly rotulosIndicador = ROTULOS_INDICADOR;
  readonly rotulosTendencia = ROTULOS_TENDENCIA;
  readonly rotulosInterpretacao = ROTULOS_INTERPRETACAO;
  readonly rotulosSituacaoMeta = ROTULOS_SITUACAO_META;
  readonly rotulosStatus = ROTULOS_STATUS;
  readonly numero = numero;
  readonly data = data;
  readonly situacao = signal<SituacaoAtual>(this.rota.snapshot.data['tela']);
  readonly aluno = signal<string | null>(null);

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.situacao.set(dados['tela']));
    this.rota.queryParamMap
      .pipe(takeUntilDestroyed())
      .subscribe((parametros) => this.aluno.set(parametros.get('aluno')));
  }
}
