package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;

/** Registro no Conselho Regional de Educação Física, no formato 000000-G/UF (G: graduado, P: provisionado). */
public final class Cref extends ObjetoValorStringAbstrato {
    public static final int TAMANHO = 11;
    public static final String MENSAGEM = "Informe o CREF no formato 000000-G/UF";
    private static final Set<String> UFS = Set.of("AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MG", "MS",
            "MT", "PA", "PB", "PE", "PI", "PR", "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO");

    public Cref(String valor) {
        super(normalizar(valor));
        var normalizado = toString();
        Validate.isTrue(normalizado.matches("\\d{6}-[GP]/[A-Z]{2}"), MENSAGEM);
        Validate.isTrue(UFS.contains(normalizado.substring(9)), MENSAGEM);
    }

    private static String normalizar(String valor) {
        Objects.requireNonNull(valor, MENSAGEM);
        return valor.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
    }
}
