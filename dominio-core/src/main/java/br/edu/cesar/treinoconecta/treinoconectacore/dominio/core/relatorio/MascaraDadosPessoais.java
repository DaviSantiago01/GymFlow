package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.util.Optional;

/**
 * RN019: CPF e telefone saem mascarados nos arquivos exportados. Os valores podem vir com pontuação do cadastro do
 * aluno; só os dígitos são considerados.
 */
public final class MascaraDadosPessoais {
    private MascaraDadosPessoais() {
    }

    /** Mantém apenas os seis dígitos centrais do CPF: ***.482.917-**. */
    public static String cpf(Optional<String> cpf) {
        return digitos(cpf).filter(valor -> valor.length() == 11)
                .map(valor -> "***." + valor.substring(3, 6) + "." + valor.substring(6, 9) + "-**").orElse("—");
    }

    /** Mantém apenas os quatro últimos dígitos: (**) *****-4312. */
    public static String telefone(Optional<String> telefone) {
        return digitos(telefone).filter(valor -> valor.length() >= 10 && valor.length() <= 11)
                .map(valor -> "(**) " + "*".repeat(valor.length() - 6) + "-" + valor.substring(valor.length() - 4))
                .orElse("—");
    }

    /** Formatação completa para quem pode ver os dados no sistema. */
    public static String telefoneFormatado(Optional<String> telefone) {
        return digitos(telefone).filter(valor -> valor.length() >= 10 && valor.length() <= 11)
                .map(valor -> "(" + valor.substring(0, 2) + ") " + valor.substring(2, valor.length() - 4) + "-"
                        + valor.substring(valor.length() - 4))
                .orElse("—");
    }

    private static Optional<String> digitos(Optional<String> valor) {
        return valor.map(texto -> texto.replaceAll("\\D", "")).filter(texto -> !texto.isEmpty());
    }
}
