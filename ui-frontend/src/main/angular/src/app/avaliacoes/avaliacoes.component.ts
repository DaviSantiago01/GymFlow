import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ServicoAutenticacao, IAgregadoAutorizacoes } from '../../d5';
import { data, numero } from '../formatacao';
import {
  Avaliacao,
  ROTULOS_CLASSIFICACAO,
  ROTULOS_SITUACAO_AVALIACAO,
} from './avaliacoes.service';

interface AutorizacoesAvaliacao extends IAgregadoAutorizacoes {
  registrar: boolean;
}

@Component({
  selector: 'app-avaliacoes',
  imports: [RouterLink],
  templateUrl: './avaliacoes.component.html',
  styleUrl: '../telas.scss',
})
export class AvaliacoesComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly autenticacao = inject(ServicoAutenticacao);
  readonly rotulosClassificacao = ROTULOS_CLASSIFICACAO;
  readonly rotulosSituacao = ROTULOS_SITUACAO_AVALIACAO;
  readonly numero = numero;
  readonly data = data;
  readonly avaliacoes = signal<Avaliacao[]>([]);
  readonly aluno = signal<string | null>(null);
  readonly todas = signal(false);
  readonly podeRegistrar = computed(
    () =>
      this.autenticacao.autorizacoesDe<AutorizacoesAvaliacao>('avaliacaoFisica')?.registrar ?? false,
  );

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.avaliacoes.set(dados['tela'].resultado.projecoes));
    this.rota.queryParamMap.pipe(takeUntilDestroyed()).subscribe((parametros) => {
      this.aluno.set(parametros.get('aluno'));
      this.todas.set(parametros.has('todas'));
    });
  }

  alternarTodas() {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { todas: this.todas() ? null : 'sim' },
      queryParamsHandling: 'merge',
    });
  }
}
