package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/** Associação direta entre um personal trainer e um aluno. O encerramento preserva o vínculo no histórico. */
public final class VinculoPersonalAluno implements IAgregado<VinculoPersonalAlunoIdentificador> {
    private final Optional<VinculoPersonalAlunoIdentificador> identificador;
    private final ProfissionalIdentificador profissional;
    private final AlunoIdentificador aluno;
    private final LocalDate inicio;
    private final Optional<LocalDate> encerramento;
    private final SituacaoVinculo situacao;

    public VinculoPersonalAluno(Optional<VinculoPersonalAlunoIdentificador> identificador,
            ProfissionalIdentificador profissional, AlunoIdentificador aluno, LocalDate inicio,
            Optional<LocalDate> encerramento, SituacaoVinculo situacao) {
        this.identificador = Objects.requireNonNull(identificador);
        this.profissional = Objects.requireNonNull(profissional);
        this.aluno = Objects.requireNonNull(aluno);
        this.inicio = Objects.requireNonNull(inicio);
        this.encerramento = Objects.requireNonNull(encerramento);
        this.situacao = Objects.requireNonNull(situacao);
        Validate.isTrue((situacao == SituacaoVinculo.ATIVO) == encerramento.isEmpty(), "Encerramento do vínculo inconsistente");
        encerramento.ifPresent(data -> Validate.isTrue(!data.isBefore(inicio), "O encerramento não pode anteceder o início"));
    }

    public static VinculoPersonalAluno vincular(Profissional profissional, AlunoIdentificador aluno, LocalDate hoje) {
        Objects.requireNonNull(profissional);
        Validate.validState(profissional.ativo(), VinculoPersonalAlunoServico.PROFISSIONAL_INATIVO);
        Validate.validState(profissional.personalTrainer(), VinculoPersonalAlunoServico.SOMENTE_PERSONAL_TRAINER);
        return new VinculoPersonalAluno(Optional.empty(), profissional.getIdentificador().orElseThrow(), aluno, hoje,
                Optional.empty(), SituacaoVinculo.ATIVO);
    }

    public VinculoPersonalAluno encerrar(LocalDate hoje) {
        Objects.requireNonNull(hoje);
        Validate.validState(ativo(), VinculoPersonalAlunoServico.VINCULO_NAO_ATIVO);
        var data = hoje.isBefore(inicio) ? inicio : hoje;
        return new VinculoPersonalAluno(identificador, profissional, aluno, inicio, Optional.of(data), SituacaoVinculo.ENCERRADO);
    }

    public boolean ativo() {
        return situacao == SituacaoVinculo.ATIVO;
    }

    public Optional<VinculoPersonalAlunoIdentificador> getIdentificador() { return identificador; }
    public ProfissionalIdentificador getProfissional() { return profissional; }
    public AlunoIdentificador getAluno() { return aluno; }
    public LocalDate getInicio() { return inicio; }
    public Optional<LocalDate> getEncerramento() { return encerramento; }
    public SituacaoVinculo getSituacao() { return situacao; }
}
