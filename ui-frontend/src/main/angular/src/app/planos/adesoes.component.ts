import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { data, mensagemDeErro } from '../formatacao';
import {
  Adesao,
  FormularioAdesao,
  PlanosService,
  ROTULOS_SITUACAO_ADESAO,
  duracao,
  moeda,
} from './planos.service';

@Component({
  selector: 'app-adesoes',
  imports: [FormsModule, RouterLink],
  templateUrl: './adesoes.component.html',
  styleUrl: '../telas.scss',
})
export class AdesoesComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(PlanosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);
  readonly rotulosSituacao = ROTULOS_SITUACAO_ADESAO;
  readonly moeda = moeda;
  readonly duracao = duracao;
  readonly data = data;
  readonly adesoes = signal<Adesao[]>([]);
  readonly formulario = signal<FormularioAdesao>({ alunos: [], planos: [] });
  readonly ocupado = signal(false);
  readonly erro = signal('');
  aluno = '';
  plano = '';
  filtroAluno = '';
  filtroSituacao = '';

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => {
      this.adesoes.set(dados['tela'].resultado.projecoes);
      this.formulario.set(dados['formulario']);
    });
    this.rota.queryParamMap.pipe(takeUntilDestroyed()).subscribe((parametros) => {
      this.filtroAluno = parametros.get('aluno') ?? '';
      this.filtroSituacao = parametros.get('situacao') ?? '';
    });
  }

  filtrar() {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { aluno: this.filtroAluno || null, situacao: this.filtroSituacao || null },
    });
  }

  aderir() {
    if (this.ocupado()) return;
    if (!this.aluno || !this.plano) {
      this.erro.set('Selecione o aluno e o plano.');
      return;
    }
    this.ocupado.set(true);
    this.erro.set('');
    this.api
      .aderir(this.aluno, this.plano)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (adesao) => {
          this.notificacoes.publicar(
            'sucesso',
            `${adesao.alunoNome} aderiu ao plano ${adesao.planoNome}. A primeira mensalidade foi gerada.`,
          );
          this.router.navigate(['/core/adesao-plano', adesao.identificador, 'visualizacao']);
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível registrar a adesão.')),
      });
  }
}
