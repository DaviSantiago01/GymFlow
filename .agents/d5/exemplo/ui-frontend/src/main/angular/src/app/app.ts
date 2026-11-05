import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { NotificacoesComponent } from '../d5/bootstrap/notificacoes.component';
import { ServicoAutenticacao } from '../d5';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, NotificacoesComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly servicoAutenticacao = inject(ServicoAutenticacao);
  private readonly router = inject(Router);

  protected sair() {
    this.servicoAutenticacao.sair().subscribe(() => this.router.navigateByUrl('/login'));
  }
}
