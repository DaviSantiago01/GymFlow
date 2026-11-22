package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno;

import java.time.LocalDate;
import java.util.Objects;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Lesão ou limitação registrada para o aluno. É informação de saúde e não aparece em relatórios exportados. */
public record RestricaoFisica(RegiaoCorporal regiao, String descricao, LocalDate registro) implements IObjetoValor {
    public static final int TAMANHO_DESCRICAO = 250;

    public RestricaoFisica {
        Validate.notNull(regiao, PerfilAlunoServico.REGIAO_INVALIDA);
        Validate.notBlank(descricao, PerfilAlunoServico.DESCRICAO_RESTRICAO_INVALIDA);
        descricao = descricao.strip();
        Validate.isTrue(descricao.length() <= TAMANHO_DESCRICAO, PerfilAlunoServico.DESCRICAO_RESTRICAO_INVALIDA);
        Objects.requireNonNull(registro);
    }
}
