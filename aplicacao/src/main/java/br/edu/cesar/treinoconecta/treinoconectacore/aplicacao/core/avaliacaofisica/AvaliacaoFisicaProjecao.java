package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica;

import java.math.BigDecimal;
import java.time.LocalDate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.ClassificacaoImc;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.SituacaoAvaliacao;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record AvaliacaoFisicaProjecao(AvaliacaoFisicaIdentificador identificador, AlunoIdentificador aluno, String alunoNome,
        String personalNome, LocalDate data, BigDecimal peso, BigDecimal altura, BigDecimal imc,
        ClassificacaoImc classificacaoImc, SituacaoAvaliacao situacao, int versao) implements IProjecao<AvaliacaoFisica> {
    public AvaliacaoFisicaProjecao(Long identificador, Long aluno, String alunoNome, String personalNome, LocalDate data,
            BigDecimal peso, BigDecimal altura, BigDecimal imc, SituacaoAvaliacao situacao, int versao) {
        this(new AvaliacaoFisicaIdentificador(identificador.toString()), new AlunoIdentificador(aluno.toString()), alunoNome,
                personalNome, data, peso, altura, imc, ClassificacaoImc.classificar(imc), situacao, versao);
    }
}
