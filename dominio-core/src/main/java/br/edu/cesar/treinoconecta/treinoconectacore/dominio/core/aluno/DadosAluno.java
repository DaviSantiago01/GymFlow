package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;

/**
 * Dados cadastrais informados pela administração (F005). O nome segue RN002, a data de nascimento RN003 e o
 * responsável legal RN004, que depende da data atual e por isso é validado em {@link #validarEm(LocalDate)}.
 */
public record DadosAluno(String nome, Cpf cpf, LocalDate nascimento, Optional<Email> email, String telefone,
        String endereco, Optional<ResponsavelLegal> responsavel) {
    public static final int TAMANHO_NOME = 100;
    public static final int TAMANHO_TELEFONE = 20;
    public static final int TAMANHO_ENDERECO = 200;
    public static final int MAIORIDADE = 18;
    public static final String NOME_OBRIGATORIO = "Informe o nome do aluno com 3 a " + TAMANHO_NOME + " caracteres";
    public static final String NASCIMENTO_OBRIGATORIO = "Informe a data de nascimento";
    public static final String NASCIMENTO_FUTURO = "A data de nascimento não pode ser posterior à data atual";
    public static final String RESPONSAVEL_OBRIGATORIO = "Alunos menores de 18 anos devem ter um responsável legal";
    public static final String RESPONSAVEL_PROPRIO_CPF = "O responsável legal deve ter um CPF diferente do aluno";

    public DadosAluno {
        Validate.notBlank(nome, NOME_OBRIGATORIO);
        nome = nome.strip().replaceAll("\\s+", " ");
        Validate.isTrue(nome.length() >= 3 && nome.length() <= TAMANHO_NOME, NOME_OBRIGATORIO);
        Objects.requireNonNull(cpf, Cpf.MENSAGEM);
        Objects.requireNonNull(nascimento, NASCIMENTO_OBRIGATORIO);
        Objects.requireNonNull(email);
        telefone = Objects.requireNonNull(telefone).strip();
        Validate.isTrue(telefone.length() <= TAMANHO_TELEFONE, "O telefone deve ter até " + TAMANHO_TELEFONE + " caracteres");
        endereco = Objects.requireNonNull(endereco).strip();
        Validate.isTrue(endereco.length() <= TAMANHO_ENDERECO, "O endereço deve ter até " + TAMANHO_ENDERECO + " caracteres");
        Objects.requireNonNull(responsavel);
    }

    /** RN003 e RN004: nascimento até hoje e, para menores de idade, um responsável com outro CPF. */
    public DadosAluno validarEm(LocalDate hoje) {
        Objects.requireNonNull(hoje);
        Validate.isTrue(!nascimento.isAfter(hoje), NASCIMENTO_FUTURO);
        Validate.isTrue(!menorDeIdadeEm(hoje) || responsavel.isPresent(), RESPONSAVEL_OBRIGATORIO);
        responsavel.ifPresent(item -> Validate.isTrue(!item.cpf().equals(cpf), RESPONSAVEL_PROPRIO_CPF));
        return this;
    }

    public boolean menorDeIdadeEm(LocalDate hoje) {
        return Period.between(nascimento, hoje).getYears() < MAIORIDADE;
    }
}
