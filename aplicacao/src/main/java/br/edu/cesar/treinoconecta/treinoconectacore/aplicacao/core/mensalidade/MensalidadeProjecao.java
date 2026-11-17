package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.Mensalidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.PeriodoCobranca;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.SituacaoMensalidade;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

/** A situação, o atraso e os acréscimos são calculados pelo domínio na data informada. */
public record MensalidadeProjecao(MensalidadeIdentificador identificador, AdesaoPlanoIdentificador adesao,
        AlunoIdentificador aluno, String alunoNome, String planoNome, LocalDate periodoInicio, LocalDate periodoFim,
        LocalDate vencimento, BigDecimal valor, SituacaoMensalidade situacao, long diasAtraso, BigDecimal multa,
        BigDecimal juros, BigDecimal valorDevido) implements IProjecao<Mensalidade> {
    public MensalidadeProjecao(Long identificador, Long adesao, Long aluno, String alunoNome, String planoNome,
            LocalDate periodoInicio, LocalDate periodoFim, LocalDate vencimento, BigDecimal valor,
            SituacaoMensalidade situacao) {
        this(new MensalidadeIdentificador(identificador.toString()), new AdesaoPlanoIdentificador(adesao.toString()),
                new AlunoIdentificador(aluno.toString()), alunoNome, planoNome, periodoInicio, periodoFim, vencimento, valor,
                situacao, 0, BigDecimal.ZERO, BigDecimal.ZERO, valor);
    }

    public MensalidadeProjecao calcularEm(LocalDate hoje) {
        if (situacao != SituacaoMensalidade.ABERTA) {
            return this;
        }
        var mensalidade = new Mensalidade(Optional.of(identificador), adesao, aluno, new PeriodoCobranca(periodoInicio,
                periodoFim), vencimento, valor, SituacaoMensalidade.ABERTA, List.of());
        return new MensalidadeProjecao(identificador, adesao, aluno, alunoNome, planoNome, periodoInicio, periodoFim,
                vencimento, valor, mensalidade.situacaoEm(hoje), mensalidade.diasDeAtraso(hoje), mensalidade.multaEm(hoje),
                mensalidade.jurosEm(hoje), mensalidade.valorDevidoEm(hoje));
    }
}
