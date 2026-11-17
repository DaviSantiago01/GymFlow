package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.profissional;

import java.util.Set;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ClassificacaoProfissional;

/** CPF e CREF chegam como texto e são validados pelos objetos de valor do domínio. */
public record ProfissionalRequisicao(@Nullable String nome, @Nullable String cpf, @Nullable String cref,
        @Nullable String contato, @Nullable Set<ClassificacaoProfissional> classificacoes) { }
