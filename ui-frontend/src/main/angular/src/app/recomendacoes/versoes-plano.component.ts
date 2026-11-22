import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ServicoAutenticacao } from '../../d5';
import { Comparacao, SerieDetalhe, TreinoDetalhe, data, numero } from './recomendacoes.service';

interface LinhaComparacao {
  nome: string;
  anterior: SerieDetalhe | null;
  atual: SerieDetalhe | null;
}

@Component({
  selector: 'app-versoes-plano',
  imports: [RouterLink],
  templateUrl: './versoes-plano.component.html',
  styleUrls: ['../analises.scss', '../analises-recomendacoes.scss'],
})
export class VersoesPlanoComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly autenticacao = inject(ServicoAutenticacao);
  readonly comparacao = signal<Comparacao | null>(null);
  readonly data = data;
  readonly aluno = this.autenticacao.autorizacoes().includes('USUARIO');

  /** Treinos lado a lado: exercícios removidos, incluídos e mantidos entre as duas versões. */
  readonly treinos = computed(() => {
    const comparacao = this.comparacao();
    if (!comparacao) return [];
    return comparacao.atual.treinos.map((treino) => {
      const anterior = comparacao.anterior.treinos.find((item) => item.dia === treino.dia);
      return { treino, linhas: linhas(anterior, treino) };
    });
  });

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => this.comparacao.set(dados['comparacao']));
  }

  serie(valor: SerieDetalhe | null): string {
    if (!valor) return '—';
    return `${valor.quantidade} × ${valor.repeticoesMinimas}–${valor.repeticoesMaximas}, ${numero(valor.carga)} kg, ${valor.intervaloSegundos} s`;
  }

  alterados(linhasDoTreino: LinhaComparacao[]): number {
    return linhasDoTreino.filter((linha) => !linha.anterior || !linha.atual).length / 2;
  }
}

function linhas(anterior: TreinoDetalhe | undefined, atual: TreinoDetalhe): LinhaComparacao[] {
  const resultado: LinhaComparacao[] = [];
  const antigas = anterior?.series ?? [];
  for (const serie of antigas) {
    const mantida = atual.series.find((item) => item.exercicio === serie.exercicio) ?? null;
    resultado.push({ nome: serie.exercicioNome, anterior: serie, atual: mantida });
  }
  for (const serie of atual.series) {
    if (!antigas.some((item) => item.exercicio === serie.exercicio)) {
      const posicao = atual.series.indexOf(serie);
      resultado.splice(Math.min(posicao + 1, resultado.length), 0, { nome: serie.exercicioNome, anterior: null, atual: serie });
    }
  }
  return resultado;
}
