import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Aula, Avaliacao, Desafio, AulasDesafiosService, Professor } from './aulas-desafios.service';
import { GruposService, TreinoResumo, situacaoAgendamento } from '../grupos/grupos.service';

@Component({
  selector: 'app-aulas-desafios', standalone: true, imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './aulas-desafios.component.html', styleUrls: ['../telas.scss', './aulas-desafios.component.scss'],
})
export class AulasDesafiosComponent implements OnInit {
  private readonly api = inject(AulasDesafiosService);
  private readonly route = inject(ActivatedRoute);
  private readonly grupos = inject(GruposService);
  readonly modo = this.route.snapshot.data['modo'] as string;
  readonly id = Number(this.route.snapshot.paramMap.get('id'));
  readonly aulas = signal<Aula[]>([]);
  readonly aulaAtual = signal<Aula | null>(null);
  readonly desafios = signal<Desafio[]>([]);
  readonly professores = signal<Professor[]>([]);
  readonly alunos = signal<{ identificador: number; nome: string }[]>([]);
  readonly inscricoes = signal<Record<string, unknown>[]>([]);
  readonly notificacoes = signal<Record<string, unknown>[]>([]);
  readonly treinosGrupo = signal<TreinoResumo[]>([]);
  situacaoTreino(t: TreinoResumo) { return situacaoAgendamento(t, Date.now()); }
  readonly avaliacoes = signal<Avaliacao[]>([]);
  readonly evolucoes = signal<Record<string, unknown>[]>([]);
  readonly erro = signal('');
  readonly sucesso = signal('');
  readonly carregando = signal(false);
  filtroData = '';
  busca = '';
  modalidade = '';
  editandoAula: number | null = null;
  editandoAvaliacao: number | null = null;
  aulaForm = { titulo: '', modalidade: '', descricao: '', data: '', inicio: '', fim: '', limite: 12, professor: 0 };
  desafioForm = { titulo: '', indicador: 'PESO', inicio: '', fim: '' };
  avaliacaoForm = { desafio: 0, aluno: 0, profissional: 0, tipo: 'INICIAL', peso: 0, cintura: 0, gordura: 0 };
  inscricaoForm = { desafio: 0, indicador: 'PESO', meta: 0 };

  ngOnInit() { this.carregar(); }

  carregar() {
    this.erro.set(''); this.sucesso.set('');
    if (this.modo.startsWith('aulas-gestao') || this.modo === 'aulas-grade') {
      this.api.aulas(this.filtroData, this.modalidade, this.busca).subscribe({ next: x => this.aulas.set(x), error: e => this.falha(e) });
      if (this.modo === 'aulas-gestao') this.api.notificacoes().subscribe({ next: x => this.notificacoes.set(x), error: () => this.notificacoes.set([]) });
      if (this.modo === 'aulas-gestao') this.api.professores().subscribe({ next: x => this.professores.set(x), error: e => this.falha(e) });
    }
    if (this.modo === 'aulas-minhas') {
      this.api.minhasAulas().subscribe({ next: x => this.inscricoes.set(x), error: e => this.falha(e) });
      this.grupos.agendamentos().subscribe({ next: x => this.treinosGrupo.set(x), error: () => this.treinosGrupo.set([]) });
    }
    if (this.modo === 'aulas-inscritos') {
      this.api.aula(this.id).subscribe({ next: x => this.aulaAtual.set(x), error: e => this.falha(e) });
      this.api.inscritos(this.id).subscribe({ next: x => this.inscricoes.set(x), error: e => this.falha(e) });
      this.api.notificacoes().subscribe({ next: x => this.notificacoes.set(x), error: () => this.notificacoes.set([]) });
    }
    if (this.modo.startsWith('desafio')) {
      this.api.desafios().subscribe({ next: x => this.desafios.set(x), error: e => this.falha(e) });
      if (this.modo === 'desafio-avaliacoes') {
        this.api.professores().subscribe({ next: x => this.professores.set(x), error: e => this.falha(e) });
        this.api.alunos().subscribe({ next: x => this.alunos.set(x), error: e => this.falha(e) });
        if (this.id) this.carregarAvaliacoes(this.id);
      }
      if (this.modo === 'desafio-evolucao') this.api.evolucao(this.id).subscribe({ next: x => this.evolucoes.set(x), error: e => this.falha(e) });
    }
  }

  salvarAula() {
    this.executar(this.api.salvarAula(this.aulaForm, this.editandoAula ?? undefined), () => {
      this.sucesso.set(this.editandoAula ? 'Aula atualizada.' : 'Aula cadastrada.');
      this.editandoAula = null; this.aulaForm = { titulo: '', modalidade: '', descricao: '', data: '', inicio: '', fim: '', limite: 12, professor: 0 }; this.carregar();
    });
  }
  editar(a: Aula) {
    this.editandoAula = a.identificador;
    this.aulaForm = { titulo: a.titulo, modalidade: a.modalidade, descricao: a.descricao ?? '', data: a.data, inicio: a.inicio.slice(0,5), fim: a.fim.slice(0,5), limite: a.limite, professor: a.professor };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  cancelarAula(a: Aula) { if (confirm(`Cancelar a turma “${a.titulo}” e notificar os inscritos?`)) this.executar(this.api.cancelarAula(a.identificador), () => { this.sucesso.set('Turma cancelada e inscritos notificados.'); this.carregar(); }); }
  inscreverAula(a: Aula) { this.executar(this.api.inscrever(a.identificador), () => { this.sucesso.set('Inscrição confirmada.'); this.carregar(); }); }
  cancelarInscricao(id: number) { this.executar(this.api.cancelarInscricao(id), () => { this.sucesso.set('Inscrição cancelada; a vaga foi liberada.'); this.carregar(); }); }
  cancelarInscricaoGestao(id: number) { this.executar(this.api.cancelarInscricaoGestao(id), () => { this.sucesso.set('Inscrição cancelada e vaga liberada.'); this.carregar(); }); }
  criarDesafio() { this.executar(this.api.criarDesafio(this.desafioForm), () => { this.sucesso.set('Desafio criado.'); this.desafioForm = { titulo: '', indicador: 'PESO', inicio: '', fim: '' }; this.carregar(); }); }
  carregarAvaliacoes(id: number) { this.avaliacaoForm.desafio = id; this.api.avaliacoes(id).subscribe({ next: x => this.avaliacoes.set(x), error: e => this.falha(e) }); }
  salvarAvaliacao() {
    const operacao = this.editandoAvaliacao ? this.api.editarAvaliacao(this.editandoAvaliacao, this.avaliacaoForm) : this.api.salvarAvaliacao(this.avaliacaoForm);
    this.executar(operacao, () => { this.editandoAvaliacao = null; this.sucesso.set('Avaliação registrada.'); this.carregarAvaliacoes(this.avaliacaoForm.desafio); });
  }
  editarAvaliacao(a: Avaliacao) { this.editandoAvaliacao = a.identificador; this.avaliacaoForm = { desafio: a.desafio, aluno: a.aluno, profissional: a.profissional, tipo: a.tipo, peso: a.peso, cintura: a.cintura, gordura: a.gordura }; }
  excluirAvaliacao(a: Avaliacao) { if (confirm('Excluir esta avaliação?')) this.executar(this.api.excluirAvaliacao(a.identificador), () => { this.sucesso.set('Avaliação removida.'); this.carregarAvaliacoes(a.desafio); }); }
  inscreverDesafio(d: Desafio) { this.inscricaoForm.desafio = d.identificador; this.inscricaoForm.indicador = d.indicador; this.executar(this.api.inscreverDesafio(this.inscricaoForm), () => this.sucesso.set('Inscrição no desafio confirmada.')); }
  encerrar(d: Desafio) { this.executar(this.api.encerrarDesafio(d.identificador), () => { this.sucesso.set('Desafio consolidado e encerrado.'); this.carregar(); }); }
  percentagem(e: Record<string, unknown>) {
    const chave = String(e['indicador'] ?? '').toLowerCase() === 'peso' ? 'peso' : String(e['indicador'] ?? '').toLowerCase() === 'cintura' ? 'cintura' : 'gordura';
    const inicial = Number(e[chave + 'Inicial']); const atual = Number(e[chave + 'Atual']); const meta = Number(e['meta']);
    if (!inicial || !atual || !meta) return 0;
    return Math.min(100, Math.round((Math.abs(inicial - atual) / meta) * 100));
  }
  evolucoesOrdenadas() { return [...this.evolucoes()].sort((a, b) => this.percentagem(b) - this.percentagem(a)); }
  private executar<T>(op: import('rxjs').Observable<T>, ok: () => void) {
    this.erro.set(''); this.sucesso.set(''); this.carregando.set(true);
    op.subscribe({ next: () => ok(), error: e => this.falha(e), complete: () => this.carregando.set(false) });
  }
  private falha(e: HttpErrorResponse) { this.carregando.set(false); this.erro.set(e.error?.mensagem ?? e.error?.message ?? e.message ?? 'Não foi possível concluir a operação.'); }
}
