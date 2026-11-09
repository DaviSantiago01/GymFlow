package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.SituacaoAvaliacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.SituacaoMeta;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica.AvaliacaoFisicaJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica.IAvaliacaoFisicaRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

@DataJpaTest
class MetaAplicacaoRepositorioTest {
    private static final LocalDate INICIO = LocalDate.parse("2026-11-01");

    @Autowired
    private IMetaRepositorioJpa repositorioJpa;
    @Autowired
    private IAlunoRepositorioJpa alunosJpa;
    @Autowired
    private IPersonalRepositorioJpa personaisJpa;
    @Autowired
    private IAvaliacaoFisicaRepositorioJpa avaliacoesJpa;
    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private MetaAplicacaoRepositorio repositorio;
    private AlunoJpa mariana;
    private AlunoJpa pedro;
    private PersonalJpa personal;

    @BeforeEach
    void configurar() {
        repositorio = new MetaAplicacaoRepositorio(repositorioJpa, seguranca);
        mariana = alunosJpa.save(new AlunoJpa(null, "mariana", "Mariana"));
        pedro = alunosJpa.save(new AlunoJpa(null, "pedro", "Pedro"));
        personal = personaisJpa.save(new PersonalJpa(null, "p", "Personal"));
    }

    private void incluir(AlunoJpa aluno, IndicadorFisico indicador, SituacaoMeta situacao) {
        var avaliacao = avaliacoesJpa.save(new AvaliacaoFisicaJpa(null, aluno, personal, LocalDate.parse("2026-10-01"),
                new BigDecimal("80"), new BigDecimal("180"), new BigDecimal("24.69"), SituacaoAvaliacao.SUBSTITUIDA, 1, null,
                Instant.parse("2026-10-01T12:00:00Z"), new ArrayList<>()));
        repositorioJpa.save(new MetaJpa(null, aluno, personal, indicador, new BigDecimal("90"), new BigDecimal("80"), INICIO,
                INICIO.plusDays(30), avaliacao, situacao, null));
    }

    private static MetaConsulta consultar(Optional<String> aluno, Optional<SituacaoMeta> situacao) {
        return new MetaConsulta(Optional.of(MetaOrdenacao.INDICADOR), Optional.empty(), Optional.empty(), Optional.empty(), aluno,
                situacao);
    }

    @Test
    void personalFiltraPorAlunoESituacao() {
        incluir(mariana, IndicadorFisico.CINTURA, SituacaoMeta.ATIVA);
        incluir(mariana, IndicadorFisico.BRACO, SituacaoMeta.ENCERRADA);
        incluir(pedro, IndicadorFisico.CINTURA, SituacaoMeta.ATIVA);
        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(false);

        var resultado = repositorio.pesquisar(consultar(Optional.of(mariana.getIdentificador().toString()),
                Optional.of(SituacaoMeta.ATIVA)));

        assertEquals(1, resultado.getTotal());
        var projecao = resultado.getProjecoes().get(0);
        assertEquals("Mariana", projecao.alunoNome());
        assertEquals(IndicadorFisico.CINTURA, projecao.indicador());
        assertTrue(projecao.percentualCumprimento().isEmpty());
    }

    @Test
    void alunoSoEnxergaAsProprias() {
        incluir(mariana, IndicadorFisico.CINTURA, SituacaoMeta.ATIVA);
        incluir(pedro, IndicadorFisico.CINTURA, SituacaoMeta.ATIVA);
        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(true);
        when(seguranca.getUsuario()).thenReturn("pedro");

        var resultado = repositorio.pesquisar(consultar(Optional.empty(), Optional.empty()));

        assertEquals(1, resultado.getTotal());
        assertEquals("Pedro", resultado.getProjecoes().get(0).alunoNome());
    }
}
