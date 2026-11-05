import { inject, Injectable } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { catchError, finalize, from, Observable, share, throwError } from 'rxjs';
import { ServicoReautenticacao } from '../servico-reautenticacao';
import { ReautenticacaoModalComponent } from './reautenticacao-modal.component';

@Injectable()
export class ServicoReautenticacaoBootstrap extends ServicoReautenticacao {
  private readonly modalService = inject(NgbModal);
  private modalEmAndamento: Observable<void> | null = null;

  /**
   * Garante um único modal por vez: chamadas concorrentes (várias requisições falhando com
   * sessão expirada ao mesmo tempo) compartilham o mesmo resultado em vez de abrir um modal cada.
   */
  solicitar(): Observable<void> {
    if (!this.modalEmAndamento) {
      const referenciaModal = this.modalService.open(ReautenticacaoModalComponent, {
        backdrop: 'static',
        keyboard: false
      });

      this.modalEmAndamento = from(referenciaModal.result).pipe(
        catchError(() => throwError(() => new Error('Reautenticação cancelada.'))),
        finalize(() => this.modalEmAndamento = null),
        share()
      );
    }

    return this.modalEmAndamento;
  }
}
