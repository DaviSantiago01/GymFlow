import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, finalize, forkJoin, of, switchMap } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import { CATEGORIAS, Recomendacao, RecomendacoesService, SITUACOES, Visiveis, data } from './recomendacoes.service';

@Component({
  selector: 'app-recomendacoes',
  imports: [RouterLink],
  templateUrl: './recomendacoes.component.html',
  styleUrls: ['../analises.scss', '../analises-graficos.scss', '../analises-recomendacoes.scss'],
})
export class RecomendacoesComponent {
  private readonly api = inject(RecomendacoesService);
  private readonly router = inject(Router);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);

  readonly visiveis = signal<Visiveis | null>(null);
  readonly respondidas = signal<Recomendacao[]>([]);
  readonly aviso = signal<string | null>(null);
  readonly ocupado = signal(false);
  readonly categorias = CATEGORIAS;
  readonly situacoes = SITUACOES;
  readonly data = data;
  readonly desativadas = computed(() => (this.visiveis()?.taxas ?? []).filter((taxa) => taxa.prioridadeReduzida));

  constructor() {
    this.carregar(true);
  }

  /** Ao abrir a tela, gera as recomendações do dia (RN001 e RN002) e mostra as visíveis, já priorizadas (RN016). */
  carregar(gerar: boolean) {
    const geracao = gerar ? this.api.gerar().pipe(catchError(() => of({ geradas: 0, aviso: null }))) : of({ geradas: 0, aviso: null });
    geracao
      .pipe(
        switchMap((resultado) => {
          this.aviso.set(resultado.aviso);
          return forkJoin({
            visiveis: this.api.minhas(),
            historico: this.api.pesquisar({ tamanhoPagina: 20 }),
          });
        }),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe({
        next: ({ visiveis, historico }) => {
          this.visiveis.set(visiveis);
          this.respondidas.set(
            historico.resultado.projecoes.filter((r) => !['PENDENTE', 'AGUARDANDO_APROVACAO'].includes(r.situacao)).slice(0, 4),
          );
        },
        error: (falha) => this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível carregar as recomendações.')),
      });
  }

  aceitar(recomendacao: Recomendacao) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.api
      .aceitar(recomendacao.identificador)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: () => {
          this.notificacoes.publicar(
            'sucesso',
            recomendacao.criterio === 'AULA_COMPATIVEL' ? 'Inscrição feita na aula.' : 'Recomendação aceita.',
          );
          this.carregar(false);
        },
        error: (falha) => this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível aceitar.')),
      });
  }

  ciente(recomendacao: Recomendacao) {
    this.api
      .ciente(recomendacao.identificador)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({ next: () => this.carregar(false) });
  }

  responder(recomendacao: Recomendacao) {
    this.router.navigate(['/core/recomendacao', recomendacao.identificador, 'visualizacao'], { queryParams: { responder: 'recusar' } });
  }

  rotuloAceite(recomendacao: Recomendacao): string {
    switch (recomendacao.criterio) {
      case 'REAVALIACAO_FISICA':
        return 'Agendar avaliação';
      case 'AULA_COMPATIVEL':
        return 'Inscrever-me';
      case 'OFERTA_PLANO':
        return 'Tenho interesse';
      default:
        return 'Aceitar';
    }
  }
}
