package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.AvaliacaoFisicaOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.ClassificacaoImc;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.SituacaoAvaliacao;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

@DataJpaTest
class AvaliacaoFisicaAplicacaoRepositorioTest {
    private static final Instant AGORA = Instant.parse("2026-11-09T12:00:00Z");
    private static final LocalDate DATA = LocalDate.parse("2026-11-01");

    @Autowired
    private IAvaliacaoFisicaRepositorioJpa repositorioJpa;
    @Autowired
    private IAlunoRepositorioJpa alunosJpa;
    @Autowired
    private IPersonalRepositorioJpa personaisJpa;
    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private AvaliacaoFisicaAplicacaoRepositorio repositorio;
    private AlunoJpa mariana;
    private AlunoJpa pedro;
    private PersonalJpa personal;

    @BeforeEach
    void configurar() {
        repositorio = new AvaliacaoFisicaAplicacaoRepositorio(repositorioJpa, seguranca);
        mariana = alunosJpa.save(new AlunoJpa(null, "mariana", "Mariana"));
        pedro = alunosJpa.save(new AlunoJpa(null, "pedro", "Pedro"));
        personal = personaisJpa.save(new PersonalJpa(null, "p", "Rafael"));
    }

    private void incluir(AlunoJpa aluno, LocalDate data, SituacaoAvaliacao situacao) {
        repositorioJpa.save(new AvaliacaoFisicaJpa(null, aluno, personal, data, new BigDecimal("80"), new BigDecimal("180"),
                new BigDecimal("24.69"), situacao, 1, null, AGORA, new ArrayList<>()));
    }

    private static AvaliacaoFisicaConsulta consultar(Optional<String> aluno, Optional<SituacaoAvaliacao> situacao) {
        return new AvaliacaoFisicaConsulta(Optional.of(AvaliacaoFisicaOrdenacao.DATA), Optional.of(true), Optional.empty(),
                Optional.empty(), aluno, situacao);
    }

    @Test
    void personalFiltraPorAlunoESituacao() {
        incluir(mariana, DATA, SituacaoAvaliacao.VALIDA);
        incluir(mariana, DATA.plusDays(1), SituacaoAvaliacao.SUBSTITUIDA);
        incluir(pedro, DATA, SituacaoAvaliacao.VALIDA);
        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(false);

        var resultado = repositorio.pesquisar(consultar(Optional.of(mariana.getIdentificador().toString()),
                Optional.of(SituacaoAvaliacao.VALIDA)));

        assertEquals(1, resultado.getTotal());
        var projecao = resultado.getProjecoes().get(0);
        assertEquals("Mariana", projecao.alunoNome());
        assertEquals("Rafael", projecao.personalNome());
        assertEquals(ClassificacaoImc.PESO_NORMAL, projecao.classificacaoImc());
    }

    @Test
    void alunoSoEnxergaAsProprias() {
        incluir(mariana, DATA, SituacaoAvaliacao.VALIDA);
        incluir(pedro, DATA, SituacaoAvaliacao.VALIDA);
        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(true);
        when(seguranca.getUsuario()).thenReturn("pedro");

        var resultado = repositorio.pesquisar(consultar(Optional.empty(), Optional.empty()));

        assertEquals(1, resultado.getTotal());
        assertEquals("Pedro", resultado.getProjecoes().get(0).alunoNome());
    }

    @Test
    void listaValidasEmOrdemCronologica() {
        incluir(mariana, DATA.plusDays(5), SituacaoAvaliacao.VALIDA);
        incluir(mariana, DATA, SituacaoAvaliacao.VALIDA);
        incluir(mariana, DATA.plusDays(9), SituacaoAvaliacao.EXCLUIDA);

        var validas = repositorio.listarValidas(new AlunoIdentificador(mariana.getIdentificador().toString()));

        assertEquals(2, validas.size());
        assertEquals(DATA, validas.get(0).data());
    }
}
