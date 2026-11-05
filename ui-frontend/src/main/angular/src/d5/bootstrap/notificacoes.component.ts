import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ServicoNotificacao } from '../servico-notificacao';
import { NotificacaoItemComponent } from './notificacao-item.component';

@Component({
  selector: 'app-notificacoes',
  standalone: true,
  imports: [CommonModule, NotificacaoItemComponent],
  templateUrl: './notificacoes.component.html'
})
export class NotificacoesComponent {
  protected readonly servicoNotificacao = inject(ServicoNotificacao)
}
