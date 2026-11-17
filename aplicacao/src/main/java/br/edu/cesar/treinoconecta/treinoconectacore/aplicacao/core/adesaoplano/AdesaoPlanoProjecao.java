package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.adesaoplano;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.SituacaoAdesao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record AdesaoPlanoProjecao(AdesaoPlanoIdentificador identificador, AlunoIdentificador aluno, String alunoNome,
        PlanoIdentificador plano, String planoNome, BigDecimal valor, int periodicidadeMeses, LocalDate inicio,
        Optional<LocalDate> encerramento, SituacaoAdesao situacao) implements IProjecao<AdesaoPlano> {
    public AdesaoPlanoProjecao(Long identificador, Long aluno, String alunoNome, Long plano, String planoNome,
            BigDecimal valor, Integer periodicidadeMeses, LocalDate inicio, @Nullable LocalDate encerramento,
            SituacaoAdesao situacao) {
        this(new AdesaoPlanoIdentificador(identificador.toString()), new AlunoIdentificador(aluno.toString()), alunoNome,
                new PlanoIdentificador(plano.toString()), planoNome, valor, periodicidadeMeses, inicio,
                Optional.ofNullable(encerramento), situacao);
    }
}
