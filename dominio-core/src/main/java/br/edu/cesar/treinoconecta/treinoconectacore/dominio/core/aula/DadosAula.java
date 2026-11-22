package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;

public record DadosAula(String titulo, String modalidade, Optional<String> descricao, LocalDate data,
        PeriodoAula periodo, int limite, ProfissionalIdentificador professor) {
    public DadosAula {
        Objects.requireNonNull(titulo); Objects.requireNonNull(modalidade); Objects.requireNonNull(descricao);
        Objects.requireNonNull(data); Objects.requireNonNull(periodo); Objects.requireNonNull(professor);
        if (titulo.isBlank() || modalidade.isBlank() || limite <= 0) throw new IllegalArgumentException("Informe título, modalidade e limite de vagas maior que zero.");
    }
}
