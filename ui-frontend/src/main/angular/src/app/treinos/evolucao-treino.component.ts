import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { dataHora, diaMes, numero } from '../formatacao';
import { Evolucao, Execucao } from './treinos.service';

@Component({
  selector: 'app-evolucao-treino',
  imports: [RouterLink],
  templateUrl: './evolucao-treino.component.html',
  styleUrl: '../telas.scss',
})
export class EvolucaoTreinoComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly numero = numero;
  readonly dataHora = dataHora;
  readonly diaMes = diaMes;
  readonly evolucao = signal<Evolucao>(this.rota.snapshot.data['tela']);
  readonly aluno = signal<string | null>(null);
  readonly maiorBarra = computed(() => {
    let maior = 0;
    for (const execucao of this.evolucao().execucoes) {
      if (execucao.carga > maior) maior = execucao.carga;
    }
    return maior;
  });

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.evolucao.set(dados['tela']));
    this.rota.queryParamMap
      .pipe(takeUntilDestroyed())
      .subscribe((parametros) => this.aluno.set(parametros.get('aluno')));
  }

  altura(execucao: Execucao) {
    return this.maiorBarra() === 0 ? 0 : (execucao.carga / this.maiorBarra()) * 85;
  }

  rotuloVisivel(execucao: Execucao, ultima: boolean) {
    return ultima || execucao.carga === this.maiorBarra();
  }

  escolherExercicio(exercicio: string) {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { exercicio },
      queryParamsHandling: 'merge',
    });
  }
}
