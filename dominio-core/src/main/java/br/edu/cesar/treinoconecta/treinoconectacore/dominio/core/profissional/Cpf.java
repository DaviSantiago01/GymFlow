package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional;

import java.util.Objects;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;

/** CPF guardado só com os 11 dígitos, validado pelos dígitos verificadores. */
public final class Cpf extends ObjetoValorStringAbstrato {
    public static final int TAMANHO = 11;
    public static final String MENSAGEM = "Informe um CPF válido";

    public Cpf(String valor) {
        super(normalizar(valor));
        Validate.isTrue(valido(toString()), MENSAGEM);
    }

    private static String normalizar(String valor) {
        Objects.requireNonNull(valor, MENSAGEM);
        return valor.replaceAll("[.\\-\\s]", "");
    }

    private static boolean valido(String digitos) {
        if (!digitos.matches("\\d{" + TAMANHO + "}") || digitos.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(digitos, 9) == digitos.charAt(9) - '0'
                && digitoVerificador(digitos, 10) == digitos.charAt(10) - '0';
    }

    private static int digitoVerificador(String digitos, int quantidade) {
        int soma = 0;
        for (int indice = 0; indice < quantidade; indice++) {
            soma += (digitos.charAt(indice) - '0') * (quantidade + 1 - indice);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    public String formatado() {
        var valor = toString();
        return valor.substring(0, 3) + "." + valor.substring(3, 6) + "." + valor.substring(6, 9) + "-" + valor.substring(9);
    }
}
