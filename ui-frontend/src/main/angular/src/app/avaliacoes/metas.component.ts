import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ServicoAutenticacao, IAgregadoAutorizacoes } from '../../d5';
import { data, numero } from '../formatacao';
import {
  Meta,
  ROTULOS_INDICADOR,
  ROTULOS_SITUACAO_META,
  ROTULOS_STATUS,
} from './avaliacoes.service';

interface AutorizacoesMeta extends IAgregadoAutorizacoes {
  criar: boolean;
  ajustar: boolean;
}

@Component({
  selector: 'app-metas',
  imports: [RouterLink],
  templateUrl: './metas.component.html',
  styleUrl: '../telas.scss',
})
export class MetasComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly autenticacao = inject(ServicoAutenticacao);
  readonly rotulosIndicador = ROTULOS_INDICADOR;
  readonly rotulosSituacao = ROTULOS_SITUACAO_META;
  readonly rotulosStatus = ROTULOS_STATUS;
  readonly numero = numero;
  readonly data = data;
  readonly metas = signal<Meta[]>([]);
  readonly aluno = signal<string | null>(null);
  readonly podeGerenciar = computed(
    () => this.autenticacao.autorizacoesDe<AutorizacoesMeta>('meta')?.criar ?? false,
  );

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.metas.set(dados['tela'].resultado.projecoes));
    this.rota.queryParamMap
      .pipe(takeUntilDestroyed())
      .subscribe((parametros) => this.aluno.set(parametros.get('aluno')));
  }
}
