import {
  Component,
  input,
  output,
  signal,
  computed,
  effect,
  DestroyRef,
  inject,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  TreinoResumo,
  podeResponder,
  podeCancelarPresenca,
  rotuloSituacao,
} from './grupos.service';
@Component({
  selector: 'app-treino-grupo',
  imports: [DatePipe, RouterLink],
  templateUrl: './treino-grupo.component.html',
  styleUrl: './grupos.component.scss',
})
export class TreinoGrupoComponent {
  readonly treino = input.required<TreinoResumo>();
  readonly ocupado = input(false);
  readonly detalhado = input(false);
  readonly acao = output<{ treino: TreinoResumo; acao: string }>();
  readonly agora = signal(Date.now());
  readonly responder = computed(() => podeResponder(this.treino(), this.agora()));
  readonly cancelar = computed(() => podeCancelarPresenca(this.treino(), this.agora()));
  readonly situacao = computed(() => {
    const t = this.treino();
    return t.meuConvite?.situacao === 'PENDENTE' && this.agora() >= Date.parse(t.prazo)
      ? 'Prazo encerrado'
      : rotuloSituacao(t.meuConvite?.situacao ?? 'Sem convite');
  });
  readonly prazoEncerrado = computed(() => this.agora() >= Date.parse(this.treino().prazo));
  readonly passado = computed(() => this.agora() >= Date.parse(this.treino().inicio));
  constructor() {
    const destroy = inject(DestroyRef);
    let base = Date.now(),
      inicio = Date.now();
    effect(() => {
      base = Date.parse(this.treino().agora);
      inicio = Date.now();
      this.agora.set(base);
    });
    const timer = setInterval(() => this.agora.set(base + Date.now() - inicio), 1000);
    destroy.onDestroy(() => clearInterval(timer));
  }
  enviar(acao: string) {
    this.acao.emit({ treino: this.treino(), acao });
  }
}
