package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.Meta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.SituacaoMeta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.StatusCumprimento;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record MetaProjecao(MetaIdentificador identificador, AlunoIdentificador aluno, String alunoNome,
        IndicadorFisico indicador, BigDecimal valorInicial, BigDecimal valorDesejado, LocalDate inicio, LocalDate fim,
        SituacaoMeta situacao, Optional<LocalDate> dataAtingida, Optional<BigDecimal> valorAtual,
        Optional<BigDecimal> percentualCumprimento, Optional<StatusCumprimento> statusCumprimento)
        implements IProjecao<Meta> {
    public MetaProjecao(Long identificador, Long aluno, String alunoNome, IndicadorFisico indicador, BigDecimal valorInicial,
            BigDecimal valorDesejado, LocalDate inicio, LocalDate fim, SituacaoMeta situacao,
            @Nullable LocalDate dataAtingida) {
        this(new MetaIdentificador(identificador.toString()), new AlunoIdentificador(aluno.toString()), alunoNome, indicador,
                valorInicial, valorDesejado, inicio, fim, situacao, Optional.ofNullable(dataAtingida), Optional.empty(),
                Optional.empty(), Optional.empty());
    }

    public MetaProjecao comCumprimento(BigDecimal atual, BigDecimal percentual, StatusCumprimento status) {
        return new MetaProjecao(identificador, aluno, alunoNome, indicador, valorInicial, valorDesejado, inicio, fim, situacao,
                dataAtingida, Optional.of(atual), Optional.of(percentual), Optional.of(status));
    }
}
