package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno;

import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Dias da semana e faixa de horário em que o aluno pode treinar. RN003: de 1 a 7 dias. */
public record Disponibilidade(Set<DiaSemana> dias, LocalTime inicio, LocalTime fim) implements IObjetoValor {
    public Disponibilidade {
        Validate.notNull(dias, PerfilAlunoServico.DISPONIBILIDADE_INVALIDA);
        Validate.isTrue(!dias.isEmpty() && dias.size() <= 7, PerfilAlunoServico.DISPONIBILIDADE_INVALIDA);
        dias = Set.copyOf(EnumSet.copyOf(dias));
        Validate.notNull(inicio, PerfilAlunoServico.HORARIO_INVALIDO);
        Validate.notNull(fim, PerfilAlunoServico.HORARIO_INVALIDO);
        Validate.isTrue(inicio.isBefore(fim), PerfilAlunoServico.HORARIO_INVALIDO);
    }

    public int diasPorSemana() {
        return dias.size();
    }

    /** Indica se o compromisso cabe inteiramente na disponibilidade do aluno. */
    public boolean comporta(DiaSemana dia, LocalTime inicioCompromisso, LocalTime fimCompromisso) {
        Objects.requireNonNull(dia);
        return dias.contains(dia) && !inicioCompromisso.isBefore(inicio) && !fimCompromisso.isAfter(fim);
    }
}
