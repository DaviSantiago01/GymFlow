package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional;

import java.time.Duration;

/** Valores definidos pela academia para a jornada dos profissionais. */
public final class ParametrosProfissionais {
    /** Jornada semanal máxima, igual ao limite da CLT. */
    public static final Duration CARGA_HORARIA_SEMANAL_MAXIMA = Duration.ofHours(44);

    private ParametrosProfissionais() {
    }
}
