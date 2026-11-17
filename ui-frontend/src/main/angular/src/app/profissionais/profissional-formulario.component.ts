import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import {
  Classificacao,
  DetalheProfissional,
  ProfissionaisService,
  ROTULOS_CLASSIFICACAO,
} from './profissionais.service';

@Component({
  selector: 'app-profissional-formulario',
  imports: [FormsModule, RouterLink],
  templateUrl: './profissional-formulario.component.html',
  styleUrl: '../telas.scss',
})
export class ProfissionalFormularioComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ProfissionaisService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);
  readonly rotulos = ROTULOS_CLASSIFICACAO;
  readonly opcoes = Object.keys(ROTULOS_CLASSIFICACAO) as Classificacao[];
  readonly detalhe = signal<DetalheProfissional | null>(this.rota.snapshot.data['tela']);
  readonly alteracao = computed(() => this.detalhe() !== null);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  readonly classificacoes = signal<Classificacao[]>(this.detalhe()?.profissional.classificacoes ?? []);
  nome = this.detalhe()?.profissional.nome ?? '';
  cpf = this.detalhe()?.cpfFormatado ?? '';
  cref = this.detalhe()?.profissional.cref ?? '';
  contato = this.detalhe()?.profissional.contato ?? '';

  marcado(classificacao: Classificacao) {
    return this.classificacoes().includes(classificacao);
  }

  alternar(classificacao: Classificacao) {
    this.classificacoes.update((atuais) =>
      atuais.includes(classificacao) ? atuais.filter((item) => item !== classificacao) : [...atuais, classificacao],
    );
  }

  salvar() {
    if (this.ocupado()) return;
    const envio = {
      nome: this.nome,
      cpf: this.cpf,
      cref: this.cref,
      contato: this.contato,
      classificacoes: this.classificacoes(),
    };
    const atual = this.detalhe();
    const requisicao = atual
      ? this.api.editar(atual.profissional.identificador, envio)
      : this.api.cadastrar(envio);
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (salvo) => {
          this.notificacoes.publicar('sucesso', `Profissional ${salvo.profissional.nome} salvo.`);
          this.router.navigate(['/core/profissional', salvo.profissional.identificador, 'visualizacao']);
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível salvar o profissional.')),
      });
  }
}
