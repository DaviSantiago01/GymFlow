import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of, tap, throwError } from 'rxjs';
import { ServicoNotificacao } from './servico-notificacao';

type EstadoAutenticacao = 'desconhecido' | 'autenticado' | 'anonimo';

export interface IAgregadoAutorizacoes {
  incluir: boolean;
  buscar: boolean;
  alterar: boolean;
  excluir: boolean;
}

/**
 * Espelha o JSON devolvido pela subclasse de AutenticacaoControladorAbstrato do backend: o nome do
 * usuário, as authorities (opcional) e, por agregado exposto na UI, um objeto de autorizações
 * (consulte-o com `autorizacoesDe`).
 */
interface Usuario {
  usuario: string;
  autorizacoes?: string[];
  [agregado: string]: unknown;
}

function cabecalhoBasic(usuario: string, senha: string): string {
  return `Basic ${btoa(`${usuario}:${senha}`)}`;
}

export function mensagemErroAutenticacao(erro: unknown): string {
  return erro instanceof HttpErrorResponse && erro.status === 401
    ? 'Usuário ou senha inválidos.'
    : 'Não foi possível entrar. Tente novamente.';
}

@Injectable({ providedIn: 'root' })
export class ServicoAutenticacao {
  private readonly http = inject(HttpClient);
  private readonly servicoNotificacao = inject(ServicoNotificacao);
  private readonly estadoSignal = signal<EstadoAutenticacao>('desconhecido');
  private readonly usuarioSignal = signal<Usuario | null>(null);

  autenticado(): boolean {
    return this.estadoSignal() === 'autenticado';
  }

  autorizacoes(): string[] {
    return this.usuarioSignal()?.autorizacoes ?? [];
  }

  /** Autorizações do agregado (ex.: `autorizacoesDe<IAgregadoAutorizacoes>('agregado')`). */
  autorizacoesDe<T extends IAgregadoAutorizacoes = IAgregadoAutorizacoes>(agregado: string): T | undefined {
    return this.usuarioSignal()?.[agregado] as T | undefined;
  }

  usuarioAtual(): string | null {
    return this.usuarioSignal()?.usuario ?? null;
  }

  /** Confirma com o backend se a sessão (cookie) ainda é válida; só chama a rede uma vez por carregamento. */
  verificarSeNecessario(): Observable<boolean> {
    if (this.estadoSignal() !== 'desconhecido') {
      return of(this.autenticado());
    }

    return this.http.get<Usuario>('backend/autenticacao', { withCredentials: true }).pipe(
      tap(resposta => this.definirAutenticado(resposta)),
      map(() => true),
      catchError(() => {
        this.definirAnonimo();
        return of(false);
      })
    );
  }

  /**
   * `notificarGlobalmente` publica o erro na área de notificações da aplicação; desative quando o
   * chamador já exibe a mensagem no próprio local (ex.: o modal de reautenticação).
   */
  entrar(usuario: string, senha: string, opcoes: { notificarGlobalmente?: boolean } = {}): Observable<void> {
    const { notificarGlobalmente = true } = opcoes;

    return this.http.get<Usuario>('backend/autenticacao', {
      headers: { Authorization: cabecalhoBasic(usuario, senha) },
      withCredentials: true
    }).pipe(
      tap(resposta => this.definirAutenticado(resposta)),
      map(() => undefined),
      catchError(erro => {
        if (notificarGlobalmente) {
          this.servicoNotificacao.publicar('erro', mensagemErroAutenticacao(erro), 0);
        }
        return throwError(() => erro);
      })
    );
  }

  sair(): Observable<void> {
    return this.http.post('backend/sair', null, { withCredentials: true, observe: 'response' }).pipe(
      map(() => undefined),
      catchError(() => of(undefined)),
      tap(() => this.definirAnonimo())
    );
  }

  private definirAutenticado(usuario: Usuario) {
    this.usuarioSignal.set(usuario);
    this.estadoSignal.set('autenticado');
  }

  private definirAnonimo() {
    this.usuarioSignal.set(null);
    this.estadoSignal.set('anonimo');
  }
}
