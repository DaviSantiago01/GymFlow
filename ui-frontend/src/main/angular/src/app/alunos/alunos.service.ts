import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ResolveFn } from '@angular/router';
import { IAgregadoAutorizacoes } from '../../d5';

export type SituacaoAluno = 'ATIVO' | 'INADIMPLENTE' | 'SEM_MATRICULA' | 'BLOQUEADO' | 'INATIVO';
export type TipoAlteracao = 'CADASTRO' | 'ALTERACAO' | 'BLOQUEIO' | 'DESBLOQUEIO' | 'DESATIVACAO' | 'REATIVACAO';

export const ROTULOS_SITUACAO_ALUNO: Record<SituacaoAluno, string> = {
  ATIVO: 'Ativo',
  INADIMPLENTE: 'Inadimplente',
  SEM_MATRICULA: 'Sem matrícula',
  BLOQUEADO: 'Bloqueado',
  INATIVO: 'Inativo',
};
export const ROTULOS_ALTERACAO: Record<TipoAlteracao, string> = {
  CADASTRO: 'Cadastro',
  ALTERACAO: 'Alteração',
  BLOQUEIO: 'Bloqueio',
  DESBLOQUEIO: 'Desbloqueio',
  DESATIVACAO: 'Desativação',
  REATIVACAO: 'Reativação',
};

export interface AlunoResumo {
  identificador: string;
  usuario: string;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  plano: string | null;
  situacao: SituacaoAluno;
}
export interface RegistroAcesso {
  identificador: string;
  aluno: string;
  alunoNome: string;
  alunoUsuario: string;
  entrada: string;
  saida: string | null;
  verificar: boolean;
}
export interface Permanencia {
  registro: RegistroAcesso;
  minutos: number;
  excedeuLimite: boolean;
}
export interface Frequencia {
  aluno: string;
  inicio: string;
  fim: string;
  diasNoPeriodo: number;
  diasComparecidos: number;
  visitas: number;
  minutosTotais: number;
  mediaMinutosPorVisita: number;
  registros: Permanencia[];
}
export interface PainelPresenca {
  capacidadeMaxima: number;
  presentes: number;
  permanenciaMaximaMinutos: number;
  abertura: string;
  fechamento: string;
  registros: Permanencia[];
}
export interface FichaAluno {
  identificador: string;
  usuario: string;
  nome: string;
  cpf: string | null;
  nascimento: string | null;
  idade: number | null;
  menorDeIdade: boolean;
  email: string | null;
  telefone: string;
  endereco: string;
  responsavel: { nome: string; cpf: string; telefone: string } | null;
  ativo: boolean;
  motivoBloqueio: string | null;
  inicioBloqueio: string | null;
  cadastroCompleto: boolean;
  situacao: SituacaoAluno;
  mensalidadeAtrasada: boolean;
  matricula: { adesao: string; plano: string; inicio: string; validade: string } | null;
  presente: boolean;
  historico: { instante: string; tipo: TipoAlteracao; descricao: string; responsavel: string }[];
  frequenciaRecente: Frequencia;
}
export interface AlunoEnvio {
  nome: string;
  cpf: string;
  nascimento: string;
  email: string;
  telefone: string;
  endereco: string;
  responsavelNome: string;
  responsavelCpf: string;
  responsavelTelefone: string;
  usuario?: string;
  senha?: string;
}
export interface AutorizacoesAluno extends IAgregadoAutorizacoes {
  gerenciar: boolean;
}

export function cpfFormatado(cpf: string): string {
  return cpf.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, '$1.$2.$3-$4');
}

export function duracao(minutos: number): string {
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  if (horas === 0) return `${resto} min`;
  return resto === 0 ? `${horas} h` : `${horas} h ${resto.toString().padStart(2, '0')} min`;
}

function parametros(valores: Record<string, string | null>): HttpParams {
  let resultado = new HttpParams();
  for (const [chave, valor] of Object.entries(valores)) {
    if (valor) resultado = resultado.set(chave, valor);
  }
  return resultado;
}

@Injectable({ providedIn: 'root' })
export class AlunosService {
  private readonly http = inject(HttpClient);
  private readonly alunos = '/backend/core/aluno';
  private readonly acessos = '/backend/core/registro-acesso';

  listar(busca: string | null, situacao: string | null) {
    return this.http.get<AlunoResumo[]>(`${this.alunos}/gestao`, { params: parametros({ busca, situacao }) });
  }
  ficha(id: string) {
    return this.http.get<FichaAluno>(`${this.alunos}/${id}/ficha`);
  }
  cadastrar(envio: AlunoEnvio) {
    return this.http.post<FichaAluno>(`${this.alunos}/gestao/cadastrar`, envio);
  }
  editar(id: string, envio: AlunoEnvio) {
    return this.http.post<FichaAluno>(`${this.alunos}/${id}/editar`, envio);
  }
  bloquear(id: string, motivo: string) {
    return this.http.post<FichaAluno>(`${this.alunos}/${id}/bloquear`, { motivo });
  }
  desbloquear(id: string) {
    return this.http.post<FichaAluno>(`${this.alunos}/${id}/desbloquear`, null);
  }
  reativar(id: string) {
    return this.http.post<FichaAluno>(`${this.alunos}/${id}/reativar`, null);
  }
  remover(id: string) {
    return this.http.post<{ resultado: 'EXCLUIDO' | 'DESATIVADO' }>(`${this.alunos}/${id}/remover`, null);
  }
  presentes() {
    return this.http.get<PainelPresenca>(`${this.acessos}/presentes`);
  }
  entrada(aluno: string) {
    return this.http.post<PainelPresenca>(`${this.acessos}/aluno/${aluno}/entrada`, null);
  }
  saida(aluno: string) {
    return this.http.post<PainelPresenca>(`${this.acessos}/aluno/${aluno}/saida`, null);
  }
  frequencia(aluno: string, inicio: string | null, fim: string | null) {
    return this.http.get<Frequencia>(`${this.acessos}/aluno/${aluno}/frequencia`, {
      params: parametros({ inicio, fim }),
    });
  }
}

export const resolverAlunos: ResolveFn<AlunoResumo[]> = (rota) =>
  inject(AlunosService).listar(rota.queryParamMap.get('busca'), rota.queryParamMap.get('situacao'));

export const resolverFicha: ResolveFn<FichaAluno | null> = (rota) => {
  const id = rota.paramMap.get('id');
  return id ? inject(AlunosService).ficha(id) : null;
};

export const resolverPresentes: ResolveFn<PainelPresenca> = () => inject(AlunosService).presentes();
