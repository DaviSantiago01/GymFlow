import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import { Detalhe, Recomendacao, RecomendacoesService, data, numero } from './recomendacoes.service';

@Component({
  selector: 'app-analise-recomendacao',
  imports: [FormsModule, RouterLink],
  templateUrl: './analise-recomendacao.component.html',
  styleUrls: ['../analises.scss', '../analises-recomendacoes.scss'],
})
export class AnaliseRecomendacaoComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(RecomendacoesService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);

  readonly detalhe = signal<Detalhe | null>(null);
  readonly ocupado = signal(false);
  readonly rejeitando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly data = data;
  readonly numero = numero;

  exercicio = '';
  series: number | null = null;
  minimas: number | null = null;
  maximas: number | null = null;
  carga: number | null = null;
  intervalo: number | null = null;
  observacao = '';
  motivo = '';

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => {
      const detalhe = dados['detalhe'] as Detalhe;
      this.detalhe.set(detalhe);
      const sugestao = detalhe.sugestao;
      if (sugestao) {
        this.exercicio = sugestao.exercicioSugerido;
        this.series = sugestao.series;
        this.minimas = sugestao.repeticoesMinimas;
        this.maximas = sugestao.repeticoesMaximas;
        this.carga = sugestao.carga;
        this.intervalo = sugestao.intervaloSegundos;
      }
    });
  }

  nomeExercicio(id: string): string {
    return this.detalhe()?.alternativas.find((opcao) => opcao.identificador === id)?.nome ?? this.detalhe()?.recomendacao.exercicioSugerido ?? '';
  }

  outrasOpcoes(): string {
    return (this.detalhe()?.alternativas ?? [])
      .filter((opcao) => opcao.identificador !== this.exercicio)
      .map((opcao) => opcao.nome.toLowerCase())
      .join(', ');
  }

  ajustado(): boolean {
    const sugestao = this.detalhe()?.sugestao;
    if (!sugestao) return false;
    return (
      this.exercicio !== sugestao.exercicioSugerido ||
      this.series !== sugestao.series ||
      this.minimas !== sugestao.repeticoesMinimas ||
      this.maximas !== sugestao.repeticoesMaximas ||
      this.carga !== sugestao.carga ||
      this.intervalo !== sugestao.intervaloSegundos
    );
  }

  /** RN011 e RN020: a aprovação cria uma nova versão do plano e preserva a atual. */
  aprovar() {
    const detalhe = this.detalhe();
    if (!detalhe) return;
    this.executar(
      this.api.aprovar(detalhe.recomendacao.identificador, {
        exercicioSugerido: detalhe.sugestao ? this.exercicio : null,
        series: this.series,
        repeticoesMinimas: this.minimas,
        repeticoesMaximas: this.maximas,
        carga: this.carga,
        intervaloSegundos: this.intervalo,
        observacao: this.observacao.trim() || null,
      }),
      (recomendacao) =>
        recomendacao.planoGerado
          ? `Aprovada. A versão ${recomendacao.planoGeradoVersao} do plano foi criada e a anterior ficou guardada.`
          : 'Sugestão aprovada.',
      (recomendacao) =>
        recomendacao.planoGerado
          ? this.router.navigate(['/core/recomendacao', recomendacao.identificador, 'versoes'])
          : this.router.navigate(['/core/recomendacao/aprovacoes']),
    );
  }

  rejeitar() {
    const detalhe = this.detalhe();
    if (!detalhe) return;
    this.executar(
      this.api.rejeitar(detalhe.recomendacao.identificador, this.motivo.trim() || null),
      () => 'Sugestão rejeitada. Ela não volta a ser sugerida durante a carência.',
      () => this.router.navigate(['/core/recomendacao/aprovacoes']),
    );
  }

  private executar(
    pedido: Observable<Recomendacao>,
    mensagem: (recomendacao: Recomendacao) => string,
    depois: (recomendacao: Recomendacao) => void,
  ) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set(null);
    pedido
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (recomendacao) => {
          this.notificacoes.publicar('sucesso', mensagem(recomendacao));
          depois(recomendacao);
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível concluir a análise.')),
      });
  }
}
