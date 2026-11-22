import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { Recomendacao, RecomendacoesService, SITUACOES, data, hojeIso } from './recomendacoes.service';

type Aba = 'AGUARDANDO_APROVACAO' | 'APROVADA' | 'REJEITADA';

@Component({
  selector: 'app-aprovacoes',
  imports: [RouterLink],
  templateUrl: './aprovacoes.component.html',
  styleUrls: ['../analises.scss', '../analises-recomendacoes.scss'],
})
export class AprovacoesComponent {
  private readonly api = inject(RecomendacoesService);
  private readonly destroy = inject(DestroyRef);

  readonly aba = signal<Aba>('AGUARDANDO_APROVACAO');
  readonly listas = signal<Record<Aba, Recomendacao[]>>({ AGUARDANDO_APROVACAO: [], APROVADA: [], REJEITADA: [] });
  readonly situacoes = SITUACOES;
  readonly data = data;
  readonly hoje = hojeIso();

  constructor() {
    const consulta = (situacao: Aba) =>
      this.api.pesquisar({ sugestoesDeTreino: true, situacao, ordenacao: situacao === 'AGUARDANDO_APROVACAO' ? 'VALIDADE' : 'GERACAO', descendente: situacao !== 'AGUARDANDO_APROVACAO' });
    forkJoin({
      AGUARDANDO_APROVACAO: consulta('AGUARDANDO_APROVACAO'),
      APROVADA: consulta('APROVADA'),
      REJEITADA: consulta('REJEITADA'),
    })
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe((resultado) =>
        this.listas.set({
          AGUARDANDO_APROVACAO: resultado.AGUARDANDO_APROVACAO.resultado.projecoes,
          APROVADA: resultado.APROVADA.resultado.projecoes,
          REJEITADA: resultado.REJEITADA.resultado.projecoes,
        }),
      );
  }

  tipo(recomendacao: Recomendacao): string {
    return recomendacao.criterio === 'SUBSTITUICAO_EXERCICIO' ? 'Substituição de exercício' : 'Revisão do plano';
  }

  iniciais(nome: string): string {
    return nome
      .split(' ')
      .filter((parte) => parte.length > 0)
      .slice(0, 2)
      .map((parte) => parte[0].toUpperCase())
      .join('');
  }
}
