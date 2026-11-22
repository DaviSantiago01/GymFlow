package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno;

import java.util.Objects;
import org.apache.commons.lang3.Validate;

/** RN003: níveis de experiência, em ordem crescente de dificuldade. */
public enum NivelExperiencia {
    INICIANTE("Iniciante"), INTERMEDIARIO("Intermediário"), AVANCADO("Avançado");

    private final String descricao;

    NivelExperiencia(String descricao) {
        this.descricao = descricao;
    }

    public static NivelExperiencia de(String valor) {
        Validate.notBlank(valor, PerfilAlunoServico.NIVEL_INVALIDO);
        for (var nivel : values()) {
            if (nivel.name().equalsIgnoreCase(valor.strip())) {
                return nivel;
            }
        }
        throw new IllegalArgumentException(PerfilAlunoServico.NIVEL_INVALIDO);
    }

    /** RN005: um item não pode exigir nível de dificuldade superior ao do aluno. */
    public boolean comporta(NivelExperiencia exigido) {
        Objects.requireNonNull(exigido);
        return exigido.ordinal() <= ordinal();
    }

    public String getDescricao() { return descricao; }
}
