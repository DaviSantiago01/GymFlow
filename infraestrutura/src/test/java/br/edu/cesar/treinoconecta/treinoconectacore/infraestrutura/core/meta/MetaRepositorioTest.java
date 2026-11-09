package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.SituacaoAvaliacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.Meta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.SituacaoMeta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica.AvaliacaoFisicaJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica.IAvaliacaoFisicaRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;

@DataJpaTest
class MetaRepositorioTest {
    private static final LocalDate INICIO = LocalDate.parse("2026-11-01");
    private static final LocalDate FIM = LocalDate.parse("2026-12-31");

    @Autowired
    private IMetaRepositorioJpa repositorioJpa;
    @Autowired
    private IAlunoRepositorioJpa alunosJpa;
    @Autowired
    private IPersonalRepositorioJpa personaisJpa;
    @Autowired
    private IAvaliacaoFisicaRepositorioJpa avaliacoesJpa;
    @Autowired
    private EntityManager entidades;
    private MetaRepositorio repositorio;
    private AlunoJpa alunoJpa;
    private PersonalJpa personalJpa;
    private AlunoIdentificador aluno;
    private PersonalIdentificador personal;
    private AvaliacaoFisicaIdentificador avaliacao;

    @BeforeEach
    void configurar() {
        repositorio = new MetaRepositorio(repositorioJpa, alunosJpa, personaisJpa, avaliacoesJpa);
        alunoJpa = alunosJpa.save(new AlunoJpa(null, "u", "Aluno"));
        personalJpa = personaisJpa.save(new PersonalJpa(null, "p", "Personal"));
        aluno = new AlunoIdentificador(alunoJpa.getIdentificador().toString());
        personal = new PersonalIdentificador(personalJpa.getIdentificador().toString());
        avaliacao = criarAvaliacao(LocalDate.parse("2026-10-01"));
    }

    private AvaliacaoFisicaIdentificador criarAvaliacao(LocalDate data) {
        var salva = avaliacoesJpa.save(new AvaliacaoFisicaJpa(null, alunoJpa, personalJpa, data, new BigDecimal("80"),
                new BigDecimal("180"), new BigDecimal("24.69"), SituacaoAvaliacao.VALIDA, 1, null,
                Instant.parse("2026-10-01T12:00:00Z"), new ArrayList<>()));
        return new AvaliacaoFisicaIdentificador(salva.getIdentificador().toString());
    }

    private Meta meta(LocalDate inicio, LocalDate fim) {
        return new Meta(Optional.empty(), aluno, personal, IndicadorFisico.CINTURA, new BigDecimal("90"), new BigDecimal("80"),
                inicio, fim, avaliacao, SituacaoMeta.ATIVA, Optional.empty());
    }

    private void limparContexto() {
        entidades.flush();
        entidades.clear();
    }

    @Test
    void incluiEAlteraSituacao() {
        var incluida = repositorio.incluir(meta(INICIO, FIM));
        limparContexto();

        repositorio.alterar(incluida.marcarAtingida(INICIO.plusDays(5)));
        limparContexto();

        var encontrada = repositorio.buscarPorIdentificador(incluida.getIdentificador().orElseThrow());
        assertEquals(SituacaoMeta.ATINGIDA, encontrada.getSituacao());
        assertEquals(INICIO.plusDays(5), encontrada.getDataAtingida().orElseThrow());
        assertEquals(avaliacao, encontrada.getAvaliacaoReferencia());
    }

    @Test
    void detectaSobreposicaoDePeriodo() {
        repositorio.incluir(meta(INICIO, FIM));
        limparContexto();
        assertTrue(repositorio.existeAtivaNoPeriodo(aluno, IndicadorFisico.CINTURA, FIM, FIM.plusDays(10), Optional.empty()));
        assertFalse(repositorio.existeAtivaNoPeriodo(aluno, IndicadorFisico.CINTURA, FIM.plusDays(1), FIM.plusDays(10), Optional.empty()));
        assertFalse(repositorio.existeAtivaNoPeriodo(aluno, IndicadorFisico.BRACO, INICIO, FIM, Optional.empty()));
    }

    @Test
    void ignoraAPropriaMetaAoAlterar() {
        var incluida = repositorio.incluir(meta(INICIO, FIM));
        limparContexto();
        assertFalse(repositorio.existeAtivaNoPeriodo(aluno, IndicadorFisico.CINTURA, INICIO, FIM, incluida.getIdentificador()));
    }

    @Test
    void metaEncerradaNaoContaComoAtiva() {
        var incluida = repositorio.incluir(meta(INICIO, FIM));
        repositorio.alterar(incluida.encerrar());
        limparContexto();
        assertFalse(repositorio.existeAtivaNoPeriodo(aluno, IndicadorFisico.CINTURA, INICIO, FIM, Optional.empty()));
        assertTrue(repositorio.buscarAtivasPorAluno(aluno).isEmpty());
    }

    @Test
    void transfereReferenciaParaAvaliacaoCorrigida() {
        repositorio.incluir(meta(INICIO, FIM));
        limparContexto();
        var corrigida = criarAvaliacao(LocalDate.parse("2026-10-02"));
        limparContexto();

        repositorio.transferirReferencia(avaliacao, corrigida);

        assertFalse(repositorio.existePorAvaliacaoReferencia(avaliacao));
        assertTrue(repositorio.existePorAvaliacaoReferencia(corrigida));
    }
}
