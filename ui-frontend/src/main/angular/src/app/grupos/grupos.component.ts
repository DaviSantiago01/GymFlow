import { Component, inject, signal, DestroyRef } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, switchMap } from 'rxjs';
import {
  GruposService,
  Tela,
  Modo,
  ConviteEntrada,
  TreinoResumo,
  mensagemErro,
  validarNome,
  validarAgenda,
  prazoDeConfirmacao,
  rotuloSituacao,
  HorarioOpcao,
  situacaoHorario,
  horarioDisponivel,
} from './grupos.service';
import { SelecaoAlunosComponent } from './selecao-alunos.component';
import { TreinoGrupoComponent } from './treino-grupo.component';
@Component({
  selector: 'app-grupos',
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    RouterLinkActive,
    SelecaoAlunosComponent,
    TreinoGrupoComponent,
  ],
  templateUrl: './grupos.component.html',
  styleUrl: './grupos.component.scss',
})
export class GruposComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(GruposService);
  private readonly destroy = inject(DestroyRef);
  readonly dados = signal<Tela>({});
  readonly modo = signal<Modo>('lista');
  readonly erro = signal('');
  readonly aviso = signal('');
  readonly ocupado = signal(false);
  readonly carregando = signal(false);
  readonly historico = signal(false);
  readonly pagina = signal(0);
  readonly editarNome = signal(false);
  readonly revisaoAlunos = signal(0);
  nome = '';
  convidados: string[] = [];
  data = '';
  readonly horarios = signal<HorarioOpcao[]>([]);
  readonly carregandoHorarios = signal(false);
  readonly horario = signal<HorarioOpcao | null>(null);
  professor = '';
  readonly situacaoHorario = situacaoHorario;
  readonly horarioDisponivel = horarioDisponivel;
  alunoEscolhido: string[] = [];
  readonly rotulo = rotuloSituacao;
  private recebido = Date.now();
  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((d) => {
      this.modo.set(d['modo']);
      this.dados.set(d['tela']);
      this.erro.set(d['tela'].erro ?? '');
      this.historico.set(this.rota.snapshot.queryParamMap.get('historico') === 'true');
      this.pagina.set(Math.max(0, Number(this.rota.snapshot.queryParamMap.get('pagina')) || 0));
      this.nome = this.dados().detalhe?.grupo.nome ?? '';
      this.convidados = [];
      this.alunoEscolhido = [];
      this.editarNome.set(false);
      this.data = '';
      this.horarios.set([]);
      this.horario.set(null);
      this.professor = '';
      this.recebido = Date.now();
      const params = this.rota.snapshot.queryParamMap;
      this.aviso.set(
        params.get('criado')
          ? 'Grupo criado com sucesso.'
          : params.get('proposto')
            ? 'Treino proposto. Sua presença já está confirmada e o treino foi salvo em Meus agendamentos.'
            : '',
      );
    });
  }
  id() {
    return this.rota.snapshot.paramMap.get('id') ?? '';
  }
  titulo() {
    return {
      lista: 'Meus grupos de treino',
      criar: 'Criar novo grupo',
      grupo: this.dados().detalhe?.grupo.nome ?? 'Detalhes do grupo',
      propor: 'Propor treino',
      treino: 'Detalhes do treino',
      convites: 'Convites para treinos',
      historico: 'Histórico dos grupos',
    }[this.modo()];
  }
  private carregar() {
    return this.api.carregar(
      this.modo(),
      this.id(),
      this.rota.snapshot.paramMap.get('treino') ?? '',
      this.pagina(),
      this.historico(),
    );
  }
  atualizar() {
    if (this.carregando() || this.ocupado()) return;
    this.carregando.set(true);
    this.carregar()
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.carregando.set(false)),
      )
      .subscribe({
        next: (d) => {
          this.dados.set(d);
          this.erro.set('');
          this.recebido = Date.now();
        },
        error: (e) => this.falha(mensagemErro(e)),
      });
  }
  private falha(mensagem: string) {
    this.erro.set(mensagem);
    setTimeout(() => document.getElementById('erro-grupos')?.focus());
  }
  criar() {
    const erro = validarNome(this.nome);
    if (erro) {
      this.falha(erro);
      return;
    }
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    this.api
      .criar(this.nome.trim(), this.convidados)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (g) =>
          this.router.navigate(['/core/grupo-treino', g.identificador, 'visualizacao'], {
            queryParams: { criado: true },
          }),
        error: (e) => this.falha(mensagemErro(e)),
      });
  }
  executar(caminho: string, corpo: unknown, mensagem: string, sair = false) {
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    this.aviso.set('');
    this.api
      .comando(caminho, corpo)
      .pipe(
        switchMap(() => (sair ? this.api.carregar('lista', '', '', 0) : this.carregar())),
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (d) => {
          if (sair) {
            this.router.navigateByUrl('/core/grupo-treino/pesquisa');
            return;
          }
          this.dados.set(d);
          this.recebido = Date.now();
          this.aviso.set(mensagem);
          this.editarNome.set(false);
          this.revisaoAlunos.update((v) => v + 1);
        },
        error: (e) => this.falha(mensagemErro(e)),
      });
  }
  renomear() {
    const erro = validarNome(this.nome);
    if (erro) this.falha(erro);
    else this.executar(`${this.id()}/renomear`, { nome: this.nome.trim() }, 'Nome atualizado.');
  }
  convidar() {
    if (this.alunoEscolhido.length)
      this.executar(
        `${this.id()}/convidar`,
        { aluno: this.alunoEscolhido[0] },
        'Convite enviado. O aluno poderá responder na área de grupos.',
      );
  }
  responderEntrada(c: ConviteEntrada, aceitar: boolean) {
    this.executar(
      `${c.grupo}/convites/${c.identificador}/${aceitar ? 'aceitar' : 'recusar'}`,
      {},
      aceitar ? 'Você entrou no grupo.' : 'Convite recusado.',
    );
  }
  agirGrupo(acao: 'sair' | 'encerrar') {
    if (
      !window.confirm(
        acao === 'encerrar'
          ? 'Encerrar este grupo? Os treinos futuros serão cancelados e o histórico será preservado.'
          : 'Sair deste grupo? Seus convites pendentes serão cancelados.',
      )
    )
      return;
    this.executar(`${this.id()}/${acao}`, {}, 'Grupo atualizado.', acao === 'sair');
  }
  /** Ao escolher a data, carrega os horários do dia com as vagas e os professores livres em cada um. */
  escolherData(data: string) {
    this.data = data;
    this.horario.set(null);
    this.professor = '';
    this.horarios.set([]);
    if (!data) return;
    this.carregandoHorarios.set(true);
    this.erro.set('');
    this.api
      .horarios(this.id(), data)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.carregandoHorarios.set(false)),
      )
      .subscribe({
        next: (h) => this.horarios.set(h),
        error: (e) => this.falha(mensagemErro(e)),
      });
  }
  escolherHorario(h: HorarioOpcao) {
    if (!horarioDisponivel(h)) return;
    this.horario.set(h);
    if (!h.professores.some((p) => p.identificador === this.professor)) this.professor = '';
  }
  propor() {
    const h = this.horario();
    if (!this.data || !h) {
      this.falha('Escolha a data e um horário com vaga disponível.');
      return;
    }
    if (!this.professor) {
      this.falha('Escolha o professor do treino.');
      return;
    }
    const agora = Date.parse(this.dados().detalhe!.agora) + Date.now() - this.recebido;
    const erro = validarAgenda(h.inicio, agora);
    if (erro) {
      this.falha(erro);
      return;
    }
    if (this.ocupado()) return;
    this.ocupado.set(true);
    this.api
      .comando(`${this.id()}/propor`, { inicio: h.inicio, professor: this.professor })
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: () =>
          this.router.navigate(['/core/grupo-treino', this.id(), 'visualizacao'], {
            queryParams: { proposto: true },
          }),
        error: (e) => {
          this.falha(mensagemErro(e));
          this.escolherData(this.data);
        },
      });
  }
  acaoTreino(evento: { treino: TreinoResumo; acao: string }) {
    const { treino: t, acao } = evento;
    if (
      acao.startsWith('cancelar') &&
      !window.confirm(
        acao === 'cancelar-treino'
          ? 'Cancelar este treino para todos os convidados?'
          : 'Cancelar sua presença neste treino?',
      )
    )
      return;
    const caminho =
      acao === 'cancelar-treino'
        ? `${t.grupo}/treinos/${t.identificador}/cancelar`
        : `${t.grupo}/treinos/${t.identificador}/convites/${t.meuConvite!.identificador}/${acao}`;
    this.executar(
      caminho,
      {},
      acao === 'confirmar'
        ? 'Presença confirmada.'
        : acao === 'recusar'
          ? 'Presença recusada.'
          : 'Cancelamento registrado.',
    );
  }
  paginar(pagina: number) {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: { pagina },
      queryParamsHandling: 'merge',
    });
  }
  alternarHistorico(historico: boolean) {
    this.router.navigate([], { relativeTo: this.rota, queryParams: { historico, pagina: 0 } });
  }
  totalPaginas() {
    const d = this.dados();
    return Math.max(
      Math.ceil((d.total ?? 0) / 12),
      Math.ceil((d.entradas?.total ?? 0) / 20),
      Math.ceil((d.treinos?.total ?? d.detalhe?.treinos.total ?? 0) / 20),
      1,
    );
  }
  prazoPrevisto() {
    const h = this.horario();
    return h ? prazoDeConfirmacao(h.inicio) : '';
  }
  minimo() {
    const base =
      this.dados().detalhe?.agora ?? this.dados().contexto?.agora ?? new Date().toISOString();
    return new Date(Date.parse(base) - 10800000).toISOString().slice(0, 10);
  }
  membrosAtivos() {
    return this.dados().detalhe?.membros.filter((m) => !m.saida) ?? [];
  }
}
