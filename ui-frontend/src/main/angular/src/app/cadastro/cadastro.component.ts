import { Component, DestroyRef, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-cadastro',
  imports: [FormsModule, RouterLink],
  templateUrl: './cadastro.component.html',
  styleUrl: './cadastro.component.scss',
})
export class CadastroComponent {
  private readonly http = inject(HttpClient);
  private readonly destroy = inject(DestroyRef);
  nome = '';
  usuario = '';
  senha = '';
  confirmacaoSenha = '';
  readonly pronto = signal(false);
  readonly enviando = signal(false);
  readonly erro = signal('');
  readonly criado = signal('');

  constructor() {
    this.preparar();
  }

  preparar() {
    this.erro.set('');
    this.http
      .get('/backend/cadastro')
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: () => this.pronto.set(true),
        error: () => this.erro.set('Não foi possível conectar ao servidor. Tente novamente.'),
      });
  }

  cadastrar() {
    if (!this.pronto() || this.enviando()) return;
    this.erro.set('');
    if (this.senha !== this.confirmacaoSenha) {
      this.erro.set('As senhas não conferem.');
      return;
    }
    if (new TextEncoder().encode(this.senha).length > 72) {
      this.erro.set(
        'A senha é muito longa. Use no máximo 72 bytes; caracteres acentuados ocupam mais de um byte.',
      );
      return;
    }
    this.enviando.set(true);
    this.http
      .post<{ usuario: string }>('/backend/cadastro', {
        nome: this.nome,
        usuario: this.usuario,
        senha: this.senha,
        confirmacaoSenha: this.confirmacaoSenha,
      })
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.enviando.set(false)),
      )
      .subscribe({
        next: (resposta) => {
          this.senha = '';
          this.confirmacaoSenha = '';
          this.criado.set(resposta.usuario);
        },
        error: (falha) => {
          this.erro.set(
            falha.error?.mensagem ?? 'Não foi possível criar a conta. Tente novamente.',
          );
          if (falha.status === 403) {
            this.pronto.set(false);
          }
        },
      });
  }
}
