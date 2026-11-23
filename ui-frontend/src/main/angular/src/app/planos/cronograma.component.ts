import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { data } from '../formatacao';
import { Cronograma, ROTULOS_SITUACAO_PARCELA, moeda } from './planos.service';

/** Parcelas de uma adesão: pagas, em aberto, em atraso e, com recorrência, as que ainda vão vencer. */
@Component({
  selector: 'app-cronograma',
  imports: [RouterLink],
  templateUrl: './cronograma.component.html',
  styleUrl: '../telas.scss',
})
export class CronogramaComponent {
  readonly cronograma = input.required<Cronograma>();
  readonly detalharMensalidades = input(false);
  readonly rotulos = ROTULOS_SITUACAO_PARCELA;
  readonly moeda = moeda;
  readonly data = data;
}
