import { computed, inject, Injectable, signal } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { Notificacao, TipoNotificacao } from './notificacao';

interface NotificacaoInterna {
  notificacao: Notificacao
  restante: number
}

@Injectable({ providedIn: 'root' })
export class ServicoNotificacao {
  private readonly _notificacoes = signal<NotificacaoInterna[]>([])
  private proximoId = 0

  readonly notificacoes = computed(() => this._notificacoes().map(interna => interna.notificacao))

  constructor() {
    inject(Router).events
      .pipe(filter(evento => evento instanceof NavigationEnd))
      .subscribe(() => this.avancarNavegacao())
  }

  publicar(tipo: TipoNotificacao, mensagem: string, preservarPorNavegacoes = 1) {
    const notificacao: Notificacao = { id: this.proximoId++, tipo, mensagem }
    this._notificacoes.update(atuais => [...atuais, { notificacao, restante: preservarPorNavegacoes }])
  }

  remover(id: number) {
    this._notificacoes.update(atuais => atuais.filter(interna => interna.notificacao.id !== id))
  }

  limparTodas() {
    this._notificacoes.set([])
  }

  private avancarNavegacao() {
    this._notificacoes.update(atuais => atuais
      .map(interna => ({ ...interna, restante: interna.restante - 1 }))
      .filter(interna => interna.restante >= 0))
  }
}
