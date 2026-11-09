import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize } from 'rxjs';
import { mensagemDeErro } from '../formatacao';
import {
  AvaliacoesService,
  FormularioMeta,
  Indicador,
  Meta,
  MetaAgregado,
  MetaEnvio,
  ROTULOS_INDICADOR,
} from './avaliacoes.service';

@Component({
  selector: 'app-meta-formulario',
  imports: [FormsModule, RouterLink],
  templateUrl: './meta-formulario.component.html',
  styleUrl: '../telas.scss',
})
export class MetaFormularioComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(AvaliacoesService);
  private readonly destroy = inject(DestroyRef);
  readonly rotulos = ROTULOS_INDICADOR;
  readonly formulario = signal<FormularioMeta>(this.rota.snapshot.data['tela'].formulario);
  readonly meta = signal<MetaAgregado | null>(this.rota.snapshot.data['tela'].meta);
  readonly alteracao = computed(() => this.meta() !== null);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  alunoEscolhido = '';
  indicador: Indicador | '' = '';
  valorDesejado: number | null = null;
  inicio = '';
  fim = '';

  constructor() {
    const meta = this.meta();
    if (meta) {
      this.alunoEscolhido = meta.aluno;
      this.indicador = meta.indicador;
      this.valorDesejado = meta.valorDesejado;
      this.inicio = meta.inicio;
      this.fim = meta.fim;
    } else {
      this.alunoEscolhido = this.rota.snapshot.queryParamMap.get('aluno') ?? '';
    }
  }

  private montarEnvio(): MetaEnvio | null {
    if (this.valorDesejado === null || !this.inicio || !this.fim) {
      this.erro.set('Informe valor desejado, início e fim.');
      return null;
    }
    return { valorDesejado: this.valorDesejado, inicio: this.inicio, fim: this.fim };
  }

  criar() {
    const envio = this.montarEnvio();
    if (!envio) return;
    if (!this.alunoEscolhido || !this.indicador) {
      this.erro.set('Selecione o aluno e o indicador.');
      return;
    }
    this.enviar(this.api.criarMeta(this.alunoEscolhido, this.indicador, envio));
  }

  alterar(acao: 'ajustar' | 'substituir') {
    const envio = this.montarEnvio();
    if (envio) this.enviar(this.api.alterarMeta(this.meta()!.identificador, acao, envio));
  }

  encerrar() {
    if (window.confirm('Encerrar esta meta?')) {
      this.enviar(this.api.encerrarMeta(this.meta()!.identificador));
    }
  }

  private enviar(requisicao: Observable<Meta>) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (meta) =>
          this.router.navigate(['/core/meta/pesquisa'], { queryParams: { aluno: meta.aluno } }),
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível salvar a meta.')),
      });
  }
}
