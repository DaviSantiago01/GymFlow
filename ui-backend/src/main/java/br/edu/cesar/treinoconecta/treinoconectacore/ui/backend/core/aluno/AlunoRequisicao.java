package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.aluno;

import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/** Dados do aluno enviados pela administração; usuário e senha só são usados no cadastro. */
public record AlunoRequisicao(@Nullable String nome, @Nullable String cpf, @Nullable LocalDate nascimento,
        @Nullable String email, @Nullable String telefone, @Nullable String endereco, @Nullable String responsavelNome,
        @Nullable String responsavelCpf, @Nullable String responsavelTelefone, @Nullable String usuario,
        @Nullable String senha) { }
