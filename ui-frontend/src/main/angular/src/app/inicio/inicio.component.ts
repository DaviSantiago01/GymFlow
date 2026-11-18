import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ServicoAutenticacao } from '../../d5';
import { perfilDaSessao } from '../perfil';
import type { AutorizacoesExecucao } from '../treinos/treinos.service';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.scss',
})
export class InicioComponent {
  protected readonly autenticacao = inject(ServicoAutenticacao);
  protected readonly perfilAtual = computed(() => perfilDaSessao(this.autenticacao.autorizacoes()));
  protected readonly podeRegistrarExecucao = computed(
    () =>
      this.autenticacao.autorizacoesDe<AutorizacoesExecucao>('execucaoDeExercicio')?.registrar ??
      false,
  );
}
