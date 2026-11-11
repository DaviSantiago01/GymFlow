package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.ClassificacaoImc;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.MedidaCorporal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.SituacaoAvaliacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.IPersonalRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;

@DataJpaTest
class AvaliacaoFisicaRepositorioTest {
    private static final Instant AGORA = Instant.parse("2026-11-09T12:00:00Z");
    private static final LocalDate DATA = LocalDate.parse("2026-11-01");

    @Autowired
    private IAvaliacaoFisicaRepositorioJpa repositorioJpa;
    @Autowired
    private IAlunoRepositorioJpa alunosJpa;
    @Autowired
    private IPersonalRepositorioJpa personaisJpa;
    @Autowired
    private EntityManager entidades;
    private AvaliacaoFisicaRepositorio repositorio;
    private AlunoIdentificador aluno;
    private PersonalIdentificador personal;

    @BeforeEach
    void configurar() {
        repositorio = new AvaliacaoFisicaRepositorio(repositorioJpa, alunosJpa, personaisJpa);
        aluno = new AlunoIdentificador(alunosJpa.save(new AlunoJpa(null, "u", "Aluno")).getIdentificador().toString());
        personal = new PersonalIdentificador(personaisJpa.save(new PersonalJpa(null, "p", "Personal")).getIdentificador().toString());
    }

    private AvaliacaoFisica avaliacao(LocalDate data, String cintura) {
        var medidas = List.of(new MedidaCorporal(IndicadorFisico.CINTURA, new BigDecimal(cintura)),
                new MedidaCorporal(IndicadorFisico.BRACO, new BigDecimal("35")));
        return new AvaliacaoFisica(Optional.empty(), aluno, personal, data, new BigDecimal("80"), new BigDecimal("180"), medidas,
                SituacaoAvaliacao.VALIDA, 1, Optional.empty(), AGORA);
    }

    private void limparContexto() {
        entidades.flush();
        entidades.clear();
    }

    @Test
    void incluiEBuscaMantendoMedidasEImc() {
        var incluida = repositorio.incluir(avaliacao(DATA, "90"));
        limparContexto();

        var encontrada = repositorio.buscarPorIdentificador(incluida.getIdentificador().orElseThrow());

        assertEquals(aluno, encontrada.getAluno());
        assertEquals(DATA, encontrada.getData());
        assertEquals(0, new BigDecimal("24.69").compareTo(encontrada.getImc()));
        assertEquals(ClassificacaoImc.PESO_NORMAL, encontrada.getClassificacaoImc());
        assertEquals(IndicadorFisico.CINTURA, encontrada.getMedidas().get(0).indicador());
        assertEquals(IndicadorFisico.BRACO, encontrada.getMedidas().get(1).indicador());
    }

    @Test
    void buscaSomenteValidasEmOrdemCronologica() {
        repositorio.incluir(avaliacao(DATA.plusDays(10), "88"));
        repositorio.incluir(avaliacao(DATA, "90"));
        var excluida = repositorio.incluir(avaliacao(DATA.plusDays(20), "86"));
        repositorio.alterar(excluida.excluir());
        limparContexto();

        var validas = repositorio.buscarValidasPorAluno(aluno);

        assertEquals(2, validas.size());
        assertEquals(DATA, validas.get(0).getData());
    }

    @Test
    void correcaoReferenciaAVersaoAnterior() {
        var original = repositorio.incluir(avaliacao(DATA, "90"));
        var corrigida = original.corrigir(personal, DATA, new BigDecimal("81"), new BigDecimal("180"), original.getMedidas(),
                DATA, AGORA);

        repositorio.alterar(original.substituir());
        var salva = repositorio.incluir(corrigida);
        limparContexto();

        var encontrada = repositorio.buscarPorIdentificador(salva.getIdentificador().orElseThrow());
        assertEquals(2, encontrada.getVersao());
        assertEquals(original.getIdentificador(), encontrada.getAvaliacaoAnterior());
        assertEquals(SituacaoAvaliacao.SUBSTITUIDA,
                repositorio.buscarPorIdentificador(original.getIdentificador().orElseThrow()).getSituacao());
    }

    @Test
    void bancoImpedeDuasAvaliacoesValidasNaMesmaData() {
        repositorio.incluir(avaliacao(DATA, "90"));
        limparContexto();
        assertThrows(DataIntegrityViolationException.class, () -> {
            repositorio.incluir(avaliacao(DATA, "88"));
            repositorioJpa.flush();
        });
    }

    @Test
    void bloqueiaAvaliacaoExistente() {
        var incluida = repositorio.incluir(avaliacao(DATA, "90"));
        limparContexto();
        assertEquals(2, repositorio.bloquear(incluida.getIdentificador().orElseThrow()).getMedidas().size());
    }
}
