import {
  ApplicationConfig,
  LOCALE_ID,
  importProvidersFrom,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import portugues from '@angular/common/locales/pt';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { routes } from './app.routes';
import { interceptorAutenticacao, ServicoReautenticacao } from '../d5';
import { ServicoReautenticacaoBootstrap } from '../d5/bootstrap/servico-reautenticacao-bootstrap';

registerLocaleData(portugues);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withInMemoryScrolling({ scrollPositionRestoration: 'top' })),
    { provide: LOCALE_ID, useValue: 'pt-BR' },
    provideHttpClient(withInterceptors([interceptorAutenticacao])),
    importProvidersFrom(NgbModule),
    { provide: ServicoReautenticacao, useClass: ServicoReautenticacaoBootstrap },
  ],
};
