import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ServicoAutenticacao } from '../../d5';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.scss',
})
export class InicioComponent {
  protected readonly autenticacao = inject(ServicoAutenticacao);
}
