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
  recorrencia,
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
  readonly recorrencia = recorrencia;
  readonly data = data;
  readonly adesoes = signal<Adesao[]>([]);
  readonly formulario = signal<FormularioAdesao>({ alunos: [], planos: [] });
  readonly ocupado = signal(false);
  readonly erro = signal('');
  aluno = '';
  plano = '';
  quantidadePeriodos: number | null = null;
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

  /** Resumo do que será cobrado com o plano e a recorrência escolhidos. */
  previa(): string {
    const plano = this.formulario().planos.find((opcao) => opcao.identificador === this.plano);
    if (!plano) return 'Sem recorrência, a cobrança continua até a troca ou o cancelamento do plano.';
    const quantidade = this.quantidadePeriodos;
    if (!quantidade) {
      return `${moeda(plano.valor)} a cada ${duracao(plano.duracaoMeses)}, até a troca ou o cancelamento do plano.`;
    }
    const meses = quantidade * plano.duracaoMeses;
    return `${quantidade} cobrança(s) de ${moeda(plano.valor)}, uma a cada ${duracao(plano.duracaoMeses)}: o plano dura ${duracao(meses)}.`;
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
      .aderir(this.aluno, this.plano, this.quantidadePeriodos ?? null)
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
