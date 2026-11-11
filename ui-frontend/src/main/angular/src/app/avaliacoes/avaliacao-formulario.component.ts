import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { mensagemDeErro } from '../formatacao';
import {
  AvaliacaoAgregado,
  AvaliacaoEnvio,
  AvaliacoesService,
  FormularioAvaliacao,
  Indicador,
  MedidaCorporal,
  ROTULOS_INDICADOR,
} from './avaliacoes.service';

interface CampoMedida {
  indicador: Indicador;
  valor: number | null;
}

@Component({
  selector: 'app-avaliacao-formulario',
  imports: [FormsModule, RouterLink],
  templateUrl: './avaliacao-formulario.component.html',
  styleUrl: '../telas.scss',
})
export class AvaliacaoFormularioComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(AvaliacoesService);
  private readonly destroy = inject(DestroyRef);
  readonly rotulos = ROTULOS_INDICADOR;
  readonly formulario = signal<FormularioAvaliacao>(this.rota.snapshot.data['tela'].formulario);
  readonly original = signal<AvaliacaoAgregado | null>(this.rota.snapshot.data['tela'].avaliacao);
  readonly correcao = computed(() => this.original() !== null);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  readonly medidas: CampoMedida[];
  alunoEscolhido = '';
  data = '';
  peso: number | null = null;
  altura: number | null = null;

  constructor() {
    const original = this.original();
    this.medidas = this.formulario().medidas.map((indicador) => ({
      indicador,
      valor: original?.medidas.find((medida) => medida.indicador === indicador)?.valor ?? null,
    }));
    if (original) {
      this.alunoEscolhido = original.aluno;
      this.data = original.data;
      this.peso = original.peso;
      this.altura = original.altura;
    } else {
      this.alunoEscolhido = this.rota.snapshot.queryParamMap.get('aluno') ?? '';
    }
  }

  private montarEnvio(): AvaliacaoEnvio {
    const medidas: MedidaCorporal[] = [];
    for (const campo of this.medidas) {
      if (campo.valor !== null) medidas.push({ indicador: campo.indicador, valor: campo.valor });
    }
    return { data: this.data, peso: this.peso!, altura: this.altura!, medidas };
  }

  salvar() {
    if (this.ocupado()) return;
    if (!this.alunoEscolhido || !this.data || this.peso === null || this.altura === null) {
      this.erro.set('Informe aluno, data, peso e altura.');
      return;
    }
    const original = this.original();
    const requisicao = original
      ? this.api.corrigir(original.identificador, this.montarEnvio())
      : this.api.registrar(this.alunoEscolhido, this.montarEnvio());
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (detalhe) =>
          this.router.navigate([
            '/core/avaliacao-fisica',
            detalhe.avaliacao.identificador,
            'visualizacao',
          ]),
        error: (falha) =>
          this.erro.set(mensagemDeErro(falha, 'Não foi possível salvar a avaliação.')),
      });
  }
}
