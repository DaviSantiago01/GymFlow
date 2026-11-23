import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize } from 'rxjs';
import { ServicoAutenticacao, ServicoNotificacao } from '../../d5';
import { data, mensagemDeErro } from '../formatacao';
import { CronogramaComponent } from './cronograma.component';
import {
  Adesao,
  AutorizacoesAdesao,
  PlanosService,
  ROTULOS_SITUACAO_ADESAO,
  VisualizacaoAdesao,
  duracao,
  moeda,
  recorrencia,
} from './planos.service';

@Component({
  selector: 'app-adesao',
  imports: [FormsModule, RouterLink, CronogramaComponent],
  templateUrl: './adesao.component.html',
  styleUrl: '../telas.scss',
})
export class AdesaoComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(PlanosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly autenticacao = inject(ServicoAutenticacao);
  private readonly destroy = inject(DestroyRef);
  readonly rotulosAdesao = ROTULOS_SITUACAO_ADESAO;
  readonly recorrencia = recorrencia;
  readonly moeda = moeda;
  readonly duracao = duracao;
  readonly data = data;
  readonly tela = signal<VisualizacaoAdesao | null>(null);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  readonly podeGerenciar = computed(
    () =>
      (this.autenticacao.autorizacoesDe<AutorizacoesAdesao>('adesaoPlano')?.trocar ?? false) &&
      this.tela()?.adesao.situacao === 'ATIVA',
  );
  readonly outrosPlanos = computed(() =>
    (this.tela()?.planos ?? []).filter((plano) => plano.identificador !== this.tela()?.adesao.plano),
  );
  novoPlano = '';
  quantidadePeriodos: number | null = null;

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => this.tela.set(dados['tela']));
  }

  trocar() {
    if (!this.novoPlano) {
      this.erro.set('Selecione o novo plano.');
      return;
    }
    if (window.confirm('A adesão atual será encerrada hoje e a nova começará em seguida. Confirmar a troca?')) {
      const troca = this.api.trocar(
        this.tela()!.adesao.identificador,
        this.novoPlano,
        this.quantidadePeriodos ?? null,
      );
      this.executar(troca, 'Plano trocado.');
    }
  }

  cancelar() {
    if (window.confirm('Cancelar o plano? O histórico e as mensalidades geradas serão preservados.')) {
      this.executar(this.api.cancelar(this.tela()!.adesao.identificador), 'Plano cancelado.');
    }
  }

  private executar(requisicao: Observable<Adesao>, sucesso: string) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (adesao) => {
          this.notificacoes.publicar('sucesso', sucesso);
          this.novoPlano = '';
          this.quantidadePeriodos = null;
          this.router.navigate(['/core/adesao-plano', adesao.identificador, 'visualizacao'], {
            onSameUrlNavigation: 'reload',
          });
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível concluir a operação.')),
      });
  }
}
