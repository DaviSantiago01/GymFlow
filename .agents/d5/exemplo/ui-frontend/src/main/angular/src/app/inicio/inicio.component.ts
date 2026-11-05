import { Component, inject } from '@angular/core';
import { ServicoAutenticacao } from '../../d5';

@Component({
  selector: 'app-inicio',
  imports: [],
  template: `
    <div class="container py-4">
      <h1 class="h4">Exemplo</h1>
      <p class="text-muted">Você está autenticado como <strong>{{ servicoAutenticacao.usuarioAtual() }}</strong>.</p>
    </div>
  `,
})
export class InicioComponent {
  protected readonly servicoAutenticacao = inject(ServicoAutenticacao);
}
