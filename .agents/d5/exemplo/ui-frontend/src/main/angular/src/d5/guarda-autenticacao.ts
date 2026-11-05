import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { ServicoAutenticacao } from './servico-autenticacao';

export const guardaAutenticacao: CanActivateFn = (_rota, estado) => {
  const servicoAutenticacao = inject(ServicoAutenticacao);
  const router = inject(Router);

  return servicoAutenticacao.verificarSeNecessario().pipe(
    map(autenticado => autenticado
      ? true
      : router.createUrlTree(['/login'], { queryParams: { retorno: estado.url } }))
  );
};
