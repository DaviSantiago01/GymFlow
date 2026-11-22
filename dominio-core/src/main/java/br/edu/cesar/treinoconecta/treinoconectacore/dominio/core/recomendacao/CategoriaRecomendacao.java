package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAlunoServico;

/**
 * Categorias de recomendação na ordem de prioridade da RN016. O acompanhamento físico não pode ser desativado pelo aluno
 * (RN017) nem perde prioridade pelas respostas dele (RN018).
 */
public enum CategoriaRecomendacao {
    ACOMPANHAMENTO_FISICO("acompanhamento físico", 1, false),
    FREQUENCIA_HORARIOS("frequência e horários", 2, true),
    AJUSTES_TREINO("ajustes no treino", 3, true),
    AULAS_COLETIVAS("aulas coletivas", 3, true),
    PLANOS_SERVICOS("planos e serviços", 4, true);

    private final String descricao;
    private final int prioridade;
    private final boolean opcional;

    CategoriaRecomendacao(String descricao, int prioridade, boolean opcional) {
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.opcional = opcional;
    }

    public static CategoriaRecomendacao de(String valor) {
        Validate.notBlank(valor, PerfilAlunoServico.CATEGORIA_INVALIDA);
        for (var categoria : values()) {
            if (categoria.name().equalsIgnoreCase(valor.strip())) {
                return categoria;
            }
        }
        throw new IllegalArgumentException(PerfilAlunoServico.CATEGORIA_INVALIDA);
    }

    /** Respostas do aluno ajustam a prioridade, exceto no acompanhamento físico e nos ajustes decididos pelo personal. */
    public boolean ajustavelPelasRespostas() {
        return this != ACOMPANHAMENTO_FISICO && this != AJUSTES_TREINO;
    }

    public String getDescricao() { return descricao; }
    public int getPrioridade() { return prioridade; }
    public boolean isOpcional() { return opcional; }
}
