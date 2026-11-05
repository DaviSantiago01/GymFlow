import { Observable } from 'rxjs';

/**
 * Contrato agnóstico de UI: solicita ao usuário que reautentique sem navegar para outra página.
 * A implementação concreta (com o kit de UI escolhido pela aplicação) fica em d5/bootstrap.
 */
export abstract class ServicoReautenticacao {
  abstract solicitar(): Observable<void>;
}
