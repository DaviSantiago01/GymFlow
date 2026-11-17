import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ResolveFn } from '@angular/router';
import { IAgregadoAutorizacoes } from '../../d5';

export type Classificacao = 'PROFESSOR' | 'PERSONAL_TRAINER';
export type SituacaoProfissional = 'ATIVO' | 'INATIVO';
export type SituacaoVinculo = 'ATIVO' | 'ENCERRADO';
export type DiaSemana =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export const ROTULOS_CLASSIFICACAO: Record<Classificacao, string> = {
  PROFESSOR: 'Professor',
  PERSONAL_TRAINER: 'Personal trainer',
};
export const ROTULOS_SITUACAO_PROFISSIONAL: Record<SituacaoProfissional, string> = {
  ATIVO: 'Ativo',
  INATIVO: 'Inativo',
};
export const ROTULOS_SITUACAO_VINCULO: Record<SituacaoVinculo, string> = {
  ATIVO: 'Ativo',
  ENCERRADO: 'Encerrado',
};
export const ROTULOS_DIA: Record<DiaSemana, string> = {
  MONDAY: 'Segunda-feira',
  TUESDAY: 'Terça-feira',
  WEDNESDAY: 'Quarta-feira',
  THURSDAY: 'Quinta-feira',
  FRIDAY: 'Sexta-feira',
  SATURDAY: 'Sábado',
  SUNDAY: 'Domingo',
};
export const DIAS_SEMANA = Object.keys(ROTULOS_DIA) as DiaSemana[];
export const SUGESTOES_ESPECIALIDADE = [
  'Musculação',
  'Treinamento funcional',
  'Pilates',
  'Ginástica',
  'Natação',
  'Treinamento para idosos',
  'Reabilitação',
  'Emagrecimento',
];

/** Horários chegam como HH:mm ou HH:mm:ss; a tela mostra só horas e minutos. */
export function horario(valor: string): string {
  return valor.slice(0, 5);
}

export function horas(minutos: number): string {
  const inteiras = Math.floor(minutos / 60);
  const resto = minutos % 60;
  return resto === 0 ? `${inteiras} h` : `${inteiras} h ${resto.toString().padStart(2, '0')} min`;
}

export function cpfFormatado(cpf: string): string {
  return cpf.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
}

export interface Pesquisa<T> {
  resultado: { projecoes: T[]; total: number };
}
export interface ProfissionalResumo {
  identificador: string;
  nome: string;
  cpf: string;
  cref: string;
  contato: string;
  professor: boolean;
  personalTrainer: boolean;
  situacao: SituacaoProfissional;
  vinculosAtivos: number;
}
export interface Disponibilidade {
  identificador: number;
  dia: DiaSemana;
  inicio: string;
  fim: string;
}
export interface Profissional {
  identificador: string;
  nome: string;
  cpf: string;
  cref: string;
  contato: string;
  classificacoes: Classificacao[];
  especialidades: { nome: string }[];
  disponibilidades: Disponibilidade[];
  situacao: SituacaoProfissional;
}
export interface Vinculo {
  identificador: string;
  profissional: string;
  profissionalNome: string;
  aluno: string;
  alunoNome: string;
  inicio: string;
  encerramento: string | null;
  situacao: SituacaoVinculo;
}
export interface DetalheProfissional {
  profissional: Profissional;
  cpfFormatado: string;
  cargaHorariaSemanalMinutos: number;
  limiteSemanalMinutos: number;
  vinculos: Vinculo[];
  alunos: { identificador: string; usuario: string; nome: string }[];
}
export interface ProfissionalEnvio {
  nome: string;
  cpf: string;
  cref: string;
  contato: string;
  classificacoes: Classificacao[];
}
export interface DisponibilidadeEnvio {
  dia: DiaSemana;
  inicio: string;
  fim: string;
}
export interface AutorizacoesProfissional extends IAgregadoAutorizacoes {
  cadastrar: boolean;
  editar: boolean;
  gerenciarEspecialidades: boolean;
  gerenciarDisponibilidades: boolean;
  inativar: boolean;
}

function parametros(valores: Record<string, string | number | null>): HttpParams {
  let resultado = new HttpParams();
  for (const [chave, valor] of Object.entries(valores)) {
    if (valor !== null && valor !== '') resultado = resultado.set(chave, valor);
  }
  return resultado;
}

@Injectable({ providedIn: 'root' })
export class ProfissionaisService {
  private readonly http = inject(HttpClient);
  private readonly profissionais = '/backend/core/profissional';
  private readonly vinculos = '/backend/core/vinculo-personal-aluno';

  listar(busca: string | null, classificacao: string | null, situacao: string | null) {
    return this.http.get<Pesquisa<ProfissionalResumo>>(`${this.profissionais}/pesquisa`, {
      params: parametros({ ordenacao: 'NOME', tamanhoPagina: 1000, busca, classificacao, situacao }),
    });
  }
  visualizar(id: string) {
    return this.http.get<DetalheProfissional>(`${this.profissionais}/${id}/visualizacao`);
  }
  cadastrar(profissional: ProfissionalEnvio) {
    return this.http.post<DetalheProfissional>(`${this.profissionais}/cadastrar`, profissional);
  }
  editar(id: string, profissional: ProfissionalEnvio) {
    return this.http.post<DetalheProfissional>(`${this.profissionais}/${id}/editar`, profissional);
  }
  adicionarEspecialidade(id: string, nome: string) {
    return this.http.post<DetalheProfissional>(`${this.profissionais}/${id}/especialidades/adicionar`, { nome });
  }
  removerEspecialidade(id: string, nome: string) {
    return this.http.post<DetalheProfissional>(`${this.profissionais}/${id}/especialidades/remover`, { nome });
  }
  adicionarDisponibilidade(id: string, disponibilidade: DisponibilidadeEnvio) {
    return this.http.post<DetalheProfissional>(
      `${this.profissionais}/${id}/disponibilidades/adicionar`,
      disponibilidade,
    );
  }
  alterarDisponibilidade(id: string, disponibilidade: number, envio: DisponibilidadeEnvio) {
    return this.http.post<DetalheProfissional>(
      `${this.profissionais}/${id}/disponibilidades/${disponibilidade}/alterar`,
      envio,
    );
  }
  removerDisponibilidade(id: string, disponibilidade: number) {
    return this.http.post<DetalheProfissional>(
      `${this.profissionais}/${id}/disponibilidades/${disponibilidade}/remover`,
      null,
    );
  }
  inativar(id: string) {
    return this.http.post<DetalheProfissional>(`${this.profissionais}/${id}/inativar`, null);
  }
  reativar(id: string) {
    return this.http.post<DetalheProfissional>(`${this.profissionais}/${id}/reativar`, null);
  }
  vincular(profissional: string, aluno: string) {
    return this.http.post<Vinculo>(`${this.vinculos}/vincular`, { profissional, aluno });
  }
  encerrarVinculo(id: string) {
    return this.http.post<Vinculo>(`${this.vinculos}/${id}/encerrar`, null);
  }
}

export const resolverProfissionais: ResolveFn<Pesquisa<ProfissionalResumo>> = (rota) =>
  inject(ProfissionaisService).listar(
    rota.queryParamMap.get('busca'),
    rota.queryParamMap.get('classificacao'),
    rota.queryParamMap.get('situacao'),
  );

export const resolverProfissional: ResolveFn<DetalheProfissional | null> = (rota) => {
  const id = rota.paramMap.get('id');
  return id ? inject(ProfissionaisService).visualizar(id) : null;
};
