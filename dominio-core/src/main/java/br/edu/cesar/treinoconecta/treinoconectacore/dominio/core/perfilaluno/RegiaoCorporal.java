package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno;

import java.util.EnumSet;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * Regiões em que o aluno pode ter restrição física. Exercícios e aulas registram as regiões para as quais são
 * contraindicados (RN004).
 */
public enum RegiaoCorporal {
    JOELHO("Joelho"), OMBRO("Ombro"), COLUNA("Coluna"), PUNHO("Punho"), COTOVELO("Cotovelo"), QUADRIL("Quadril"),
    TORNOZELO("Tornozelo");

    private final String descricao;

    RegiaoCorporal(String descricao) {
        this.descricao = descricao;
    }

    public static RegiaoCorporal de(String valor) {
        Validate.notBlank(valor, PerfilAlunoServico.REGIAO_INVALIDA);
        for (var regiao : values()) {
            if (regiao.name().equalsIgnoreCase(valor.strip())) {
                return regiao;
            }
        }
        throw new IllegalArgumentException(PerfilAlunoServico.REGIAO_INVALIDA);
    }

    /** Converte a lista gravada como texto separado por vírgulas. */
    public static Set<RegiaoCorporal> lista(String valores) {
        var resultado = EnumSet.noneOf(RegiaoCorporal.class);
        for (var valor : valores.split(",")) {
            if (!valor.isBlank()) {
                resultado.add(de(valor));
            }
        }
        return resultado;
    }

    public String getDescricao() { return descricao; }
}
