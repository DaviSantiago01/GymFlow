package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;

/**
 * Consultas sobre registros de outros agregados que referenciam o aluno. Fica no pacote do aluno para que ele não
 * dependa dos pacotes de adesão e de acesso, que já dependem dele.
 */
public interface IAlunoVinculosRepositorio {
    boolean possuiMatriculaAtiva(AlunoIdentificador aluno);
    boolean possuiFrequencia(AlunoIdentificador aluno);
    /** Qualquer outro registro que impeça a exclusão definitiva, como reservas, treinos ou adesões encerradas. */
    boolean possuiOutrosRegistros(AlunoIdentificador aluno);
}
