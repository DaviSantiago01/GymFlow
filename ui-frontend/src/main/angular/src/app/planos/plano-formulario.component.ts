import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import { Plano, PlanosService } from './planos.service';

@Component({
  selector: 'app-plano-formulario',
  imports: [FormsModule, RouterLink],
  templateUrl: './plano-formulario.component.html',
  styleUrl: '../telas.scss',
})
export class PlanoFormularioComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(PlanosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);
  readonly plano = signal<Plano | null>(this.rota.snapshot.data['tela']);
  readonly alteracao = computed(() => this.plano() !== null);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  nome = this.plano()?.nome ?? '';
  valor: number | null = this.plano()?.valor ?? null;
  duracaoMeses: number | null = this.plano()?.duracaoMeses ?? 1;

  salvar() {
    if (this.ocupado()) return;
    const envio = { nome: this.nome, valor: this.valor, duracaoMeses: this.duracaoMeses };
    const plano = this.plano();
    const requisicao = plano
      ? this.api.editarPlano(plano.identificador, envio)
      : this.api.cadastrarPlano(envio);
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (salvo) => {
          this.notificacoes.publicar('sucesso', `Plano ${salvo.nome} salvo.`);
          this.router.navigate(['/core/plano/pesquisa']);
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível salvar o plano.')),
      });
  }
}
