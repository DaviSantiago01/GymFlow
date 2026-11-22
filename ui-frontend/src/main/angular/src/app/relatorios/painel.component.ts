import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import {
  INDICADORES,
  Indicador,
  Painel,
  ValorIndicador,
  data,
  hojeIso,
  nomeMes,
  valorIndicador,
  variacaoCurta,
} from './relatorios.service';

@Component({
  selector: 'app-painel',
  imports: [RouterLink, FormsModule],
  templateUrl: './painel.component.html',
  styleUrls: ['../analises.scss', '../analises-graficos.scss'],
})
export class PainelComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly painel = signal<Painel | null>(null);
  readonly indicadores = INDICADORES;
  readonly data = data;
  readonly nomeMes = nomeMes;
  readonly valorIndicador = valorIndicador;
  readonly variacaoCurta = variacaoCurta;
  readonly meses = ultimosMeses(12);
  mes = '';

  readonly principais: Indicador[] = ['ALUNOS_ATIVOS', 'NOVAS_MATRICULAS', 'TAXA_EVASAO', 'RECEITA_LIQUIDA'];
  readonly secundarios: Indicador[] = ['FREQUENCIA_MEDIA', 'TAXA_RENOVACAO', 'TAXA_INADIMPLENCIA', 'ALUNOS_EM_RISCO'];

  readonly metasAtingidas = computed(() => this.painel()?.metas.filter((meta) => meta.atingida).length ?? 0);

  /** Pontos da linha de alunos ativos em coordenadas do SVG (600 × 200). */
  readonly grafico = computed(() => {
    const serie = this.painel()?.alunosAtivos ?? [];
    if (serie.length === 0) return null;
    const valores = serie.map((ponto) => ponto.valor);
    const minimo = Math.min(...valores);
    const maximo = Math.max(...valores);
    const folga = Math.max(1, Math.round((maximo - minimo) * 0.2));
    const piso = Math.max(0, minimo - folga);
    const teto = maximo + folga;
    const pontos = serie.map((ponto, indice) => ({
      x: 40 + (indice * 540) / Math.max(1, serie.length - 1),
      y: 180 - ((ponto.valor - piso) / Math.max(1, teto - piso)) * 160,
      valor: ponto.valor,
      rotulo: nomeMes(ponto.mes, false).substring(0, 3),
    }));
    return { pontos, linha: pontos.map((p) => `${p.x},${p.y}`).join(' '), piso, teto };
  });

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => {
      const painel = dados['painel'] as Painel;
      this.painel.set(painel);
      this.mes = painel.periodo.inicio.substring(0, 7);
    });
  }

  valor(indicador: Indicador): ValorIndicador | undefined {
    return this.painel()?.conteudo.indicadores.find((item) => item.indicador === indicador);
  }

  classeVariacao(item: ValorIndicador | undefined): string {
    const resultado = item?.variacao?.resultado;
    return resultado === 'MELHOROU' ? 'selo selo-bom' : resultado === 'PIOROU' ? 'selo selo-ruim' : 'selo';
  }

  trocarMes() {
    this.router.navigate([], { relativeTo: this.rota, queryParams: { mes: this.mes } });
  }
}

function ultimosMeses(quantidade: number): string[] {
  const [ano, mes] = hojeIso().split('-').map(Number);
  const lista: string[] = [];
  for (let indice = 0; indice < quantidade; indice++) {
    const data = new Date(ano, mes - 1 - indice, 1);
    lista.push(`${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`);
  }
  return lista;
}
