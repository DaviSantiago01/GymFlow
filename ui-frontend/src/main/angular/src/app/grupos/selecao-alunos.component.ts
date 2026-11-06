import {
  Component,
  inject,
  input,
  output,
  signal,
  effect,
  untracked,
  DestroyRef,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { GruposService, AlunoOpcao, Pagina, mensagemErro } from './grupos.service';
@Component({
  selector: 'app-selecao-alunos',
  imports: [FormsModule],
  templateUrl: './selecao-alunos.component.html',
  styleUrl: './grupos.component.scss',
})
export class SelecaoAlunosComponent {
  readonly grupo = input('');
  readonly multiplo = input(true);
  readonly bloqueado = input(false);
  readonly revisao = input(0);
  readonly selecao = output<string[]>();
  private readonly api = inject(GruposService);
  private readonly destroy = inject(DestroyRef);
  private requisicao = 0;
  readonly lista = signal<Pagina<AlunoOpcao>>({ itens: [], total: 0, pagina: 0, tamanho: 20 });
  readonly escolhidos = signal<AlunoOpcao[]>([]);
  readonly erro = signal('');
  readonly pesquisado = signal(false);
  readonly carregando = signal(false);
  busca = '';
  constructor() {
    effect(() => {
      this.revisao();
      this.grupo();
      untracked(() => {
        this.escolhidos.set([]);
        this.selecao.emit([]);
        this.busca = '';
        this.requisicao++;
        this.lista.set({ itens: [], total: 0, pagina: 0, tamanho: 20 });
        this.pesquisado.set(false);
        this.carregando.set(false);
      });
    });
  }
  carregar(pagina: number) {
    const numero = ++this.requisicao;
    this.lista.set({ itens: [], total: 0, pagina: 0, tamanho: 20 });
    this.pesquisado.set(false);
    if (!this.busca.trim()) {
      this.erro.set('Informe o nome de usuário usado no login.');
      this.carregando.set(false);
      return;
    }
    this.pesquisado.set(true);
    this.carregando.set(true);
    this.erro.set('');
    this.api
      .alunos(this.busca, pagina, this.grupo())
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (r) => {
          if (numero === this.requisicao) {
            this.lista.set(r);
            this.carregando.set(false);
          }
        },
        error: (e) => {
          if (numero === this.requisicao) {
            this.erro.set(mensagemErro(e));
            this.carregando.set(false);
          }
        },
      });
  }
  marcado(id: string) {
    return this.escolhidos().some((a) => a.identificador === id);
  }
  escolher(a: AlunoOpcao) {
    if (this.bloqueado()) return;
    this.escolhidos.update((atuais) =>
      this.marcado(a.identificador)
        ? atuais.filter((v) => v.identificador !== a.identificador)
        : this.multiplo()
          ? [...atuais, a]
          : [a],
    );
    this.selecao.emit(this.escolhidos().map((v) => v.identificador));
  }
}
