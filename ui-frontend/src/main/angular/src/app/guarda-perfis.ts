import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { ServicoAutenticacao } from '../d5';

type Papel = 'ADMINISTRADOR' | 'PERSONAL' | 'USUARIO';

export function guardaPerfis(...papeis: Papel[]): CanActivateFn {
  return (_rota, estado) => {
    const autenticacao = inject(ServicoAutenticacao);
    const router = inject(Router);
    return autenticacao.verificarSeNecessario().pipe(
      map((autenticado) => {
        if (!autenticado) {
          return router.createUrlTree(['/login'], { queryParams: { retorno: estado.url } });
        }
        const papeisDaSessao = autenticacao.autorizacoes();
        return papeis.some((papel) => papeisDaSessao.includes(papel))
          ? true
          : router.createUrlTree(['/inicio']);
      }),
    );
  };
}
