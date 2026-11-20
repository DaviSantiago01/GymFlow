package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Valores configurados pela academia para o controle de acesso: capacidade máxima (F006, RN010), limite de
 * permanência (RN009) e horário em que os planos permitem a entrada (RN003). Os planos atuais não definem horários
 * próprios, então todos seguem o horário de funcionamento da academia; o fechamento 00:00 significa meia-noite.
 */
public record ParametrosAcesso(int capacidadeMaxima, Duration permanenciaMaxima, LocalTime abertura, LocalTime fechamento) {
    public ParametrosAcesso {
        Validate.isTrue(capacidadeMaxima > 0, "A capacidade máxima deve ser positiva");
        Objects.requireNonNull(permanenciaMaxima);
        Validate.isTrue(permanenciaMaxima.isPositive(), "O limite de permanência deve ser positivo");
        Objects.requireNonNull(abertura);
        Objects.requireNonNull(fechamento);
    }

    public boolean dentroDoHorario(LocalTime horario) {
        Objects.requireNonNull(horario);
        boolean aposAbertura = !horario.isBefore(abertura);
        return fechamento.equals(LocalTime.MIDNIGHT) ? aposAbertura : aposAbertura && horario.isBefore(fechamento);
    }
}
