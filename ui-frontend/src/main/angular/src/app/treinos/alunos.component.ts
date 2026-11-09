import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Aluno } from './treinos.service';

@Component({
  selector: 'app-alunos',
  imports: [RouterLink, FormsModule],
  templateUrl: './alunos.component.html',
  styleUrl: '../telas.scss',
})
export class AlunosComponent {
  private readonly rota = inject(ActivatedRoute);
  readonly alunos = signal<Aluno[]>([]);
  readonly busca = signal('');
  readonly filtrados = computed(() => {
    const termo = this.busca().trim().toLowerCase();
    return this.alunos().filter(
      (aluno) =>
        aluno.nome.toLowerCase().includes(termo) || aluno.usuario.toLowerCase().includes(termo),
    );
  });

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.alunos.set(dados['tela'].resultado.projecoes));
  }
}
