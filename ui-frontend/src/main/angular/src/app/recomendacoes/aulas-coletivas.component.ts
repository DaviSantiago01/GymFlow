import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import { Aula, Aulas, DIAS, RecomendacoesService, diaCurto, hora } from './recomendacoes.service';

@Component({
  selector: 'app-aulas-coletivas',
  imports: [RouterLink],
  templateUrl: './aulas-coletivas.component.html',
  styleUrls: ['../analises.scss', '../analises-recomendacoes.scss'],
})
export class AulasColetivasComponent {
  private readonly api = inject(RecomendacoesService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);

  readonly aulas = signal<Aulas | null>(null);
  readonly aba = signal<'recomendadas' | 'todas' | 'inscricoes'>('recomendadas');
  readonly ocupado = signal(false);
  readonly hora = hora;
  readonly diaCurto = diaCurto;

  constructor() {
    this.carregar();
  }

  carregar() {
    this.api
      .aulasParaMim()
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({ next: (aulas) => this.aulas.set(aulas) });
  }

  diaPlural(aula: Aula): string {
    return DIAS.find((dia) => dia.valor === aula.dia)?.plural ?? '';
  }

  /** A inscrição pela recomendação registra o aceite; sem recomendação, inscreve direto. */
  inscrever(aula: Aula) {
    const pedido: Observable<unknown> = aula.recomendacao ? this.api.aceitar(aula.recomendacao) : this.api.inscrever(aula.identificador);
    this.executar(pedido, `Inscrição feita em ${aula.nome}.`);
  }

  recusar(aula: Aula) {
    if (!aula.recomendacao) return;
    this.executar(this.api.recusar(aula.recomendacao, 'Não tenho interesse'), 'Recomendação recusada.');
  }

  private executar(pedido: Observable<unknown>, mensagem: string) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    pedido
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: () => {
          this.notificacoes.publicar('sucesso', mensagem);
          this.carregar();
        },
        error: (falha) => this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível concluir.')),
      });
  }
}
