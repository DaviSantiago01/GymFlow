package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public record Aula(Optional<AulaIdentificador> identificador, String titulo, String modalidade,
        Optional<String> descricao, LocalDate data, PeriodoAula periodo, int limite,
        ProfissionalIdentificador professor, SituacaoAula situacao) implements IAgregado<AulaIdentificador> {
    public Aula {
        Objects.requireNonNull(identificador); Objects.requireNonNull(titulo); Objects.requireNonNull(modalidade);
        Objects.requireNonNull(descricao); Objects.requireNonNull(data); Objects.requireNonNull(periodo);
        Objects.requireNonNull(professor); Objects.requireNonNull(situacao);
        Validate.notBlank(titulo); Validate.notBlank(modalidade); Validate.isTrue(limite > 0);
    }

    public static Aula cadastrar(DadosAula dados) {
        Objects.requireNonNull(dados);
        return new Aula(Optional.empty(), dados.titulo().strip(), dados.modalidade().strip(), dados.descricao(),
                dados.data(), dados.periodo(), dados.limite(), dados.professor(), SituacaoAula.ABERTA);
    }

    public Aula editar(DadosAula dados, int ocupacao) {
        Objects.requireNonNull(dados);
        Validate.validState(situacao == SituacaoAula.ABERTA, "A turma cancelada não pode ser editada.");
        Validate.isTrue(dados.limite() >= ocupacao, "O limite não pode ficar abaixo da ocupação atual.");
        return new Aula(identificador, dados.titulo().strip(), dados.modalidade().strip(), dados.descricao(),
                dados.data(), dados.periodo(), dados.limite(), dados.professor(), situacao);
    }

    public Aula cancelar() {
        Validate.validState(situacao == SituacaoAula.ABERTA, "A turma já está cancelada.");
        return new Aula(identificador, titulo, modalidade, descricao, data, periodo, limite, professor, SituacaoAula.CANCELADA);
    }

    @Override public Optional<AulaIdentificador> getIdentificador() { return identificador; }
}
