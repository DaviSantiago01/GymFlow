import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import { CATEGORIAS, Categoria, DIAS, DiaSemana, Perfil, RecomendacoesService, data, diaCurto } from './recomendacoes.service';

@Component({
  selector: 'app-meu-perfil',
  imports: [FormsModule, RouterLink],
  templateUrl: './meu-perfil.component.html',
  styleUrls: ['../analises.scss', '../analises-graficos.scss', '../analises-recomendacoes.scss'],
})
export class MeuPerfilComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly api = inject(RecomendacoesService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);

  readonly perfil = signal<Perfil | null>(null);
  readonly aba = signal<'dados' | 'preferencias'>('dados');
  readonly ocupado = signal(false);
  readonly erro = signal<string | null>(null);
  readonly editandoRestricao = signal(false);
  readonly dias = DIAS;
  readonly categorias = CATEGORIAS;
  readonly data = data;
  readonly diaCurto = diaCurto;

  objetivo: string | null = null;
  nivel: string | null = null;
  diasEscolhidos = new Set<DiaSemana>();
  inicio = '';
  fim = '';
  regiao = '';
  descricao = '';
  desativadas = new Set<Categoria>();

  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((dados) => {
      this.aba.set(dados['aba']);
      this.aplicar(dados['perfil']);
    });
  }

  private aplicar(perfil: Perfil) {
    this.perfil.set(perfil);
    this.objetivo = perfil.objetivo;
    this.nivel = perfil.nivel;
    this.diasEscolhidos = new Set(perfil.dias);
    this.inicio = perfil.inicio?.substring(0, 5) ?? '18:00';
    this.fim = perfil.fim?.substring(0, 5) ?? '21:00';
    this.desativadas = new Set(perfil.categoriasDesativadas);
  }

  alternarDia(dia: DiaSemana) {
    if (this.diasEscolhidos.has(dia)) this.diasEscolhidos.delete(dia);
    else this.diasEscolhidos.add(dia);
  }

  descricaoNivel(): string {
    switch (this.nivel) {
      case 'INICIANTE':
        return 'Iniciante: começando agora ou voltando depois de muito tempo.';
      case 'INTERMEDIARIO':
        return 'Intermediário: treina com regularidade há mais de 6 meses.';
      case 'AVANCADO':
        return 'Avançado: treina há anos e domina os exercícios.';
      default:
        return '';
    }
  }

  resumoDisponibilidade(): string {
    const quantidade = this.diasEscolhidos.size;
    return `${quantidade} ${quantidade === 1 ? 'dia' : 'dias'} por semana, das ${this.inicio.replace(':00', 'h')} às ${this.fim.replace(':00', 'h')}.`;
  }

  editarRestricao(regiao: string, descricao: string) {
    this.regiao = regiao;
    this.descricao = descricao;
    this.editandoRestricao.set(true);
  }

  novaRestricao() {
    this.regiao = '';
    this.descricao = '';
    this.editandoRestricao.set(true);
  }

  nomeRegiao(valor: string): string {
    return this.perfil()?.regioes.find((regiao) => regiao.valor === valor)?.descricao ?? valor;
  }

  salvarDados() {
    const ordem = DIAS.map((dia) => dia.valor).filter((dia) => this.diasEscolhidos.has(dia));
    this.executar(
      this.api.salvarDadosDeTreino({ objetivo: this.objetivo, nivel: this.nivel, dias: ordem, inicio: this.inicio, fim: this.fim }),
      'Perfil salvo. As próximas recomendações já usam esses dados.',
    );
  }

  salvarRestricao() {
    this.executar(this.api.registrarRestricao(this.regiao, this.descricao), 'Restrição registrada.', () =>
      this.editandoRestricao.set(false),
    );
  }

  removerRestricao(regiao: string) {
    this.executar(this.api.removerRestricao(regiao), 'Restrição removida.');
  }

  alternarCategoria(categoria: Categoria, opcional: boolean) {
    if (!opcional) return;
    if (this.desativadas.has(categoria)) this.desativadas.delete(categoria);
    else this.desativadas.add(categoria);
  }

  salvarPreferencias() {
    this.executar(this.api.salvarPreferencias([...this.desativadas]), 'Preferências salvas.');
  }

  private executar(pedido: Observable<Perfil>, mensagem: string, depois?: () => void) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set(null);
    pedido
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (perfil) => {
          this.aplicar(perfil);
          this.notificacoes.publicar('sucesso', mensagem);
          depois?.();
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível salvar.')),
      });
  }
}
