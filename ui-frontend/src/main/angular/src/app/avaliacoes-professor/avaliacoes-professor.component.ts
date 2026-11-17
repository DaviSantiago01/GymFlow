import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, switchMap } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AvaliacoesProfessorService,
  Criterio,
  Escolhas,
  Notas,
  TelaAvaliacao,
  ModoAvaliacao,
  escolhasVazias,
  validarAvaliacao,
} from './avaliacoes-professor.service';
import { CriteriosComponent } from './criterios.component';
@Component({
  selector: 'app-avaliacoes-professor',
  imports: [DatePipe, DecimalPipe, FormsModule, RouterLink, RouterLinkActive, CriteriosComponent],
  templateUrl: './avaliacoes-professor.component.html',
  styleUrl: './avaliacoes-professor.component.scss',
})
export class AvaliacoesProfessorComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(AvaliacoesProfessorService);
  readonly dados = signal<TelaAvaliacao>({});
  readonly modo = signal<ModoAvaliacao>('minhas');
  readonly notas = signal<Escolhas>(escolhasVazias());
  readonly comentario = signal('');
  readonly busca = signal('');
  readonly erro = signal('');
  readonly erros = signal<Record<string, string>>({});
  readonly ocupado = signal(false);
  readonly aviso = signal('');
  readonly pagina = signal(0);
  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((data) => {
      const tela: TelaAvaliacao = data['tela'];
      this.dados.set(tela);
      this.modo.set(data['modo']);
      this.notas.set(tela.avaliacao ? { ...tela.avaliacao.notas } : escolhasVazias());
      this.comentario.set(tela.avaliacao?.comentario ?? '');
      this.erro.set('');
      this.erros.set({});
      this.busca.set(this.rota.snapshot.queryParamMap.get('busca') ?? '');
      this.pagina.set(Math.max(0, Number(this.rota.snapshot.queryParamMap.get('pagina')) || 0));
      const aviso = this.rota.snapshot.queryParamMap.get('aviso');
      this.aviso.set(
        aviso === 'salva'
          ? 'Avaliação salva com sucesso.'
          : aviso === 'excluida'
            ? 'Avaliação excluída. As médias foram atualizadas.'
            : '',
      );
    });
  }
  escolher(escolha: { chave: Criterio; nota: number }) {
    this.notas.update((n) => ({ ...n, [escolha.chave]: escolha.nota }));
    this.limparErro(escolha.chave);
  }
  escreverComentario(texto: string) {
    this.comentario.set(texto);
    if (texto.length <= 500) this.limparErro('comentario');
  }
  private limparErro(campo: string) {
    this.erros.update((atuais) => {
      const restantes = { ...atuais };
      delete restantes[campo];
      return restantes;
    });
    if (
      !Object.keys(this.erros()).length &&
      this.erro() === 'Preencha as cinco notas e confira o comentário.'
    )
      this.erro.set('');
  }
  private focarErro() {
    setTimeout(() => document.getElementById('erro-avaliacao')?.focus());
  }
  carregarDemonstracao() {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    this.api
      .carregarDemonstracao()
      .pipe(
        switchMap(() => this.api.carregar('treinos', '', this.pagina(), '')),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (dados) => {
          this.dados.set(dados);
          this.aviso.set(
            'Treinos de demonstração carregados. Eles estão identificados como exemplos.',
          );
        },
        error: (e: HttpErrorResponse) => this.falha(e),
      });
  }
  temDemonstracao() {
    return this.dados().treinos?.some((t) => t.demonstracao) ?? false;
  }
  pesquisar() {
    this.router.navigate(['/core/professor/pesquisa'], {
      queryParams: { busca: this.busca().trim() || null },
    });
  }
  mudarPagina(pagina: number) {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { pagina },
      queryParamsHandling: 'merge',
    });
  }
  iniciais(nome: string) {
    return nome
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((n) => n[0])
      .join('');
  }
  salvar() {
    if (this.ocupado()) return;
    const erros = validarAvaliacao(this.notas(), this.comentario());
    this.erros.set(erros);
    this.erro.set('');
    if (Object.keys(erros).length) {
      this.erro.set('Preencha as cinco notas e confira o comentário.');
      this.focarErro();
      return;
    }
    const d = this.dados();
    const treino = d.treino?.identificador ?? d.avaliacao?.treino;
    if (!treino && !d.avaliacao) return;
    this.ocupado.set(true);
    this.api
      .salvar(treino ?? '', d.avaliacao?.identificador, {
        ...(this.notas() as Notas),
        comentario: this.comentario(),
      })
      .pipe(finalize(() => this.ocupado.set(false)))
      .subscribe({
        next: (a) =>
          this.router.navigate(['/core/avaliacao-professor', a.identificador, 'visualizacao'], {
            queryParams: { aviso: 'salva' },
          }),
        error: (e: HttpErrorResponse) => this.falha(e),
      });
  }
  excluir() {
    const a = this.dados().avaliacao;
    if (
      !a ||
      this.ocupado() ||
      !window.confirm(
        'Excluir esta avaliação permanentemente? As notas e o comentário serão removidos. Você não poderá criar outra avaliação para este treino.',
      )
    )
      return;
    this.ocupado.set(true);
    this.erro.set('');
    this.api
      .excluir(a.identificador)
      .pipe(finalize(() => this.ocupado.set(false)))
      .subscribe({
        next: () =>
          this.router.navigate(['/core/treino/pesquisa'], {
            queryParams: { aviso: 'excluida' },
          }),
        error: (e: HttpErrorResponse) => this.falha(e),
      });
  }
  private falha(e: HttpErrorResponse) {
    this.erro.set(
      e.error?.mensagem ?? 'Não foi possível concluir. Verifique sua conexão e tente novamente.',
    );
    this.erros.set(e.error?.campos ?? {});
    this.focarErro();
  }
}
