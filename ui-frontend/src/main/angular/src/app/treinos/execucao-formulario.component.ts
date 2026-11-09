import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { mensagemDeErro, numero } from '../formatacao';
import {
  DetalhePlano,
  DiaSemana,
  ROTULOS_DIA,
  SerieDetalhe,
  Sugestao,
  TreinosService,
} from './treinos.service';

interface EntradaExecucao {
  serie: SerieDetalhe;
  series: number;
  repeticoes: number;
  carga: number;
}

@Component({
  selector: 'app-execucao-formulario',
  imports: [FormsModule, RouterLink],
  templateUrl: './execucao-formulario.component.html',
  styleUrl: '../telas.scss',
})
export class ExecucaoFormularioComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly api = inject(TreinosService);
  private readonly destroy = inject(DestroyRef);
  readonly rotulosDia = ROTULOS_DIA;
  readonly numero = numero;
  readonly detalhe = signal<DetalhePlano>(this.rota.snapshot.data['tela']);
  readonly diaEscolhido = signal<DiaSemana>(this.detalhe().treinos[0]?.dia ?? 'SEGUNDA');
  readonly entradas = computed(() => this.montarEntradas());
  readonly ocupado = signal(false);
  readonly erro = signal('');
  readonly aviso = signal('');
  readonly sugestao = signal<Sugestao | null>(null);

  private montarEntradas(): EntradaExecucao[] {
    const treino = this.detalhe().treinos.find((item) => item.dia === this.diaEscolhido());
    return (treino?.series ?? []).map((serie) => ({
      serie,
      series: serie.quantidade,
      repeticoes: serie.repeticoesMaximas,
      carga: serie.carga,
    }));
  }

  registrar(entrada: EntradaExecucao) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    this.aviso.set('');
    this.sugestao.set(null);
    this.api
      .registrarExecucao({
        plano: this.detalhe().plano.identificador,
        dia: this.diaEscolhido(),
        exercicio: entrada.serie.exercicio,
        series: entrada.series,
        repeticoes: entrada.repeticoes,
        carga: entrada.carga,
      })
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (registrada) => {
          this.aviso.set(`Execução de ${registrada.execucao.exercicioNome} registrada.`);
          this.sugestao.set(registrada.sugestao);
        },
        error: (falha) =>
          this.erro.set(mensagemDeErro(falha, 'Não foi possível registrar a execução.')),
      });
  }
}
