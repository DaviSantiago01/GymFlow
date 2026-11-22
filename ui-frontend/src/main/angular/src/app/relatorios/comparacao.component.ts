import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  INDICADORES,
  Relatorio,
  ValorIndicador,
  data,
  decimal,
  sinal,
  valorIndicador,
  variacaoAbsoluta,
} from './relatorios.service';

@Component({
  selector: 'app-comparacao',
  imports: [RouterLink],
  templateUrl: './comparacao.component.html',
  styleUrls: ['../analises.scss', '../analises-graficos.scss'],
})
export class ComparacaoComponent {
  private readonly rota = inject(ActivatedRoute);
  readonly relatorio = signal<Relatorio | null>(null);
  readonly indicadores = INDICADORES;
  readonly data = data;
  readonly valorIndicador = valorIndicador;
  readonly variacaoAbsoluta = variacaoAbsoluta;
  readonly maiorVariacao = computed(() =>
    Math.max(1, ...(this.relatorio()?.conteudo.indicadores ?? []).map((item) => Math.abs(item.variacao?.percentual ?? 0))),
  );

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => this.relatorio.set(dados['relatorio']));
  }

  percentual(item: ValorIndicador): string {
    const valor = item.variacao?.percentual;
    if (valor === null || valor === undefined) return 'não calculada';
    return `${sinal(valor)}${decimal(Math.abs(valor))}%`;
  }

  largura(item: ValorIndicador): number {
    return (Math.abs(item.variacao?.percentual ?? 0) / this.maiorVariacao()) * 100;
  }
}
