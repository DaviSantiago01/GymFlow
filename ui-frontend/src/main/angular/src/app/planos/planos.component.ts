import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoAutenticacao, ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import {
  AutorizacoesPlano,
  Plano,
  PlanosService,
  ROTULOS_SITUACAO_PLANO,
  duracao,
  moeda,
} from './planos.service';

@Component({
  selector: 'app-planos',
  imports: [FormsModule, RouterLink],
  templateUrl: './planos.component.html',
  styleUrl: '../telas.scss',
})
export class PlanosComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(PlanosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly autenticacao = inject(ServicoAutenticacao);
  private readonly destroy = inject(DestroyRef);
  readonly rotulosSituacao = ROTULOS_SITUACAO_PLANO;
  readonly moeda = moeda;
  readonly duracao = duracao;
  readonly planos = signal<Plano[]>([]);
  readonly ocupado = signal(false);
  readonly podeGerenciar = computed(
    () => this.autenticacao.autorizacoesDe<AutorizacoesPlano>('plano')?.cadastrar ?? false,
  );
  nome = '';
  situacao = '';

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.planos.set(dados['tela'].resultado.projecoes));
    this.rota.queryParamMap.pipe(takeUntilDestroyed()).subscribe((parametros) => {
      this.nome = parametros.get('nome') ?? '';
      this.situacao = parametros.get('situacao') ?? '';
    });
  }

  filtrar() {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { nome: this.nome.trim() || null, situacao: this.situacao || null },
    });
  }

  inativar(plano: Plano) {
    if (this.ocupado() || !window.confirm(`Inativar o plano ${plano.nome}? Ele não receberá novas adesões.`)) {
      return;
    }
    this.ocupado.set(true);
    this.api
      .inativarPlano(plano.identificador)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (inativado) => {
          this.planos.update((lista) =>
            lista.map((item) => (item.identificador === inativado.identificador ? inativado : item)),
          );
          this.notificacoes.publicar('sucesso', `Plano ${inativado.nome} inativado.`);
        },
        error: (falha) =>
          this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível inativar o plano.')),
      });
  }
}
