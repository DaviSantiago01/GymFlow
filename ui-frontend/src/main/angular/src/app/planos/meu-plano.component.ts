import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { data } from '../formatacao';
import {
  MeuPlano,
  ROTULOS_SITUACAO_MENSALIDADE,
  duracao,
  moeda,
} from './planos.service';

/** Consulta do aluno: o plano ativo e as próprias mensalidades, sem ações de gerenciamento. */
@Component({
  selector: 'app-meu-plano',
  templateUrl: './meu-plano.component.html',
  styleUrl: '../telas.scss',
})
export class MeuPlanoComponent {
  private readonly rota = inject(ActivatedRoute);
  readonly rotulos = ROTULOS_SITUACAO_MENSALIDADE;
  readonly moeda = moeda;
  readonly duracao = duracao;
  readonly data = data;
  readonly tela: MeuPlano = this.rota.snapshot.data['tela'];
  readonly pendentes = this.tela.mensalidades.filter((mensalidade) => mensalidade.situacao !== 'PAGA');
}
