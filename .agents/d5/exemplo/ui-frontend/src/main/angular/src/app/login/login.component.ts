import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServicoAutenticacao } from '../../d5';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly servicoAutenticacao = inject(ServicoAutenticacao);
  private readonly router = inject(Router);
  private readonly rota = inject(ActivatedRoute);

  protected usuario = '';
  protected senha = '';
  protected readonly entrando = signal(false);

  protected entrar() {
    this.entrando.set(true);

    this.servicoAutenticacao.entrar(this.usuario, this.senha).subscribe({
      next: () => {
        const urlRetorno = this.rota.snapshot.queryParamMap.get('retorno') ?? '/';
        // replaceUrl evita que /login fique no histórico do navegador — senão um "voltar" (seja o
        // botão do navegador ou Location.back() do próprio app) retornaria para a tela de login.
        this.router.navigateByUrl(urlRetorno, { replaceUrl: true });
      },
      error: () => this.entrando.set(false)
    });
  }
}
