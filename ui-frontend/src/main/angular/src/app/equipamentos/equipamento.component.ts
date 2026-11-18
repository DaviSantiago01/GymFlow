import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { EquipamentosMenuComponent } from './equipamentos-menu.component';
import {
  AREAS,
  RESULTADOS,
  SITUACOES,
  Equipamento,
  EquipamentoEnvio,
  EquipamentosService,
  Operacoes,
  dia,
  hoje,
  rotulo,
  usarAcaoEquipamento,
} from './equipamentos.service';

type Acao = '' | 'cadastrar' | 'editar' | 'movimentar' | 'inspecionar' | 'inativar' | 'excluir';

@Component({
  selector: 'app-equipamento',
  imports: [FormsModule, RouterLink, EquipamentosMenuComponent],
  templateUrl: './equipamento.component.html',
  styleUrl: './equipamentos.scss',
})
export class EquipamentoComponent {
  private readonly api = inject(EquipamentosService);
  private readonly router = inject(Router);
  readonly dados = signal<Operacoes | null>(inject(ActivatedRoute).snapshot.data['item'] ?? null);
  readonly acao = signal<Acao>('');
  readonly envio = usarAcaoEquipamento();
  readonly areas = AREAS;
  readonly situacoes = SITUACOES.filter((valor) => valor !== 'INATIVO');
  readonly resultados = RESULTADOS;
  readonly dia = dia;
  readonly rotulo = rotulo;
  readonly hoje = this.dados()?.hoje ?? hoje();
  formulario = this.ficha(this.dados()?.equipamento);
  operacao = this.camposOperacao(this.dados()?.equipamento);

  constructor() {
    if (!this.dados()) this.abrir('cadastrar');
  }

  abrir(acao: Acao) {
    this.envio.erro.set('');
    this.formulario = this.ficha(this.dados()?.equipamento);
    this.operacao = this.camposOperacao(this.dados()?.equipamento);
    this.acao.set(acao);
  }

  private ficha(item?: Equipamento): EquipamentoEnvio {
    return {
      nome: item?.nome ?? '',
      patrimonio: item?.patrimonio ?? '',
      serie: item?.serie ?? '',
      tipo: item?.tipo ?? '',
      marca: item?.marca ?? '',
      modelo: item?.modelo ?? '',
      aquisicao: item?.aquisicao ?? this.hoje,
      area: item?.area ?? '',
      periodicidade: item?.periodicidade ?? 30,
    };
  }

  private camposOperacao(item?: Equipamento) {
    return {
      area: item?.area ?? '',
      situacao: item?.situacao ?? '',
      resultado: 'APROVADO',
      data: this.hoje,
      observacao: '',
    };
  }

  salvar() {
    const acao = this.acao();
    const dados = this.dados();
    const id = dados?.equipamento.identificador;
    if (acao === 'cadastrar' || acao === 'editar') {
      const pedido = this.api.comando<Equipamento>('equipamento', acao, this.formulario, id);
      if (acao === 'cadastrar') {
        this.envio.executar(
          pedido,
          'Equipamento cadastrado.',
          (item) =>
            void this.router.navigate(['/core/equipamento', item.identificador, 'visualizacao']),
        );
      } else if (dados) {
        this.envio.executar(pedido, 'Cadastro atualizado.', (equipamento) =>
          this.atualizar({ ...dados, equipamento }),
        );
      }
      return;
    }
    if (!id || !dados) return;
    if (acao === 'excluir') {
      this.envio.executar(
        this.api.excluir(id),
        'Equipamento excluído.',
        () => void this.router.navigate(['/core/equipamento/pesquisa']),
      );
    } else if (acao === 'inativar') {
      this.envio.executar(
        this.api.comando<Equipamento>('equipamento', acao, {}, id),
        'Equipamento inativado.',
        (equipamento) => this.atualizar({ ...dados, equipamento, podeExcluir: false }),
      );
    } else if (acao === 'movimentar' || acao === 'inspecionar') {
      const { area, situacao, resultado, data, observacao } = this.operacao;
      const corpo =
        acao === 'movimentar'
          ? { area, situacao, observacao }
          : { area, situacao, resultado, data, observacao };
      const pedido = this.api
        .comando<void>('registro-equipamento', acao, corpo, id)
        .pipe(switchMap(() => this.api.operacoes(id)));
      this.envio.executar(
        pedido,
        acao === 'movimentar' ? 'Mudança registrada.' : 'Inspeção registrada.',
        (atualizado) => this.atualizar(atualizado),
      );
    }
  }

  private atualizar(dados: Operacoes) {
    this.dados.set(dados);
    this.abrir('');
  }
}
