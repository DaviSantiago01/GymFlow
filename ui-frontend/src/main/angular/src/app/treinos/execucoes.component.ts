import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { dataHora, numero } from '../formatacao';
import { Execucao, ROTULOS_DIA } from './treinos.service';

@Component({
  selector: 'app-execucoes',
  imports: [FormsModule, RouterLink],
  templateUrl: './execucoes.component.html',
  styleUrl: '../telas.scss',
})
export class ExecucoesComponent {
  private readonly rota = inject(ActivatedRoute);
  readonly rotulosDia = ROTULOS_DIA;
  readonly numero = numero;
  readonly dataHora = dataHora;
  readonly execucoes = signal<Execucao[]>([]);
  readonly busca = signal('');
  readonly aluno = signal<string | null>(null);
  readonly filtradas = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    return this.execucoes().filter((execucao) =>
      execucao.exercicioNome.toLowerCase().includes(termo),
    );
  });

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.execucoes.set(dados['tela'].resultado.projecoes));
    this.rota.queryParamMap
      .pipe(takeUntilDestroyed())
      .subscribe((parametros) => this.aluno.set(parametros.get('aluno')));
  }
}
