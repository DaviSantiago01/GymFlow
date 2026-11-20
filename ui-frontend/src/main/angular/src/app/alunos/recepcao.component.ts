import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { dataHora, mensagemDeErro } from '../formatacao';
import {
  AlunoResumo,
  AlunosService,
  PainelPresenca,
  ROTULOS_SITUACAO_ALUNO,
  cpfFormatado,
  duracao,
} from './alunos.service';

/** Recepção (F006): busca o aluno, registra entrada e saída e mostra quem está na academia. */
@Component({
  selector: 'app-recepcao',
  imports: [FormsModule, RouterLink],
  templateUrl: './recepcao.component.html',
  styleUrl: '../telas.scss',
})
export class RecepcaoComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly api = inject(AlunosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);
  readonly rotulos = ROTULOS_SITUACAO_ALUNO;
  readonly cpfFormatado = cpfFormatado;
  readonly dataHora = dataHora;
  readonly duracao = duracao;
  readonly painel = signal<PainelPresenca>(this.rota.snapshot.data['tela']);
  readonly resultados = signal<AlunoResumo[]>([]);
  readonly pesquisou = signal(false);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  readonly lotacao = computed(() =>
    Math.min(100, Math.round((this.painel().presentes / this.painel().capacidadeMaxima) * 100)),
  );
  readonly presentes = computed(() => new Set(this.painel().registros.map((item) => item.registro.aluno)));
  busca = '';

  pesquisar() {
    const termo = this.busca.trim();
    if (!termo) return;
    this.erro.set('');
    this.api
      .listar(termo, null)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (alunos) => {
          this.resultados.set(alunos);
          this.pesquisou.set(true);
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível pesquisar.')),
      });
  }

  entrada(aluno: { identificador: string; nome: string }) {
    this.executar(this.api.entrada(aluno.identificador), `Entrada de ${aluno.nome} registrada.`);
  }

  saida(aluno: { identificador: string; nome: string }) {
    this.executar(this.api.saida(aluno.identificador), `Saída de ${aluno.nome} registrada.`);
  }

  atualizar() {
    this.api
      .presentes()
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe((painel) => this.painel.set(painel));
  }

  private executar(requisicao: Observable<PainelPresenca>, mensagem: string) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (painel) => {
          this.painel.set(painel);
          this.notificacoes.publicar('sucesso', mensagem);
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível registrar o acesso.')),
      });
  }
}
