package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno;

import java.time.Instant;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/** Bloqueio ativo: enquanto existir, o aluno não entra na academia (F006, RN004). */
public record BloqueioAluno(String motivo, Instant inicio) {
    public static final int TAMANHO_MOTIVO = 250;
    public static final String MOTIVO_OBRIGATORIO = "Informe o motivo do bloqueio com até " + TAMANHO_MOTIVO + " caracteres";

    public BloqueioAluno {
        Validate.notBlank(motivo, MOTIVO_OBRIGATORIO);
        motivo = motivo.strip();
        Validate.isTrue(motivo.length() <= TAMANHO_MOTIVO, MOTIVO_OBRIGATORIO);
        Objects.requireNonNull(inicio);
    }
}
