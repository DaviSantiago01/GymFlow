import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { data } from '../formatacao';
import { CronogramaComponent } from './cronograma.component';
import {
  MeuPlano,
  ROTULOS_SITUACAO_MENSALIDADE,
  duracao,
  moeda,
  recorrencia,
} from './planos.service';

/**
 * Consulta do aluno: o plano ativo, as parcelas dele (inclusive as que ainda vão vencer, quando há recorrência) e o
 * histórico de todas as mensalidades, sem ações de gerenciamento.
 */
@Component({
  selector: 'app-meu-plano',
  imports: [CronogramaComponent],
  templateUrl: './meu-plano.component.html',
  styleUrl: '../telas.scss',
})
export class MeuPlanoComponent {
  private readonly rota = inject(ActivatedRoute);
  readonly rotulos = ROTULOS_SITUACAO_MENSALIDADE;
  readonly moeda = moeda;
  readonly duracao = duracao;
  readonly recorrencia = recorrencia;
  readonly data = data;
  readonly tela: MeuPlano = this.rota.snapshot.data['tela'];
  readonly proxima = this.tela.cronograma?.parcelas.find((parcela) => parcela.situacao !== 'PAGA') ?? null;
}
