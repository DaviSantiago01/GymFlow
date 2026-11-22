import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import {
  ItemHistorico,
  Pesquisa,
  Relatorio,
  RelatoriosService,
  TIPOS,
  TipoRelatorio,
  data,
  dataHora,
  salvarArquivo,
  valorIndicador,
  INDICADORES,
} from './relatorios.service';

@Component({
  selector: 'app-historico-relatorios',
  imports: [FormsModule, RouterLink],
  templateUrl: './historico-relatorios.component.html',
  styleUrls: ['../analises.scss', '../analises-graficos.scss'],
})
export class HistoricoRelatoriosComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(RelatoriosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);

  readonly itens = signal<ItemHistorico[]>([]);
  readonly selecionado = signal<Relatorio | null>(null);
  readonly ocupado = signal(false);
  readonly tipos = Object.keys(TIPOS) as TipoRelatorio[];
  readonly info = TIPOS;
  readonly indicadores = INDICADORES;
  readonly data = data;
  readonly dataHora = dataHora;
  readonly valorIndicador = valorIndicador;
  readonly usuarios = computed(() => {
    const mapa = new Map<string, string>();
    for (const item of this.itens()) mapa.set(item.usuario, item.usuarioNome);
    return [...mapa.entries()];
  });

  termo = '';
  tipo = '';
  usuario = '';
  dias = '60';

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => {
      const pesquisa = dados['historico'] as Pesquisa<ItemHistorico>;
      this.itens.set(pesquisa.resultado.projecoes);
      const primeiro = pesquisa.resultado.projecoes[0];
      if (primeiro) this.selecionar(primeiro);
      else this.selecionado.set(null);
    });
    this.rota.queryParamMap.pipe(takeUntilDestroyed()).subscribe((parametros) => {
      this.termo = parametros.get('termo') ?? '';
      this.tipo = parametros.get('tipo') ?? '';
      this.usuario = parametros.get('usuario') ?? '';
      this.dias = parametros.get('dias') ?? '60';
    });
  }

  filtrar() {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: {
        termo: this.termo || null,
        tipo: this.tipo || null,
        usuario: this.usuario || null,
        dias: this.dias === '60' ? null : this.dias,
      },
    });
  }

  selecionar(item: ItemHistorico) {
    this.api
      .visualizar(item.identificador)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({ next: (relatorio) => this.selecionado.set(relatorio) });
  }

  novaVersao() {
    const relatorio = this.selecionado();
    if (!relatorio || this.ocupado()) return;
    this.ocupado.set(true);
    this.api
      .novaVersao(relatorio.identificador)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (nova) => {
          this.notificacoes.publicar('sucesso', `Versão ${nova.versao} gerada. A anterior foi mantida com os valores originais.`);
          this.router.navigate([], { relativeTo: this.rota, queryParamsHandling: 'preserve', onSameUrlNavigation: 'reload' });
        },
        error: (falha) => this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível gerar a nova versão.')),
      });
  }

  exportar(formato: 'PDF' | 'PLANILHA') {
    const relatorio = this.selecionado();
    if (!relatorio) return;
    this.api
      .arquivo(relatorio.identificador, formato)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (resposta) => {
          const nome = /filename="?([^";]+)"?/.exec(resposta.headers.get('Content-Disposition') ?? '')?.[1] ?? 'relatorio';
          salvarArquivo(resposta.body!, decodeURIComponent(nome));
        },
        error: (falha) => this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível exportar.')),
      });
  }
}
