package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public record Desafio(Optional<DesafioIdentificador> identificador, String titulo, IndicadorDesafio indicador,
        VigenciaDesafio vigencia, SituacaoDesafio situacao) implements IAgregado<DesafioIdentificador> {
    public Desafio {
        Objects.requireNonNull(identificador); Objects.requireNonNull(titulo); Objects.requireNonNull(indicador);
        Objects.requireNonNull(vigencia); Objects.requireNonNull(situacao);
        if (titulo.isBlank()) throw new IllegalArgumentException("Informe o título do desafio.");
    }
    public static Desafio criar(DadosDesafio dados) {
        return new Desafio(Optional.empty(),dados.titulo().strip(),dados.indicador(),dados.vigencia(),SituacaoDesafio.ABERTO);
    }
    public boolean aceitaInscricao(LocalDate data) { return situacao==SituacaoDesafio.ABERTO && vigencia.aceitaInscricao(data); }
    public boolean encerradoEm(LocalDate data) { return situacao==SituacaoDesafio.ENCERRADO || data.isAfter(vigencia.fim()); }
    public Desafio encerrar() { return new Desafio(identificador,titulo,indicador,vigencia,SituacaoDesafio.ENCERRADO); }
    @Override public Optional<DesafioIdentificador> getIdentificador() { return identificador; }
}
