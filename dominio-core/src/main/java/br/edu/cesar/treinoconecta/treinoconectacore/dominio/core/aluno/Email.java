package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno;

import java.util.Locale;
import java.util.Objects;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;

/** E-mail guardado em minúsculas, para que a unicidade não dependa de maiúsculas. */
public final class Email extends ObjetoValorStringAbstrato {
    public static final int TAMANHO = 100;
    public static final String MENSAGEM = "Informe um e-mail válido";
    private static final String FORMATO = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+";

    public Email(String valor) {
        super(normalizar(valor));
        Validate.isTrue(toString().length() <= TAMANHO && toString().matches(FORMATO), MENSAGEM);
    }

    private static String normalizar(String valor) {
        Objects.requireNonNull(valor, MENSAGEM);
        return valor.strip().toLowerCase(Locale.ROOT);
    }
}
