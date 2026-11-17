import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize } from 'rxjs';
import { ServicoAutenticacao, ServicoNotificacao } from '../../d5';
import { data, dataHora, mensagemDeErro } from '../formatacao';
import {
  AutorizacoesMensalidade,
  DetalheMensalidade,
  FORMAS_PAGAMENTO,
  FormaPagamento,
  Pagamento,
  PlanosService,
  ROTULOS_FORMA_PAGAMENTO,
  ROTULOS_SITUACAO_MENSALIDADE,
  ROTULOS_SITUACAO_PAGAMENTO,
  moeda,
} from './planos.service';

@Component({
  selector: 'app-mensalidade',
  imports: [FormsModule, RouterLink],
  templateUrl: './mensalidade.component.html',
  styleUrl: '../telas.scss',
})
export class MensalidadeComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly api = inject(PlanosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly autenticacao = inject(ServicoAutenticacao);
  private readonly destroy = inject(DestroyRef);
  readonly rotulosMensalidade = ROTULOS_SITUACAO_MENSALIDADE;
  readonly rotulosPagamento = ROTULOS_SITUACAO_PAGAMENTO;
  readonly rotulosForma = ROTULOS_FORMA_PAGAMENTO;
  readonly formas = FORMAS_PAGAMENTO;
  readonly moeda = moeda;
  readonly data = data;
  readonly dataHora = dataHora;
  readonly tela = signal<DetalheMensalidade>(this.rota.snapshot.data['tela']);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  private readonly autorizacoes = computed(() =>
    this.autenticacao.autorizacoesDe<AutorizacoesMensalidade>('mensalidade'),
  );
  readonly podePagar = computed(
    () => (this.autorizacoes()?.registrarPagamento ?? false) && this.tela().mensalidade.situacao !== 'PAGA',
  );
  readonly podeEstornar = computed(() => this.autorizacoes()?.estornarPagamento ?? false);
  forma: FormaPagamento = 'PIX';

  pagar() {
    const valor = moeda(this.tela().mensalidade.valorDevido);
    if (window.confirm(`Registrar o pagamento de ${valor} em ${this.rotulosForma[this.forma]}?`)) {
      this.executar(this.api.pagar(this.tela().mensalidade.identificador, this.forma), 'Pagamento registrado.');
    }
  }

  estornar(pagamento: Pagamento) {
    const motivo = window.prompt('Informe o motivo do estorno. O registro do pagamento será preservado.');
    if (motivo === null) return;
    this.executar(
      this.api.estornar(this.tela().mensalidade.identificador, pagamento.identificador, motivo),
      'Pagamento estornado.',
    );
  }

  private executar(requisicao: Observable<DetalheMensalidade>, sucesso: string) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (detalhe) => {
          this.tela.set(detalhe);
          this.notificacoes.publicar('sucesso', sucesso, 0);
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível concluir a operação.')),
      });
  }
}
