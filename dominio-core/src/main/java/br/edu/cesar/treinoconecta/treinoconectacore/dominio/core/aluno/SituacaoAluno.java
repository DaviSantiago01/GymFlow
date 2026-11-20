package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno;

/**
 * Situação calculada do aluno (F005, RN009), nunca gravada. A precedência é: inativo, bloqueado, sem matrícula,
 * inadimplente e, por fim, ativo.
 */
public enum SituacaoAluno {
    ATIVO, INADIMPLENTE, SEM_MATRICULA, BLOQUEADO, INATIVO;

    public static SituacaoAluno calcular(boolean ativo, boolean bloqueado, boolean matriculaAtiva,
            boolean mensalidadeAtrasada) {
        if (!ativo) {
            return INATIVO;
        }
        if (bloqueado) {
            return BLOQUEADO;
        }
        if (!matriculaAtiva) {
            return SEM_MATRICULA;
        }
        return mensalidadeAtrasada ? INADIMPLENTE : ATIVO;
    }
}
