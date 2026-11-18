package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

public final class DadosOperacionais {
    private DadosOperacionais() { }

    public static String texto(String valor, int limite, String campo) {
        Validate.notBlank(valor, "%s é obrigatório", campo);
        var texto = valor.strip();
        Validate.isTrue(texto.length() <= limite, "%s deve ter até %s caracteres", campo, limite);
        return texto;
    }

    public static Optional<String> textoOpcional(Optional<String> valor, int limite, String campo) {
        return Objects.requireNonNull(valor).map(String::strip).filter(s -> !s.isEmpty())
                .map(s -> texto(s, limite, campo));
    }

    public static void validarData(LocalDate data, LocalDate minimo, LocalDate hoje) {
        Validate.isTrue(!Objects.requireNonNull(data).isBefore(minimo) && !data.isAfter(hoje),
                "Informe uma data entre %s e %s", minimo, hoje);
    }
}
