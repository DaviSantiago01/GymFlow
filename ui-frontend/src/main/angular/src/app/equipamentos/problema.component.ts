import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EquipamentosMenuComponent } from './equipamentos-menu.component';
import {
  Equipamento,
  EquipamentosService,
  GRAVIDADES,
  Problema,
  ProblemaEnvio,
  dia,
  hoje,
  rotulo,
  usarAcaoEquipamento,
} from './equipamentos.service';

type Acao = '' | 'registrar' | 'editar' | 'resolver' | 'cancelar';

@Component({
  selector: 'app-problema-equipamento',
  imports: [FormsModule, RouterLink, EquipamentosMenuComponent],
  templateUrl: './problema.component.html',
  styleUrl: './equipamentos.scss',
})
export class ProblemaComponent {
  private readonly rota = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(EquipamentosService);
  readonly equipamento = this.rota.snapshot.data['equipamento'] as Equipamento | undefined;
  readonly problema = signal<Problema | null>(this.rota.snapshot.data['item'] ?? null);
  readonly acao = signal<Acao>('');
  readonly envio = usarAcaoEquipamento();
  readonly gravidades = GRAVIDADES;
  readonly hoje = hoje();
  readonly dia = dia;
  readonly rotulo = rotulo;
  formulario = this.campos();
  desfecho = '';
  dataSolucao = this.hoje;

  constructor() {
    if (!this.problema()) this.abrir('registrar');
  }

  abrir(acao: Acao) {
    this.envio.erro.set('');
    this.formulario = this.campos();
    this.desfecho = '';
    this.dataSolucao = this.hoje;
    this.acao.set(acao);
  }

  private campos(): ProblemaEnvio {
    const item = this.problema();
    return {
      titulo: item?.titulo ?? '',
      relato: item?.relato ?? '',
      gravidade: item?.gravidade ?? '',
      risco: item?.risco ?? false,
      evidencia: item?.evidencia ?? '',
      dataRelato: item?.dataRelato ?? this.hoje,
    };
  }

  salvar() {
    const item = this.problema();
    const acao = this.acao();
    if (acao === 'registrar' && this.equipamento) {
      const corpo = { ...this.formulario, equipamento: this.equipamento.identificador };
      this.envio.executar(
        this.api.comando<Problema>('problema-equipamento', acao, corpo),
        'Problema registrado.',
        (problema) =>
          void this.router.navigate([
            '/core/problema-equipamento',
            problema.identificador,
            'visualizacao',
          ]),
      );
      return;
    }
    if (!item || !acao || acao === 'registrar') return;
    const corpos = {
      editar: this.formulario,
      resolver: { data: this.dataSolucao, solucao: this.desfecho },
      cancelar: { justificativa: this.desfecho },
    };
    const mensagens = {
      editar: 'Problema atualizado.',
      resolver: 'Problema resolvido.',
      cancelar: 'Problema cancelado.',
    };
    this.envio.executar(
      this.api.comando<Problema>('problema-equipamento', acao, corpos[acao], item.identificador),
      mensagens[acao],
      (problema) => {
        this.problema.set(problema);
        this.abrir('');
      },
    );
  }
}
