import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ArmariosService, DadosTela, Modo } from './armarios.service';
import { segundosRestantes, formatarDuracao } from './relogio';

@Component({
  selector: 'app-armarios',
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './armarios.component.html',
  styleUrl: './armarios.component.scss',
})
export class ArmariosComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ArmariosService);
  private readonly destroy = inject(DestroyRef);
  readonly dados = signal<DadosTela>(this.rota.snapshot.data['tela']);
  readonly modo = signal<Modo>(this.rota.snapshot.data['modo']);
  readonly ocupado = signal(false);
  readonly atualizando = signal(false);
  readonly erro = signal('');
  readonly aviso = signal('');
  readonly busca = signal('');
  readonly filtro = signal('todos');
  readonly agora = signal(performance.now());
  readonly restante = computed(() => {
    const d = this.dados();
    return d.reserva
      ? segundosRestantes(d.reserva.termino, d.horarioServidor, d.recebidoEm, this.agora())
      : 0;
  });
  readonly tempo = computed(() => formatarDuracao(this.restante()));
  readonly vigente = computed(
    () => this.dados().reserva?.situacao === 'ATIVA' && this.restante() > 0,
  );
  readonly sincronizado = computed(() => this.agora() - this.dados().recebidoEm < 45000);
  readonly progresso = computed(() => Math.min(100, Math.max(0, 100 - this.restante() / 108)));
  readonly disponiveis = computed(
    () => this.dados().armarios?.filter((a) => a.disponivel).length ?? 0,
  );
  readonly filtrados = computed(
    () =>
      this.dados().armarios?.filter(
        (a) =>
          (a.identificador + ' ' + a.localizacao)
            .toLowerCase()
            .includes(this.busca().toLowerCase()) &&
          (this.filtro() === 'todos' || (this.filtro() === 'livres') === a.disponivel),
      ) ?? [],
  );
  readonly rotulos = {
    ATIVA: 'Ativa',
    CANCELADA: 'Cancelada',
    ENCERRADA: 'Uso encerrado',
    VENCIDA: 'Vencida',
  };
  readonly titulos = {
    lista: 'Armários',
    detalhe: 'Detalhes do armário',
    confirmacao: 'Reserva confirmada',
    ativa: 'Minha reserva',
    historico: 'Histórico de reservas',
    reserva: 'Detalhes da reserva',
  };
  private vencimentoAtualizado = '';
  constructor() {
    this.rota.data.pipe(takeUntilDestroyed()).subscribe((data) => {
      this.dados.set(data['tela']);
      this.modo.set(data['modo']);
      this.erro.set('');
      this.aviso.set('');
    });
    const contador = setInterval(() => {
      this.agora.set(performance.now());
      const reserva = this.dados().reserva;
      if (
        reserva?.situacao === 'ATIVA' &&
        this.restante() === 0 &&
        this.vencimentoAtualizado !== reserva.identificador
      ) {
        this.vencimentoAtualizado = reserva.identificador;
        this.aviso.set('O prazo de uso terminou. O armário está disponível novamente.');
        this.atualizar();
      }
    }, 1000);
    const sincronizacao = setInterval(() => this.atualizar(), 15000);
    const aoVoltar = () => {
      if (!document.hidden) this.atualizar();
    };
    document.addEventListener('visibilitychange', aoVoltar);
    this.destroy.onDestroy(() => {
      clearInterval(contador);
      clearInterval(sincronizacao);
      document.removeEventListener('visibilitychange', aoVoltar);
    });
  }
  atualizar() {
    if (this.atualizando() || this.ocupado()) return;
    this.atualizando.set(true);
    this.api
      .carregar(this.modo(), this.rota.snapshot.paramMap.get('id') ?? '', this.pagina())
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.atualizando.set(false)),
      )
      .subscribe({
        next: (d) => {
          this.dados.set(d);
          this.erro.set('');
        },
        error: (e) =>
          this.erro.set(
            e.error?.mensagem ??
              'Não foi possível atualizar. Verifique sua conexão e tente novamente.',
          ),
      });
  }
  reservar() {
    const armario = this.dados().armario;
    if (!armario || this.ocupado()) return;
    this.ocupado.set(true);
    this.erro.set('');
    this.api
      .reservar(armario.identificador)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (r) =>
          this.router.navigate(['/core/reserva-armario', r.dados.identificador, 'confirmacao']),
        error: (e) => {
          this.erro.set(
            e.error?.mensagem ??
              'Não foi possível reservar. Consulte sua reserva antes de tentar novamente.',
          );
        },
      });
  }
  finalizar(acao: 'cancelar' | 'encerrar') {
    const reserva = this.dados().reserva;
    if (
      !reserva ||
      this.ocupado() ||
      !window.confirm(
        acao === 'cancelar'
          ? 'Cancelar sua reserva e liberar o armário?'
          : 'Encerrar o uso e liberar o armário? Retire seus pertences antes de confirmar.',
      )
    )
      return;
    this.ocupado.set(true);
    this.erro.set('');
    this.api
      .finalizar(reserva.identificador, acao)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (r) => {
          this.dados.set({
            reserva: r.dados,
            horarioServidor: r.horarioServidor,
            recebidoEm: performance.now(),
          });
          this.aviso.set(
            acao === 'cancelar'
              ? 'Reserva cancelada. O armário foi liberado.'
              : 'Uso encerrado. O armário foi liberado.',
          );
        },
        error: (e) =>
          this.erro.set(
            e.error?.mensagem ?? 'Não foi possível concluir. Atualize para verificar a situação.',
          ),
      });
  }
  pagina() {
    return Math.max(0, Number(this.rota.snapshot.queryParamMap.get('pagina') ?? 0));
  }
}
