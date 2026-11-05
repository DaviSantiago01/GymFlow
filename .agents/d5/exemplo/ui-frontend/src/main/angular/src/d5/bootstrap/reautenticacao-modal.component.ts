import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { mensagemErroAutenticacao, ServicoAutenticacao } from '../servico-autenticacao';

@Component({
  selector: 'd5-reautenticacao-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './reautenticacao-modal.component.html'
})
export class ReautenticacaoModalComponent {
  private readonly servicoAutenticacao = inject(ServicoAutenticacao);
  protected readonly modal = inject(NgbActiveModal);

  // Capturado na abertura do modal, antes de qualquer nova tentativa de login — usado para
  // impedir que outra pessoa reautentique com sua própria conta e submeta, sem perceber, os
  // dados que o usuário original deixou preenchidos no formulário.
  private readonly usuarioAnterior = this.servicoAutenticacao.usuarioAtual();

  protected usuario = '';
  protected senha = '';
  protected readonly entrando = signal(false);
  protected readonly mensagemErro = signal<string | null>(null);

  protected entrar() {
    this.entrando.set(true);
    this.mensagemErro.set(null);

    this.servicoAutenticacao.entrar(this.usuario, this.senha, { notificarGlobalmente: false }).subscribe({
      next: () => {
        if (this.usuarioAnterior && this.servicoAutenticacao.usuarioAtual() !== this.usuarioAnterior) {
          this.entrando.set(false);
          this.mensagemErro.set(`Entre com o mesmo usuário que estava usando (${this.usuarioAnterior}).`);
          this.servicoAutenticacao.sair().subscribe();
          return;
        }

        this.modal.close();
      },
      error: erro => {
        this.entrando.set(false);
        this.mensagemErro.set(mensagemErroAutenticacao(erro));
      }
    });
  }
}
