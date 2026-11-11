import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ServicoAutenticacao, IAgregadoAutorizacoes } from '../../d5';
import { dataHora } from '../formatacao';
import { Plano, ROTULOS_SITUACAO } from './treinos.service';

interface AutorizacoesPlano extends IAgregadoAutorizacoes {
  criar: boolean;
}

@Component({
  selector: 'app-planos',
  imports: [RouterLink],
  templateUrl: './planos.component.html',
  styleUrl: '../telas.scss',
})
export class PlanosComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly autenticacao = inject(ServicoAutenticacao);
  readonly rotulos = ROTULOS_SITUACAO;
  readonly dataHora = dataHora;
  readonly planos = signal<Plano[]>([]);
  readonly aluno = signal<string | null>(null);
  readonly podeCriar = computed(
    () => this.autenticacao.autorizacoesDe<AutorizacoesPlano>('planoDeTreino')?.criar ?? false,
  );

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.planos.set(dados['tela'].resultado.projecoes));
    this.rota.queryParamMap
      .pipe(takeUntilDestroyed())
      .subscribe((parametros) => this.aluno.set(parametros.get('aluno')));
  }
}
