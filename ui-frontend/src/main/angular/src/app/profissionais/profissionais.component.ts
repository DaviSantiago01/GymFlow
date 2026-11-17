import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ServicoAutenticacao } from '../../d5';
import {
  AutorizacoesProfissional,
  ProfissionalResumo,
  ROTULOS_SITUACAO_PROFISSIONAL,
  cpfFormatado,
} from './profissionais.service';

@Component({
  selector: 'app-profissionais',
  imports: [FormsModule, RouterLink],
  templateUrl: './profissionais.component.html',
  styleUrl: '../telas.scss',
})
export class ProfissionaisComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly autenticacao = inject(ServicoAutenticacao);
  readonly rotulosSituacao = ROTULOS_SITUACAO_PROFISSIONAL;
  readonly cpfFormatado = cpfFormatado;
  readonly profissionais = signal<ProfissionalResumo[]>([]);
  readonly podeCadastrar = computed(
    () => this.autenticacao.autorizacoesDe<AutorizacoesProfissional>('profissional')?.cadastrar ?? false,
  );
  busca = '';
  classificacao = '';
  situacao = '';

  constructor() {
    this.rota.data
      .pipe(takeUntilDestroyed())
      .subscribe((dados) => this.profissionais.set(dados['tela'].resultado.projecoes));
    this.rota.queryParamMap.pipe(takeUntilDestroyed()).subscribe((parametros) => {
      this.busca = parametros.get('busca') ?? '';
      this.classificacao = parametros.get('classificacao') ?? '';
      this.situacao = parametros.get('situacao') ?? '';
    });
  }

  filtrar() {
    this.router.navigate([], {
      relativeTo: this.rota,
      queryParams: {
        busca: this.busca.trim() || null,
        classificacao: this.classificacao || null,
        situacao: this.situacao || null,
      },
    });
  }

  classificacoes(profissional: ProfissionalResumo): string {
    return [profissional.professor ? 'Professor' : '', profissional.personalTrainer ? 'Personal trainer' : '']
      .filter(Boolean)
      .join(' e ');
  }
}
