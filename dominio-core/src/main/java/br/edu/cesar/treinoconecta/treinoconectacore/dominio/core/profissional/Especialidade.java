package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional;

import java.text.Normalizer;
import java.util.Locale;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Duas especialidades são iguais quando diferem só em maiúsculas, acentos ou espaços repetidos. */
public final class Especialidade implements IObjetoValor {
    public static final int TAMANHO_NOME = 60;
    public static final String MENSAGEM = "Informe a especialidade com 2 a " + TAMANHO_NOME + " caracteres";

    private final String nome;
    private final String chave;

    public Especialidade(String nome) {
        Validate.notBlank(nome, MENSAGEM);
        this.nome = nome.strip().replaceAll("\\s+", " ");
        Validate.isTrue(this.nome.length() >= 2 && this.nome.length() <= TAMANHO_NOME, MENSAGEM);
        this.chave = Normalizer.normalize(this.nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    public String getNome() { return nome; }

    /** Forma usada para comparar especialidades e garantir a unicidade no banco. */
    public String chave() { return chave; }

    @Override
    public boolean equals(@Nullable Object objeto) {
        return objeto instanceof Especialidade outra && chave.equals(outra.chave);
    }

    @Override
    public int hashCode() {
        return chave.hashCode();
    }

    @Override
    public String toString() {
        return nome;
    }
}
