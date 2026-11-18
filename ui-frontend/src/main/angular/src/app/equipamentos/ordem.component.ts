import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EquipamentosMenuComponent } from './equipamentos-menu.component';
import {
  EquipamentosService,
  Ordem,
  OrdemEnvio,
  Problema,
  dia,
  hoje,
  rotulo,
  usarAcaoEquipamento,
} from './equipamentos.service';

type Acao = '' | 'abrir' | 'editar' | 'iniciar' | 'concluir' | 'cancelar';

@Component({
  selector: 'app-ordem-manutencao',
  imports: [CurrencyPipe, FormsModule, RouterLink, EquipamentosMenuComponent],
  templateUrl: './ordem.component.html',
  styleUrl: './equipamentos.scss',
})
export class OrdemComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(EquipamentosService);
  readonly problema = this.rota.snapshot.data['problema'] as Problema | undefined;
  readonly ordem = signal<Ordem | null>(this.rota.snapshot.data['item'] ?? null);
  readonly acao = signal<Acao>('');
  readonly envio = usarAcaoEquipamento();
  readonly hoje = hoje();
  readonly dia = dia;
  readonly rotulo = rotulo;
  formulario = this.campos();
  data = this.hoje;
  desfecho = '';
  custoReal: number | null = null;

  constructor() {
    if (!this.ordem()) this.abrir('abrir');
  }

  abrir(acao: Acao) {
    this.envio.erro.set('');
    this.formulario = this.campos();
    this.data = this.hoje;
    this.desfecho = '';
    this.custoReal = null;
    this.acao.set(acao);
  }

  private campos(): OrdemEnvio {
    const item = this.ordem();
    return {
      tecnico: item?.tecnico ?? '',
      diagnostico: item?.diagnostico ?? '',
      custoEstimado: item?.custoEstimado ?? null,
    };
  }

  salvar() {
    const item = this.ordem();
    const acao = this.acao();
    if (acao === 'abrir' && this.problema) {
      const corpo = { ...this.formulario, problema: this.problema.identificador };
      this.envio.executar(
        this.api.comando<Ordem>('ordem-manutencao', acao, corpo),
        'Ordem aberta.',
        (ordem) =>
          void this.router.navigate([
            '/core/ordem-manutencao',
            ordem.identificador,
            'visualizacao',
          ]),
      );
      return;
    }
    if (!item || !acao || acao === 'abrir') return;
    const corpos = {
      editar: this.formulario,
      iniciar: { data: this.data },
      concluir: { data: this.data, servicoExecutado: this.desfecho, custoReal: this.custoReal },
      cancelar: { justificativa: this.desfecho },
    };
    const mensagens = {
      editar: 'Ordem atualizada.',
      iniciar: 'Ordem iniciada.',
      concluir: 'Ordem concluída.',
      cancelar: 'Ordem cancelada.',
    };
    this.envio.executar(
      this.api.comando<Ordem>('ordem-manutencao', acao, corpos[acao], item.identificador),
      mensagens[acao],
      (ordem) => {
        this.ordem.set(ordem);
        this.abrir('');
      },
    );
  }
}
