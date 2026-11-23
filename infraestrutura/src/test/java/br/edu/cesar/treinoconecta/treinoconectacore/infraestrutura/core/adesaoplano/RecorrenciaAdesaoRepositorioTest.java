package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.SituacaoAdesao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpaTeste;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano.IPlanoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano.PlanoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano.PlanoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

@DataJpaTest
class RecorrenciaAdesaoRepositorioTest {
    private static final LocalDate INICIO = LocalDate.parse("2026-11-17");

    @Autowired
    private EntityManager entidades;
    @Autowired
    private IAdesaoPlanoRepositorioJpa adesoesJpa;
    @Autowired
    private IPlanoRepositorioJpa planosJpa;
    @Autowired
    private IAlunoRepositorioJpa alunosJpa;
    private AdesaoPlanoRepositorio adesoes;
    private AlunoJpa mariana;
    private Plano mensal;

    @BeforeEach
    void configurar() {
        adesoes = new AdesaoPlanoRepositorio(adesoesJpa, alunosJpa, planosJpa);
        mariana = alunosJpa.save(AlunoJpaTeste.novo(entidades, "mariana-recorrencia", "Mariana"));
        mensal = new PlanoRepositorio(planosJpa).incluir(Plano.cadastrar("Mensal recorrente", new BigDecimal("99.90"), 1));
    }

    private AlunoIdentificador aluno() {
        return new AlunoIdentificador(mariana.getIdentificador().toString());
    }

    private void limparContexto() {
        entidades.flush();
        entidades.clear();
    }

    @Test
    void recorrenciaEConclusaoSaoPersistidas() {
        var adesao = adesoes.incluir(AdesaoPlano.aderir(aluno(), mensal, INICIO, Optional.of(5)));
        limparContexto();
        var salva = adesoes.buscarPorIdentificador(adesao.getIdentificador().orElseThrow());
        assertEquals(Optional.of(5), salva.getQuantidadePeriodos());
        assertEquals(Optional.of(LocalDate.parse("2027-04-16")), salva.terminoPrevisto());

        adesoes.alterar(salva.concluir(LocalDate.parse("2027-04-17")));
        limparContexto();
        var concluida = adesoes.buscarPorIdentificador(adesao.getIdentificador().orElseThrow());
        assertEquals(SituacaoAdesao.CONCLUIDA, concluida.getSituacao());
        assertEquals(Optional.of(LocalDate.parse("2027-04-17")), concluida.getEncerramento());
        assertEquals(Optional.empty(), adesoes.buscarAtivaDoAluno(aluno()));
    }

    @Test
    void adesaoSemRecorrenciaContinuaSemQuantidade() {
        var adesao = adesoes.incluir(AdesaoPlano.aderir(aluno(), mensal, INICIO));
        limparContexto();
        var salva = adesoes.buscarPorIdentificador(adesao.getIdentificador().orElseThrow());
        assertEquals(Optional.empty(), salva.getQuantidadePeriodos());
        assertEquals(Optional.empty(), salva.terminoPrevisto());
    }

    @Test
    void projecaoInformaQuantidadeETerminoPrevisto() {
        var adesao = adesoes.incluir(AdesaoPlano.aderir(aluno(), mensal, INICIO, Optional.of(3)));
        limparContexto();
        var projecao = new AdesaoPlanoAplicacaoRepositorio(adesoesJpa, mock(ISegurancaServico.class))
                .projetar(adesao.getIdentificador().orElseThrow());
        assertEquals(Optional.of(3), projecao.quantidadePeriodos());
        assertEquals(Optional.of(LocalDate.parse("2027-02-16")), projecao.terminoPrevisto());
    }

    @Test
    void bancoRecusaQuantidadeForaDoIntervalo() {
        var plano = planosJpa.getReferenceById(Long.valueOf(mensal.getIdentificador().orElseThrow().toString()));
        assertThrows(DataIntegrityViolationException.class, () -> adesoesJpa.saveAndFlush(
                new AdesaoPlanoJpa(null, mariana, plano, 1, INICIO, null, SituacaoAdesao.ATIVA, 0)));
    }

    @Test
    void bancoRecusaAdesaoConcluidaSemRecorrencia() {
        PlanoJpa plano = planosJpa.getReferenceById(Long.valueOf(mensal.getIdentificador().orElseThrow().toString()));
        assertThrows(DataIntegrityViolationException.class, () -> adesoesJpa.saveAndFlush(
                new AdesaoPlanoJpa(null, mariana, plano, 1, INICIO, INICIO.plusMonths(1), SituacaoAdesao.CONCLUIDA, null)));
    }
}
