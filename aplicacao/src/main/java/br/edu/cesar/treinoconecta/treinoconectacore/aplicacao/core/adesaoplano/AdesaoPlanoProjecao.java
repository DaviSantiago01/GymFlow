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

/** Sem recorrência, a quantidade de cobranças e o término previsto ficam vazios: a cobrança é contínua. */
public record AdesaoPlanoProjecao(AdesaoPlanoIdentificador identificador, AlunoIdentificador aluno, String alunoNome,
        PlanoIdentificador plano, String planoNome, BigDecimal valor, int periodicidadeMeses, LocalDate inicio,
        Optional<LocalDate> encerramento, SituacaoAdesao situacao, Optional<Integer> quantidadePeriodos,
        Optional<LocalDate> terminoPrevisto) implements IProjecao<AdesaoPlano> {
    public AdesaoPlanoProjecao(Long identificador, Long aluno, String alunoNome, Long plano, String planoNome,
            BigDecimal valor, Integer periodicidadeMeses, LocalDate inicio, @Nullable LocalDate encerramento,
            SituacaoAdesao situacao, @Nullable Integer quantidadePeriodos) {
        this(new AdesaoPlanoIdentificador(identificador.toString()), new AlunoIdentificador(aluno.toString()), alunoNome,
                new PlanoIdentificador(plano.toString()), planoNome, valor, periodicidadeMeses, inicio,
                Optional.ofNullable(encerramento), situacao, Optional.ofNullable(quantidadePeriodos),
                Optional.ofNullable(quantidadePeriodos)
                        .map(quantidade -> inicio.plusMonths((long) quantidade * periodicidadeMeses).minusDays(1)));
    }
}
