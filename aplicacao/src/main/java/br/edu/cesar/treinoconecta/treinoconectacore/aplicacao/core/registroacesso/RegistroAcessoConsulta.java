package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

/** Histórico de entradas e saídas (F006, RN011): filtra por aluno e por período, com as datas inclusivas. */
public final class RegistroAcessoConsulta extends Consulta<RegistroAcessoOrdenacao> {
    public static final String PERIODO_INVERTIDO = "A data inicial deve ser anterior ou igual à data final";

    private final Optional<AlunoIdentificador> aluno;
    private final Optional<LocalDate> inicio;
    private final Optional<LocalDate> fim;

    public RegistroAcessoConsulta(Optional<RegistroAcessoOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<AlunoIdentificador> aluno,
            Optional<LocalDate> inicio, Optional<LocalDate> fim) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.aluno = Objects.requireNonNull(aluno);
        this.inicio = Objects.requireNonNull(inicio);
        this.fim = Objects.requireNonNull(fim);
        if (inicio.isPresent() && fim.isPresent()) {
            Validate.isTrue(!inicio.get().isAfter(fim.get()), PERIODO_INVERTIDO);
        }
    }

    public Optional<AlunoIdentificador> getAluno() { return aluno; }
    public Optional<LocalDate> getInicio() { return inicio; }
    public Optional<LocalDate> getFim() { return fim; }
}
