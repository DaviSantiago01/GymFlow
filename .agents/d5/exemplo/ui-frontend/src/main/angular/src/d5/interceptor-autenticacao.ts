import { HttpClient, HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { ServicoAutenticacao } from './servico-autenticacao';
import { ServicoReautenticacao } from './servico-reautenticacao';

const NOME_COOKIE_CSRF = 'XSRF-TOKEN';
const NOME_HEADER_CSRF = 'X-XSRF-TOKEN';

function lerCookie(nome: string): string | null {
  const correspondencia = document.cookie.match(new RegExp(`(?:^|; )${nome}=([^;]*)`));
  return correspondencia ? decodeURIComponent(correspondencia[1]) : null;
}

/**
 * Clona sempre lendo o cookie CSRF na hora, em vez de confiar no header já anexado à requisição —
 * essencial na retentativa após reautenticação, quando o token pode ter mudado.
 */
function comCredenciaisAtuais(requisicao: HttpRequest<unknown>): HttpRequest<unknown> {
  const tokenCsrf = lerCookie(NOME_COOKIE_CSRF);
  return requisicao.clone({
    withCredentials: true,
    setHeaders: tokenCsrf ? { [NOME_HEADER_CSRF]: tokenCsrf } : {}
  });
}

export const interceptorAutenticacao: HttpInterceptorFn = (requisicao, proximo) => {
  const http = inject(HttpClient);
  const servicoAutenticacao = inject(ServicoAutenticacao);
  const servicoReautenticacao = inject(ServicoReautenticacao);
  const router = inject(Router);
  const ehBackend = requisicao.url.startsWith('backend/');
  const estavaAutenticado = servicoAutenticacao.autenticado();
  // Chamadas de login/reautenticação já carregam Authorization explícito e tratam seu próprio
  // erro (ServicoAutenticacao.entrar); nunca devem dar origem a um novo ciclo de reautenticação,
  // senão travam esperando o mesmo modal que já as originou.
  const ehTentativaDeLogin = requisicao.headers.has('Authorization');

  if (!ehBackend) {
    return proximo(requisicao);
  }

  return proximo(comCredenciaisAtuais(requisicao)).pipe(
    catchError(erro => {
      if (ehTentativaDeLogin || !estavaAutenticado || !(erro instanceof HttpErrorResponse) || erro.status !== 401) {
        return throwError(() => erro);
      }

      return servicoReautenticacao.solicitar().pipe(
        catchError(() => {
          // Reautenticação cancelada pelo usuário: aí sim desiste e vai para o login.
          servicoAutenticacao.sair().subscribe();
          router.navigateByUrl(`/login?retorno=${encodeURIComponent(router.url)}`);
          return throwError(() => erro);
        }),
        // O login troca o token CSRF, mas a resposta do login só apaga o antigo — o novo só é
        // efetivamente gravado numa requisição GET seguinte. Por isso, antes de repetir a
        // requisição original, forçamos essa renovação com uma chamada leve e idempotente.
        switchMap(() => http.get('backend/autenticacao', { withCredentials: true })),
        switchMap(() => proximo(comCredenciaisAtuais(requisicao)))
      );
    })
  );
};
