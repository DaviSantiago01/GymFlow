import { Component, inject, signal } from '@angular/core';
import {
  NavigationEnd,
  NavigationError,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NotificacoesComponent } from '../d5/bootstrap/notificacoes.component';
import { ServicoAutenticacao, ServicoNotificacao } from '../d5';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NotificacoesComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly servicoAutenticacao = inject(ServicoAutenticacao);
  private readonly router = inject(Router);
  private readonly notificacoes = inject(ServicoNotificacao);
  protected readonly menuAberto = signal(false);

  constructor() {
    this.router.events.pipe(takeUntilDestroyed()).subscribe((evento) => {
      if (evento instanceof NavigationEnd) this.menuAberto.set(false);
      if (evento instanceof NavigationError) {
        this.notificacoes.publicar(
          'erro',
          evento.error?.error?.mensagem ??
            'Não foi possível carregar a tela. Verifique sua conexão e tente novamente.',
          0,
        );
      }
    });
  }

  protected sair() {
    this.menuAberto.set(false);
    this.servicoAutenticacao.sair().subscribe(() => this.router.navigateByUrl('/login'));
  }
}
