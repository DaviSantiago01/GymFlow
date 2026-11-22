import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import { CATEGORIAS, Detalhe, RecomendacoesService, SITUACOES, data, diaCurto, numero } from './recomendacoes.service';

@Component({
  selector: 'app-recomendacao',
  imports: [FormsModule, RouterLink],
  templateUrl: './recomendacao.component.html',
  styleUrls: ['../analises.scss', '../analises-graficos.scss', '../analises-recomendacoes.scss'],
})
export class RecomendacaoComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(RecomendacoesService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);

  readonly detalhe = signal<Detalhe | null>(null);
  readonly respondendo = signal(false);
  readonly escolha = signal<'aceitar' | 'recusar'>('aceitar');
  readonly ocupado = signal(false);
  readonly categorias = CATEGORIAS;
  readonly situacoes = SITUACOES;
  readonly data = data;
  readonly numero = numero;
  readonly diaCurto = diaCurto;
  readonly motivos = ['Esse horário não funciona para mim', 'Já voltei a treinar com frequência', 'Não tenho interesse', 'Outro motivo'];
  motivo = this.motivos[0];

  readonly maior = computed(() => Math.max(1, ...(this.detalhe()?.evidencias ?? []).map((e) => e.valor)));
  readonly frequencia = computed(() => this.detalhe()?.recomendacao.criterio === 'REENGAJAMENTO');
  readonly previstos = computed(() => this.detalhe()?.aluno.plano?.frequenciaSemanal ?? 0);

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => this.detalhe.set(dados['detalhe']));
    this.rota.queryParamMap.pipe(takeUntilDestroyed()).subscribe((parametros) => {
      const pedido = parametros.get('responder');
      if (pedido === 'aceitar' || pedido === 'recusar') this.abrir(pedido);
    });
  }

  pendente(): boolean {
    return this.detalhe()?.recomendacao.situacao === 'PENDENTE';
  }

  abrir(escolha: 'aceitar' | 'recusar') {
    this.escolha.set(escolha);
    this.respondendo.set(true);
  }

  rotuloAceite(): string {
    switch (this.detalhe()?.recomendacao.criterio) {
      case 'AULA_COMPATIVEL':
        return 'Você será inscrito na aula.';
      case 'REAVALIACAO_FISICA':
        return 'A recepção entra em contato para agendar.';
      case 'REENGAJAMENTO':
        return this.detalhe()?.recomendacao.titulo.includes('personal')
          ? 'Seu personal será avisado para conversar com você.'
          : 'Vamos lembrar você no novo horário.';
      default:
        return 'Registramos seu interesse.';
    }
  }

  confirmar() {
    const detalhe = this.detalhe();
    if (!detalhe || this.ocupado()) return;
    this.ocupado.set(true);
    const id = detalhe.recomendacao.identificador;
    const pedido = this.escolha() === 'aceitar' ? this.api.aceitar(id) : this.api.recusar(id, this.motivo);
    pedido
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: () => {
          this.notificacoes.publicar('sucesso', this.escolha() === 'aceitar' ? 'Recomendação aceita.' : 'Recomendação recusada.');
          this.router.navigate(['/core/recomendacao/minhas']);
        },
        error: (falha) => this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível responder.')),
      });
  }
}
