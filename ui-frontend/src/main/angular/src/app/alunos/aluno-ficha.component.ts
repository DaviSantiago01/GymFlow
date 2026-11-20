import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize, switchMap } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { data, dataHora, mensagemDeErro } from '../formatacao';
import {
  AlunosService,
  FichaAluno,
  Frequencia,
  ROTULOS_ALTERACAO,
  ROTULOS_SITUACAO_ALUNO,
  duracao,
} from './alunos.service';

@Component({
  selector: 'app-aluno-ficha',
  imports: [FormsModule, RouterLink],
  templateUrl: './aluno-ficha.component.html',
  styleUrl: '../telas.scss',
})
export class AlunoFichaComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(AlunosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);
  readonly rotulosSituacao = ROTULOS_SITUACAO_ALUNO;
  readonly rotulosAlteracao = ROTULOS_ALTERACAO;
  readonly data = data;
  readonly dataHora = dataHora;
  readonly duracao = duracao;
  readonly ficha = signal<FichaAluno>(this.rota.snapshot.data['tela']);
  readonly frequencia = signal<Frequencia>(this.ficha().frequenciaRecente);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  motivo = '';
  inicio = this.ficha().frequenciaRecente.inicio;
  fim = this.ficha().frequenciaRecente.fim;

  private get id() {
    return this.ficha().identificador;
  }

  bloquear() {
    this.executar(this.api.bloquear(this.id, this.motivo), 'Aluno bloqueado.', () => (this.motivo = ''));
  }

  desbloquear() {
    this.executar(this.api.desbloquear(this.id), 'Bloqueio encerrado.');
  }

  reativar() {
    this.executar(this.api.reativar(this.id), 'Cadastro reativado.');
  }

  entrada() {
    this.executar(
      this.api.entrada(this.id).pipe(switchMap(() => this.api.ficha(this.id))),
      'Entrada registrada.',
    );
  }

  saida() {
    this.executar(this.api.saida(this.id).pipe(switchMap(() => this.api.ficha(this.id))), 'Saída registrada.');
  }

  remover() {
    if (!confirm(`Remover ${this.ficha().nome}? Se houver matrícula ou frequência, o cadastro será desativado.`)) return;
    this.ocupado.set(true);
    this.erro.set('');
    this.api
      .remover(this.id)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: ({ resultado }) => {
          if (resultado === 'EXCLUIDO') {
            this.notificacoes.publicar('sucesso', 'Aluno excluído definitivamente.');
            this.router.navigate(['/core/aluno/gestao']);
          } else {
            this.notificacoes.publicar('sucesso', 'O aluno possui registros e foi desativado.');
            this.recarregar();
          }
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível remover o aluno.')),
      });
  }

  consultarFrequencia() {
    this.erro.set('');
    this.api
      .frequencia(this.id, this.inicio || null, this.fim || null)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (frequencia) => this.frequencia.set(frequencia),
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível consultar a frequência.')),
      });
  }

  private recarregar() {
    this.api
      .ficha(this.id)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe((ficha) => this.atualizar(ficha));
  }

  private atualizar(ficha: FichaAluno) {
    this.ficha.set(ficha);
    this.frequencia.set(ficha.frequenciaRecente);
    this.inicio = ficha.frequenciaRecente.inicio;
    this.fim = ficha.frequenciaRecente.fim;
  }

  private executar(requisicao: Observable<FichaAluno>, mensagem: string, depois?: () => void) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (ficha) => {
          this.atualizar(ficha);
          this.notificacoes.publicar('sucesso', mensagem);
          depois?.();
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível concluir a operação.')),
      });
  }
}
