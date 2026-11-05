import { ApplicationConfig, importProvidersFrom, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { routes } from './app.routes';
import { interceptorAutenticacao, ServicoReautenticacao } from '../d5';
import { ServicoReautenticacaoBootstrap } from '../d5/bootstrap/servico-reautenticacao-bootstrap';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([interceptorAutenticacao])),
    importProvidersFrom(NgbModule),
    { provide: ServicoReautenticacao, useClass: ServicoReautenticacaoBootstrap }
  ]
};
