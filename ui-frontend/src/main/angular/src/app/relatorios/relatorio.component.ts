import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import {
  CelulaOcupacao,
  DIAS,
  DiaSemana,
  Exportacao,
  INDICADORES,
  Relatorio,
  RelatoriosService,
  ValorIndicador,
  data,
  dataHora,
  decimal,
  diaCurto,
  hora,
  moeda,
  moedaCurta,
  numero,
  percentual,
  salvarArquivo,
  valorIndicador,
  variacaoCurta,
} from './relatorios.service';

@Component({
  selector: 'app-relatorio',
  imports: [RouterLink],
  templateUrl: './relatorio.component.html',
  styleUrls: ['../analises.scss', '../analises-graficos.scss'],
})
export class RelatorioComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(RelatoriosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);

  readonly relatorio = signal<Relatorio | null>(null);
  readonly exportando = signal(false);
  readonly formato = signal<'PDF' | 'PLANILHA'>('PDF');
  readonly previa = signal<Exportacao | null>(null);
  readonly ocupado = signal(false);

  readonly indicadores = INDICADORES;
  readonly dias = DIAS;
  readonly data = data;
  readonly dataHora = dataHora;
  readonly numero = numero;
  readonly decimal = decimal;
  readonly percentual = percentual;
  readonly moeda = moeda;
  readonly moedaCurta = moedaCurta;
  readonly hora = hora;
  readonly diaCurto = diaCurto;
  readonly valorIndicador = valorIndicador;
  readonly variacaoCurta = variacaoCurta;

  readonly conteudo = computed(() => this.relatorio()?.conteudo);
  readonly faixas = computed(() => {
    const sala = this.conteudo()?.ocupacao?.sala ?? [];
    return [...new Set(sala.map((celula) => celula.inicio))].sort();
  });
  readonly picos = computed(() => (this.conteudo()?.ocupacao?.sala ?? []).filter((c) => c.classificacao === 'PICO'));
  readonly menorMovimento = computed(() => {
    const abertas = (this.conteudo()?.ocupacao?.sala ?? []).filter((c) => c.classificacao !== 'FECHADO');
    return abertas.reduce<CelulaOcupacao | null>((menor, c) => (!menor || c.percentual < menor.percentual ? c : menor), null);
  });
  readonly maiorFrequencia = computed(() => Math.max(1, ...(this.conteudo()?.grupos ?? []).map((g) => g.frequencia)));
  readonly maiorReceita = computed(() => Math.max(1, ...(this.conteudo()?.grupos ?? []).map((g) => g.receita)));
  readonly maiorAtraso = computed(() =>
    Math.max(1, ...(this.conteudo()?.financeiro?.faixasAtraso ?? []).map((f) => f.valor)),
  );
  readonly adesaoMedia = computed(() => {
    const personais = this.conteudo()?.personais ?? [];
    const previstos = personais.reduce((soma, p) => soma + p.diasPrevistos, 0);
    const realizados = personais.reduce((soma, p) => soma + Math.min(p.diasComTreino, p.diasPrevistos), 0);
    return previstos === 0 ? 0 : (realizados * 100) / previstos;
  });
  readonly totalMetas = computed(() => (this.conteudo()?.personais ?? []).reduce((soma, p) => soma + p.metasAtingidas, 0));
  readonly semEntrada = computed(
    () => (this.conteudo()?.alunosEmRisco ?? []).filter((aluno) => aluno.motivo === 'SEM_ENTRADA').length,
  );
  readonly movimentoBarras = computed(() => {
    const movimento = this.conteudo()?.movimento;
    if (!movimento) return [];
    const itens = [
      { rotulo: 'Novas matrículas', valor: movimento.novasMatriculas, tipo: 'entrada' },
      { rotulo: 'Reativações', valor: movimento.reativacoes, tipo: 'entrada' },
      { rotulo: 'Evasões', valor: -movimento.evasoes, tipo: 'saida' },
      { rotulo: 'Trancamentos (saldo)', valor: -movimento.saldoTrancamentos, tipo: 'saida' },
      { rotulo: 'Saldo do período', valor: movimento.fim - movimento.inicio, tipo: 'saldo' },
    ];
    const maior = Math.max(1, ...itens.map((item) => Math.abs(item.valor)));
    return itens.map((item) => ({ ...item, altura: (Math.abs(item.valor) / maior) * 100 }));
  });

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => this.relatorio.set(dados['relatorio']));
  }

  contato(aluno: string) {
    return this.relatorio()?.contatos.find((item) => item.aluno === aluno);
  }

  celula(dia: DiaSemana, faixa: string): CelulaOcupacao | undefined {
    return this.conteudo()?.ocupacao?.sala.find((c) => c.dia === dia && c.inicio === faixa);
  }

  fundo(celula: CelulaOcupacao | undefined): string {
    if (!celula || celula.classificacao === 'FECHADO') return '';
    const intensidade = Math.min(1, celula.percentual / 100);
    return `rgba(197, 255, 120, ${0.08 + intensidade * 0.62})`;
  }

  classeVariacao(item: ValorIndicador): string {
    const resultado = item.variacao?.resultado;
    return resultado === 'MELHOROU' ? 'selo selo-bom' : resultado === 'PIOROU' ? 'selo selo-ruim' : 'selo';
  }

  agrupamento(): string {
    const valor = this.relatorio()?.agrupamento;
    return valor === 'DIA' ? 'por dia' : valor === 'SEMANA' ? 'por semana' : 'por mês';
  }

  abrirExportacao() {
    const relatorio = this.relatorio();
    if (!relatorio) return;
    this.exportando.set(true);
    this.api
      .exportacao(relatorio.identificador)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({ next: (previa) => this.previa.set(previa) });
  }

  /** Prévia dos dados pessoais como sairão no arquivo: CPF e telefone mascarados (RN019). */
  previaDadosPessoais() {
    const tabela = this.previa()?.tabelas.find((t) => t.colunas.includes('CPF'));
    if (!tabela) return null;
    const indices = ['Aluno', 'CPF', 'Telefone'].map((coluna) => tabela.colunas.indexOf(coluna));
    return tabela.linhas.slice(0, 3).map((linha) => indices.map((indice) => linha[indice]));
  }

  baixar() {
    const relatorio = this.relatorio();
    if (!relatorio || this.ocupado()) return;
    this.ocupado.set(true);
    this.api
      .arquivo(relatorio.identificador, this.formato())
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (resposta) => {
          const disposicao = resposta.headers.get('Content-Disposition') ?? '';
          const nome = /filename="?([^";]+)"?/.exec(disposicao)?.[1] ?? 'relatorio';
          salvarArquivo(resposta.body!, decodeURIComponent(nome));
          this.exportando.set(false);
        },
        error: (falha) =>
          this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível exportar o relatório.')),
      });
  }

  novaVersao() {
    const relatorio = this.relatorio();
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
          this.notificacoes.publicar('sucesso', `Versão ${nova.versao} gerada. A versão anterior continua no histórico.`);
          this.router.navigate(['/core/relatorio', nova.identificador, 'visualizacao']);
        },
        error: (falha) => this.notificacoes.publicar('erro', mensagemDeErro(falha, 'Não foi possível gerar a nova versão.')),
      });
  }

  fimFaixa(faixa: string): string {
    return hora(`${String(Number(faixa.substring(0, 2)) + 2).padStart(2, '0')}:00`);
  }
}
