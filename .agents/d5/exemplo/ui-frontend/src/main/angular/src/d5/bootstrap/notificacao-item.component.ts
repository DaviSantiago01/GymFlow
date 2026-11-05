import { Component, input, OnInit, output, viewChild } from '@angular/core';
import { NgbAlert, NgbAlertModule } from '@ng-bootstrap/ng-bootstrap';
import { Notificacao, TipoNotificacao } from '../notificacao';

const DURACAO_SUCESSO_MS = 10000

const TIPO_NGB_POR_TIPO: Record<TipoNotificacao, string> = {
  erro: 'danger',
  sucesso: 'success'
}

@Component({
  selector: 'app-notificacao-item',
  standalone: true,
  imports: [NgbAlertModule],
  template: `
    <ngb-alert class="d-block mb-2" [type]="tipoNgb" (closed)="fechar.emit()">
      {{ notificacao().mensagem }}
    </ngb-alert>
  `
})
export class NotificacaoItemComponent implements OnInit {
  readonly notificacao = input.required<Notificacao>()
  readonly fechar = output<void>()

  private readonly alerta = viewChild.required(NgbAlert)

  protected get tipoNgb() {
    return TIPO_NGB_POR_TIPO[this.notificacao().tipo]
  }

  ngOnInit() {
    if (this.notificacao().tipo === 'sucesso') {
      setTimeout(() => this.alerta().close(), DURACAO_SUCESSO_MS)
    }
  }
}
