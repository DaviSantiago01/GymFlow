package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno;

import java.util.Objects;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;

/** Responsável pelo aluno menor de 18 anos (F005, RN004). */
public record ResponsavelLegal(String nome, Cpf cpf, String telefone) {
    public static final int TAMANHO_NOME = 100;
    public static final int TAMANHO_TELEFONE = 20;
    public static final String NOME_OBRIGATORIO = "Informe o nome do responsável legal com 3 a " + TAMANHO_NOME
            + " caracteres";

    public ResponsavelLegal {
        Validate.notBlank(nome, NOME_OBRIGATORIO);
        nome = nome.strip().replaceAll("\s+", " ");
        Validate.isTrue(nome.length() >= 3 && nome.length() <= TAMANHO_NOME, NOME_OBRIGATORIO);
        Objects.requireNonNull(cpf, Cpf.MENSAGEM);
        telefone = Objects.requireNonNullElse(telefone, "").strip();
        Validate.isTrue(telefone.length() <= TAMANHO_TELEFONE, "O telefone do responsável deve ter até "
                + TAMANHO_TELEFONE + " caracteres");
    }
}
