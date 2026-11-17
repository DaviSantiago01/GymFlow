import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize, switchMap } from 'rxjs';
import { ServicoAutenticacao, ServicoNotificacao } from '../../d5';
import { data, mensagemDeErro } from '../formatacao';
import {
  AutorizacoesProfissional,
  DIAS_SEMANA,
  DetalheProfissional,
  DiaSemana,
  Disponibilidade,
  ProfissionaisService,
  ROTULOS_CLASSIFICACAO,
  ROTULOS_DIA,
  ROTULOS_SITUACAO_PROFISSIONAL,
  ROTULOS_SITUACAO_VINCULO,
  SUGESTOES_ESPECIALIDADE,
  Vinculo,
  horario,
  horas,
} from './profissionais.service';

@Component({
  selector: 'app-profissional',
  imports: [FormsModule, RouterLink],
  templateUrl: './profissional.component.html',
  styleUrls: ['../telas.scss', './profissional.component.scss'],
})
export class ProfissionalComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly api = inject(ProfissionaisService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly autenticacao = inject(ServicoAutenticacao);
  private readonly destroy = inject(DestroyRef);
  readonly rotulosClassificacao = ROTULOS_CLASSIFICACAO;
  readonly rotulosDia = ROTULOS_DIA;
  readonly rotulosSituacao = ROTULOS_SITUACAO_PROFISSIONAL;
  readonly rotulosVinculo = ROTULOS_SITUACAO_VINCULO;
  readonly dias = DIAS_SEMANA;
  readonly sugestoes = SUGESTOES_ESPECIALIDADE;
  readonly horario = horario;
  readonly horas = horas;
  readonly data = data;
  readonly tela = signal<DetalheProfissional>(this.rota.snapshot.data['tela']);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  readonly editando = signal<number | null>(null);
  readonly podeGerenciar = computed(
    () => this.autenticacao.autorizacoesDe<AutorizacoesProfissional>('profissional')?.editar ?? false,
  );
  readonly ativo = computed(() => this.tela().profissional.situacao === 'ATIVO');
  readonly personal = computed(() => this.tela().profissional.classificacoes.includes('PERSONAL_TRAINER'));
  readonly percentualCarga = computed(() =>
    Math.min(100, Math.round((this.tela().cargaHorariaSemanalMinutos / this.tela().limiteSemanalMinutos) * 100)),
  );
  readonly disponibilidades = computed(() =>
    [...this.tela().profissional.disponibilidades].sort(
      (a, b) => this.dias.indexOf(a.dia) - this.dias.indexOf(b.dia) || a.inicio.localeCompare(b.inicio),
    ),
  );
  readonly vinculosAtivos = computed(() => this.tela().vinculos.filter((vinculo) => vinculo.situacao === 'ATIVO'));
  especialidade = '';
  dia: DiaSemana = 'MONDAY';
  inicio = '08:00';
  fim = '12:00';
  aluno = '';

  private get id() {
    return this.tela().profissional.identificador;
  }

  adicionarEspecialidade() {
    this.executar(this.api.adicionarEspecialidade(this.id, this.especialidade), 'Especialidade adicionada.', () => {
      this.especialidade = '';
    });
  }

  removerEspecialidade(nome: string) {
    this.executar(this.api.removerEspecialidade(this.id, nome), 'Especialidade removida.');
  }

  editarDisponibilidade(disponibilidade: Disponibilidade) {
    this.editando.set(disponibilidade.identificador);
    this.dia = disponibilidade.dia;
    this.inicio = horario(disponibilidade.inicio);
    this.fim = horario(disponibilidade.fim);
  }

  cancelarEdicao() {
    this.editando.set(null);
  }

  salvarDisponibilidade() {
    const envio = { dia: this.dia, inicio: this.inicio, fim: this.fim };
    const editando = this.editando();
    const requisicao =
      editando === null
        ? this.api.adicionarDisponibilidade(this.id, envio)
        : this.api.alterarDisponibilidade(this.id, editando, envio);
    this.executar(requisicao, 'Disponibilidade salva. A carga semanal foi recalculada.', () =>
      this.editando.set(null),
    );
  }

  removerDisponibilidade(disponibilidade: Disponibilidade) {
    if (window.confirm(`Remover ${ROTULOS_DIA[disponibilidade.dia]} das ${horario(disponibilidade.inicio)}?`)) {
      this.executar(
        this.api.removerDisponibilidade(this.id, disponibilidade.identificador),
        'Disponibilidade removida. A carga semanal foi recalculada.',
      );
    }
  }

  vincular() {
    if (!this.aluno) {
      this.erro.set('Selecione o aluno.');
      return;
    }
    this.executar(this.recarregarApos(this.api.vincular(this.id, this.aluno)), 'Aluno vinculado.', () => {
      this.aluno = '';
    });
  }

  encerrarVinculo(vinculo: Vinculo) {
    if (window.confirm(`Encerrar o vínculo com ${vinculo.alunoNome}? O histórico será preservado.`)) {
      this.executar(this.recarregarApos(this.api.encerrarVinculo(vinculo.identificador)), 'Vínculo encerrado.');
    }
  }

  inativar() {
    if (window.confirm('Inativar o profissional? Ele não receberá novas associações e o histórico será preservado.')) {
      this.executar(this.api.inativar(this.id), 'Profissional inativado.');
    }
  }

  reativar() {
    this.executar(this.api.reativar(this.id), 'Profissional reativado.');
  }

  /** Vínculos pertencem a outro agregado: depois da operação, a tela do profissional é relida. */
  private recarregarApos(requisicao: Observable<Vinculo>): Observable<DetalheProfissional> {
    return requisicao.pipe(switchMap(() => this.api.visualizar(this.id)));
  }

  private executar(requisicao: Observable<DetalheProfissional>, sucesso: string, depois?: () => void) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (detalhe) => {
          this.tela.set(detalhe);
          depois?.();
          this.notificacoes.publicar('sucesso', sucesso, 0);
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível concluir a operação.')),
      });
  }
}
