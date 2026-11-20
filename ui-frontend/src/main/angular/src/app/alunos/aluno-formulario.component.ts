import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ServicoNotificacao } from '../../d5';
import { mensagemDeErro } from '../formatacao';
import { AlunoEnvio, AlunosService, FichaAluno } from './alunos.service';

@Component({
  selector: 'app-aluno-formulario',
  imports: [FormsModule, RouterLink],
  templateUrl: './aluno-formulario.component.html',
  styleUrl: '../telas.scss',
})
export class AlunoFormularioComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(AlunosService);
  private readonly notificacoes = inject(ServicoNotificacao);
  private readonly destroy = inject(DestroyRef);
  readonly ficha = signal<FichaAluno | null>(this.rota.snapshot.data['tela']);
  readonly alteracao = computed(() => this.ficha() !== null);
  readonly ocupado = signal(false);
  readonly erro = signal('');
  readonly hoje = new Date().toISOString().slice(0, 10);
  nome = this.ficha()?.nome ?? '';
  cpf = this.ficha()?.cpf ?? '';
  nascimento = this.ficha()?.nascimento ?? '';
  email = this.ficha()?.email ?? '';
  telefone = this.ficha()?.telefone ?? '';
  endereco = this.ficha()?.endereco ?? '';
  responsavelNome = this.ficha()?.responsavel?.nome ?? '';
  responsavelCpf = this.ficha()?.responsavel?.cpf ?? '';
  responsavelTelefone = this.ficha()?.responsavel?.telefone ?? '';
  usuario = '';
  senha = '';

  /** RN004: o responsável é pedido quando a data informada indica menos de 18 anos. */
  menorDeIdade(): boolean {
    if (!this.nascimento) return false;
    const nascimento = new Date(this.nascimento + 'T00:00:00');
    const limite = new Date();
    limite.setFullYear(limite.getFullYear() - 18);
    return nascimento > limite;
  }

  salvar() {
    if (this.ocupado()) return;
    const envio: AlunoEnvio = {
      nome: this.nome,
      cpf: this.cpf,
      nascimento: this.nascimento,
      email: this.email,
      telefone: this.telefone,
      endereco: this.endereco,
      responsavelNome: this.responsavelNome,
      responsavelCpf: this.responsavelCpf,
      responsavelTelefone: this.responsavelTelefone,
    };
    const atual = this.ficha();
    const requisicao = atual
      ? this.api.editar(atual.identificador, envio)
      : this.api.cadastrar({ ...envio, usuario: this.usuario, senha: this.senha });
    this.ocupado.set(true);
    this.erro.set('');
    requisicao
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.ocupado.set(false)),
      )
      .subscribe({
        next: (salvo) => {
          this.notificacoes.publicar('sucesso', `Aluno ${salvo.nome} salvo.`);
          this.router.navigate(['/core/aluno', salvo.identificador, 'ficha']);
        },
        error: (falha) => this.erro.set(mensagemDeErro(falha, 'Não foi possível salvar o aluno.')),
      });
  }
}
