import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { mensagemDeErro } from '../formatacao';
import {
  DetalhePlano,
  DiaSemana,
  FaixaObjetivo,
  FormularioPlano,
  Objetivo,
  PlanoAgregado,
  PlanoEnvio,
  ROTULOS_DIA,
  ROTULOS_OBJETIVO,
  TreinoEnvio,
  TreinosService,
} from './treinos.service';

interface TreinoEdicao extends TreinoEnvio {
  exercicioEscolhido: string;
}

const INTERVALO_PADRAO = 60;

@Component({
  selector: 'app-plano-formulario',
  imports: [FormsModule, RouterLink],
  templateUrl: './plano-formulario.component.html',
  styleUrl: '../telas.scss',
})
export class PlanoFormularioComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(TreinosService);
  private readonly destroy = inject(DestroyRef);
  readonly rotulosDia = ROTULOS_DIA;
  readonly rotulosObjetivo = ROTULOS_OBJETIVO;
  readonly formulario = signal<FormularioPlano>(this.rota.snapshot.data['tela'].formulario);
  readonly plano = signal<PlanoAgregado | null>(this.rota.snapshot.data['tela'].plano);
  readonly treinos = signal<TreinoEdicao[]>([]);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  /** Motivo da RN003 exibido em cada treino, indexado pelo dia. */
  readonly repeticoes = signal<Record<string, string>>({});
  readonly alteracao = computed(() => this.plano() !== null);
  alunoEscolhido = '';
  nome = '';
  frequencia = 1;

  constructor() {
    const plano = this.plano();
    if (plano) {
      this.alunoEscolhido = plano.aluno;
      this.nome = plano.nome;
      this.frequencia = plano.frequenciaSemanal;
      this.treinos.set(plano.treinos.map((treino) => ({ ...treino, exercicioEscolhido: '' })));
    } else {
      this.alunoEscolhido = this.rota.snapshot.queryParamMap.get('aluno') ?? '';
    }
    this.rota.data.pipe(takeUntilDestroyed()).subscribe(() => this.erro.set(''));
  }

  diaSelecionado(dia: DiaSemana) {
    return this.treinos().some((treino) => treino.dia === dia);
  }

  alternarDia(dia: DiaSemana) {
    if (this.diaSelecionado(dia)) {
      this.treinos.set(this.treinos().filter((treino) => treino.dia !== dia));
      return;
    }
    const novo: TreinoEdicao = {
      nome: `Treino de ${ROTULOS_DIA[dia]}`,
      dia,
      objetivo: 'HIPERTROFIA',
      series: [],
      exercicioEscolhido: '',
    };
    const ordem = this.formulario().dias;
    this.treinos.set(
      [...this.treinos(), novo].sort((a, b) => ordem.indexOf(a.dia) - ordem.indexOf(b.dia)),
    );
  }

  faixa(objetivo: Objetivo): FaixaObjetivo {
    return this.formulario().objetivos.find((item) => item.objetivo === objetivo)!;
  }


  nomeExercicio(identificador: string) {
    return (
      this.formulario().exercicios.find((exercicio) => exercicio.identificador === identificador)
        ?.nome ?? identificador
    );
  }

  adicionarExercicio(treino: TreinoEdicao) {
    if (!treino.exercicioEscolhido) return;
    // RN003: o mesmo exercício não pode se repetir no treino
    const repetido = treino.series.some((serie) => serie.exercicio === treino.exercicioEscolhido);
    const motivo = repetido
      ? `O exercício ${this.nomeExercicio(treino.exercicioEscolhido)} já faz parte do treino ${treino.nome}`
      : '';
    this.repeticoes.set({ ...this.repeticoes(), [treino.dia]: motivo });
    if (repetido) return;
    const faixa = this.faixa(treino.objetivo);
    treino.series.push({
      exercicio: treino.exercicioEscolhido,
      quantidade: faixa.seriesMinimas,
      repeticoesMinimas: faixa.repeticoesMinimas,
      repeticoesMaximas: faixa.repeticoesMaximas,
      carga: 0,
      intervaloSegundos: INTERVALO_PADRAO,
    });
    treino.exercicioEscolhido = '';
    this.treinos.set([...this.treinos()]);
  }

  removerExercicio(treino: TreinoEdicao, exercicio: string) {
    treino.series = treino.series.filter((serie) => serie.exercicio !== exercicio);
    this.treinos.set([...this.treinos()]);
  }

  private montarEnvio(): PlanoEnvio {
    return {
      nome: this.nome,
      frequenciaSemanal: this.frequencia,
      treinos: this.treinos().map(({ exercicioEscolhido, ...treino }) => treino),
    };
  }

  salvar() {
    if (this.ocupado()) return;
    const plano = this.plano();
    if (!plano && !this.alunoEscolhido) {
      this.erro.set('Selecione o aluno do plano.');
      return;
    }
    const envio = this.montarEnvio();
    const requisicao = plano
      ? this.api.revisar(plano.identificador, envio)
      : this.api.criar(this.alunoEscolhido, envio);
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (detalhe) => this.abrir(detalhe),
        error: (falha) =>
          this.erro.set(mensagemDeErro(falha, 'Não foi possível salvar o plano. Tente novamente.')),
      });
  }

  encerrar() {
    const plano = this.plano();
    if (!plano || this.ocupado() || !window.confirm('Encerrar este plano de treino?')) return;
    this.ocupado.set(true);
    this.erro.set('');
    this.api
      .executarAcao(plano.identificador, 'encerrar')
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (detalhe) => this.abrir(detalhe),
        error: (falha) =>
          this.erro.set(mensagemDeErro(falha, 'Não foi possível encerrar o plano.')),
      });
  }

  private abrir(detalhe: DetalhePlano) {
    this.router.navigate(['/core/plano-de-treino', detalhe.plano.identificador, 'visualizacao']);
  }
}
