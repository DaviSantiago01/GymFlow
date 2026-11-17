import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoAutenticacao, ServicoNotificacao } from '../../d5';
import { data, mensagemDeErro } from '../formatacao';
import {
  AutorizacoesAdesao,
  Mensalidade,
  PlanosService,
  ROTULOS_SITUACAO_MENSALIDADE,
  moeda,
} from './planos.service';

@Component({
  selector: 'app-mensalidades',
  imports: [FormsModule, RouterLink],
  templateUrl: './mensalidades.component.html',
  styleUrl: '../telas.scss',
})
export class MensalidadesComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(PlanosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly autenticacao = inject(ServicoAutenticacao);
  private readonly destroy = inject(DestroyRef);
  readonly rotulos = ROTULOS_SITUACAO_MENSALIDADE;
  readonly moeda = moeda;
  readonly data = data;
  readonly mensalidades = signal<Mensalidade[]>([]);
  readonly ocupado = signal(false);
  readonly podeGerar = computed(
    () =>
      this.autenticacao.autorizacoesDe<AutorizacoesAdesao>('adesaoPlano')?.gerarMensalidades ?? false,
  );
  readonly totalEmAtraso = computed(() =>
    this.mensalidades()
      .filter((mensalidade) => mensalidade.situacao === 'ATRASADA')
      .reduce((total, mensalidade) => total + mensalidade.valorDevido, 0),
  );
  situacao = '';
  aluno: string | null = null;

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.mensalidades.set(dados['tela'].resultado.projecoes));
    this.rota.queryParamMap.pipe(takeUntilDestroyed()).subscribe((parametros) => {
      this.situacao = parametros.get('situacao') ?? '';
      this.aluno = parametros.get('aluno');
    });
  }

  filtrar() {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { situacao: this.situacao || null },
      queryParamsHandling: 'merge',
    });
  }

  gerar() {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.api
      .gerarMensalidades()
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: ({ geradas }) => {
          this.notificacoes.publicar(
            'sucesso',
            geradas === 0
              ? 'Todas as mensalidades dos períodos iniciados já existiam.'
              : `${geradas} mensalidade(s) gerada(s).`,
          );
          this.router.navigate([], {
            relativeTo: this.rota,
            queryParamsHandling: 'preserve',
            onSameUrlNavigation: 'reload',
          });
        },
        error: (falha) =>
          this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível gerar as mensalidades.')),
      });
  }
}
