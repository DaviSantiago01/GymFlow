import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import {
  Agrupamento,
  FormularioRelatorio,
  Requisicao,
  RelatoriosService,
  Resumo,
  TIPOS,
  TipoRelatorio,
  data,
  diasEntre,
  numero,
  somarDias,
} from './relatorios.service';

type Atalho = 'ULTIMOS_30' | 'MES_ANTERIOR' | 'TRIMESTRE_ANTERIOR' | 'PERSONALIZADO';

@Component({
  selector: 'app-novo-relatorio',
  imports: [FormsModule, RouterLink],
  templateUrl: './novo-relatorio.component.html',
  styleUrls: ['../analises.scss', '../analises-graficos.scss'],
})
export class NovoRelatorioComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(RelatoriosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);

  readonly tipos = Object.keys(TIPOS) as TipoRelatorio[];
  readonly info = TIPOS;
  readonly data = data;
  readonly numero = numero;
  readonly formulario = signal<FormularioRelatorio | null>(null);
  readonly passo = signal<1 | 2>(1);
  readonly tipo = signal<TipoRelatorio>('DESEMPENHO_GERAL');
  readonly resumo = signal<Resumo | null>(null);
  readonly aviso = signal<string | null>(null);
  readonly ocupado = signal(false);

  atalho: Atalho = 'MES_ANTERIOR';
  inicio = '';
  fim = '';
  agrupamento: Agrupamento = 'SEMANA';
  plano = '';
  modalidade = '';
  personal = '';
  aula = '';
  comparar = true;

  readonly permitidos = computed(() => new Set(this.formulario()?.tipos ?? []));

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => {
      const formulario = dados['formulario'] as FormularioRelatorio;
      this.formulario.set(formulario);
      const permitido = formulario.tipos[0];
      if (permitido) this.tipo.set(permitido);
      if (formulario.personalRestrito && formulario.personais[0]) this.personal = formulario.personais[0].identificador;
      this.aplicarAtalho('MES_ANTERIOR');
    });
  }

  escolher(tipo: TipoRelatorio) {
    if (this.permitidos().has(tipo)) this.tipo.set(tipo);
  }

  continuar() {
    this.passo.set(2);
    this.verificar();
  }

  aceita(filtro: 'PLANO' | 'MODALIDADE' | 'PERSONAL' | 'AULA_COLETIVA') {
    return this.info[this.tipo()].filtros.includes(filtro);
  }

  aplicarAtalho(atalho: Atalho) {
    this.atalho = atalho;
    const hoje = this.formulario()?.hoje ?? new Date().toISOString().substring(0, 10);
    const [ano, mes] = hoje.split('-').map(Number);
    if (atalho === 'ULTIMOS_30') {
      this.inicio = somarDias(hoje, -29);
      this.fim = hoje;
    } else if (atalho === 'MES_ANTERIOR') {
      const primeiro = new Date(ano, mes - 2, 1);
      const ultimo = new Date(ano, mes - 1, 0);
      this.inicio = isoDe(primeiro);
      this.fim = isoDe(ultimo);
    } else if (atalho === 'TRIMESTRE_ANTERIOR') {
      const trimestre = Math.floor((mes - 1) / 3);
      this.inicio = isoDe(new Date(ano, trimestre * 3 - 3, 1));
      this.fim = isoDe(new Date(ano, trimestre * 3, 0));
    }
    this.ajustarAgrupamento();
    this.verificar();
  }

  dias(): number {
    return this.inicio && this.fim ? diasEntre(this.inicio, this.fim) : 0;
  }

  /** RN004: o agrupamento não pode ser maior que o período, e o diário só vale até o limite da academia. */
  agrupamentoPermitido(agrupamento: Agrupamento): boolean {
    const dias = this.dias();
    if (agrupamento === 'DIA') return dias >= 1 && dias <= (this.formulario()?.agrupamentoDiarioMaximoDias ?? 90);
    if (agrupamento === 'SEMANA') return dias >= 7;
    return dias >= 28;
  }

  private ajustarAgrupamento() {
    if (this.agrupamentoPermitido(this.agrupamento)) return;
    this.agrupamento = (['SEMANA', 'DIA', 'MES'] as Agrupamento[]).find((opcao) => this.agrupamentoPermitido(opcao)) ?? 'DIA';
  }

  quantidadeDeGrupos(): number {
    const dias = this.dias();
    if (this.agrupamento === 'DIA') return dias;
    if (this.agrupamento === 'SEMANA') return Math.ceil(dias / 7);
    if (!this.inicio || !this.fim) return 0;
    const [a1, m1] = this.inicio.split('-').map(Number);
    const [a2, m2] = this.fim.split('-').map(Number);
    return (a2 - a1) * 12 + (m2 - m1) + 1;
  }

  periodoAnterior(): string {
    const dias = this.dias();
    if (!this.inicio || dias <= 0) return '—';
    return `${data(somarDias(this.inicio, -dias))} a ${data(somarDias(this.inicio, -1))}`;
  }

  alterarPeriodo() {
    this.atalho = 'PERSONALIZADO';
    this.ajustarAgrupamento();
    this.verificar();
  }

  private requisicao(): Requisicao {
    return {
      tipo: this.tipo(),
      inicio: this.inicio,
      fim: this.fim,
      agrupamento: this.agrupamento,
      plano: this.aceita('PLANO') ? this.plano || null : null,
      modalidade: this.aceita('MODALIDADE') ? this.modalidade || null : null,
      personal: this.aceita('PERSONAL') ? this.personal || null : null,
      aula: this.aceita('AULA_COLETIVA') ? this.aula || null : null,
      comparar: this.comparar,
    };
  }

  /** RN002 e RN003: confere período, filtros e se existem dados antes de gerar. */
  verificar() {
    if (this.passo() !== 2 || !this.inicio || !this.fim) return;
    this.api
      .verificar(this.requisicao())
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (resumo) => {
          this.resumo.set(resumo);
          this.aviso.set(null);
        },
        error: (falha) => {
          this.resumo.set(null);
          this.aviso.set(mensagemDeErro(falha, 'Não foi possível verificar os dados do período.'));
        },
      });
  }

  gerar() {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.api
      .gerar(this.requisicao())
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (relatorio) => {
          this.notificacoes.publicar('sucesso', 'Relatório gerado e guardado no histórico.');
          this.router.navigate(['/core/relatorio', relatorio.identificador, 'visualizacao']);
        },
        error: (falha) => this.aviso.set(mensagemDeErro(falha, 'Não foi possível gerar o relatório.')),
      });
  }
}

function isoDe(valor: Date): string {
  return `${valor.getFullYear()}-${String(valor.getMonth() + 1).padStart(2, '0')}-${String(valor.getDate()).padStart(2, '0')}`;
}
