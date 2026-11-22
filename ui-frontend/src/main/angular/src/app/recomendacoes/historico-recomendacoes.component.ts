import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, forkJoin, of, switchMap } from 'rxjs';
import { ServicoAutenticacao } from '../../d5';
import { CATEGORIAS, Categoria, Recomendacao, RecomendacoesService, ResumoAluno, SITUACOES, Taxa, data } from './recomendacoes.service';

@Component({
  selector: 'app-historico-recomendacoes',
  imports: [FormsModule, RouterLink],
  templateUrl: './historico-recomendacoes.component.html',
  styleUrls: ['../analises.scss', '../analises-recomendacoes.scss'],
})
export class HistoricoRecomendacoesComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(RecomendacoesService);
  private readonly autenticacao = inject(ServicoAutenticacao);
  private readonly destroy = inject(DestroyRef);

  readonly itens = signal<Recomendacao[]>([]);
  readonly taxas = signal<Taxa[]>([]);
  readonly aluno = signal<ResumoAluno | null>(null);
  readonly personal = this.autenticacao.autorizacoes().includes('PERSONAL');
  readonly categorias = CATEGORIAS;
  readonly listaCategorias = Object.keys(CATEGORIAS) as Categoria[];
  readonly situacoes = SITUACOES;
  readonly data = data;
  categoria = '';
  dias = '180';
  alunoId = '';

  constructor() {
    this.rota.queryParamMap
      .pipe(
        switchMap((parametros) => {
          this.categoria = parametros.get('categoria') ?? '';
          this.dias = parametros.get('dias') ?? '180';
          this.alunoId = parametros.get('aluno') ?? '';
          const resumo: Observable<ResumoAluno | null> = this.alunoId
            ? this.api.resumoDoAluno(this.alunoId)
            : this.personal
              ? of(null)
              : this.api.minhas().pipe(switchMap((visiveis) => of(visiveis.aluno)));
          return resumo.pipe(
            switchMap((aluno) =>
              forkJoin({
                aluno: of(aluno),
                itens: this.api.pesquisar({
                  aluno: aluno?.aluno ?? null,
                  categoria: this.categoria || null,
                  dias: this.dias === 'todos' ? null : Number(this.dias),
                }),
                taxas: aluno ? this.api.taxas(aluno.aluno) : of([] as Taxa[]),
              }),
            ),
          );
        }),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe(({ aluno, itens, taxas }) => {
        this.aluno.set(aluno);
        this.itens.set(itens.resultado.projecoes);
        this.taxas.set(taxas);
      });
  }

  filtrar() {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { categoria: this.categoria || null, dias: this.dias === '180' ? null : this.dias },
      queryParamsHandling: 'merge',
    });
  }

  respondente(item: Recomendacao): string {
    if (item.situacao === 'EXPIRADA') return 'Sem resposta';
    if (!item.respondente) return 'Aguardando resposta';
    return item.criterio === 'SUBSTITUICAO_EXERCICIO' || item.criterio === 'REVISAO_PLANO'
      ? `${item.respondente} (personal)`
      : item.respondente;
  }
}
