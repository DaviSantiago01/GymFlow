package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno;

import org.apache.commons.lang3.Validate;

/** RN003: opções de objetivo definidas pelo sistema. */
public enum ObjetivoAluno {
    HIPERTROFIA("Hipertrofia"), EMAGRECIMENTO("Emagrecimento"), CONDICIONAMENTO("Condicionamento");

    private final String descricao;

    ObjetivoAluno(String descricao) {
        this.descricao = descricao;
    }

    public static ObjetivoAluno de(String valor) {
        Validate.notBlank(valor, PerfilAlunoServico.OBJETIVO_INVALIDO);
        for (var objetivo : values()) {
            if (objetivo.name().equalsIgnoreCase(valor.strip())) {
                return objetivo;
            }
        }
        throw new IllegalArgumentException(PerfilAlunoServico.OBJETIVO_INVALIDO);
    }

    public String getDescricao() { return descricao; }
}
