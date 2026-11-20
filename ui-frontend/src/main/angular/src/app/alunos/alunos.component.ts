import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AlunoResumo, ROTULOS_SITUACAO_ALUNO, SituacaoAluno, cpfFormatado } from './alunos.service';

@Component({
  selector: 'app-alunos',
  imports: [FormsModule, RouterLink],
  templateUrl: './alunos.component.html',
  styleUrl: '../telas.scss',
})
export class AlunosComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly rotulos = ROTULOS_SITUACAO_ALUNO;
  readonly situacoes = Object.keys(ROTULOS_SITUACAO_ALUNO) as SituacaoAluno[];
  readonly cpfFormatado = cpfFormatado;
  readonly alunos = signal<AlunoResumo[]>([]);
  busca = '';
  situacao = '';

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => this.alunos.set(dados['tela']));
    this.rota.queryParamMap.pipe(takeUntilDestroyed()).subscribe((parametros) => {
      this.busca = parametros.get('busca') ?? '';
      this.situacao = parametros.get('situacao') ?? '';
    });
  }

  filtrar() {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { busca: this.busca.trim() || null, situacao: this.situacao || null },
    });
  }

  alerta(situacao: SituacaoAluno) {
    return situacao !== 'ATIVO';
  }
}
