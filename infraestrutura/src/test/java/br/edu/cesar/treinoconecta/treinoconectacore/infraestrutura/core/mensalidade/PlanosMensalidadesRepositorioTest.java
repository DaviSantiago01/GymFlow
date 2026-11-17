package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.mensalidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.mensalidade.MensalidadeOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.plano.PlanoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.SituacaoAdesao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.FormaPagamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.Mensalidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.SituacaoMensalidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.SituacaoPagamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.SituacaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano.AdesaoPlanoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.adesaoplano.IAdesaoPlanoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpaTeste;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano.IPlanoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano.PlanoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano.PlanoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano.PlanoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

@DataJpaTest
class PlanosMensalidadesRepositorioTest {
    private static final LocalDate HOJE = LocalDate.parse("2026-11-17");

    @Autowired
    private EntityManager entidades;
    @Autowired
    private IPlanoRepositorioJpa planosJpa;
    @Autowired
    private IAdesaoPlanoRepositorioJpa adesoesJpa;
    @Autowired
    private IMensalidadeRepositorioJpa mensalidadesJpa;
    @Autowired
    private IAlunoRepositorioJpa alunosJpa;
    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private PlanoRepositorio planos;
    private AdesaoPlanoRepositorio adesoes;
    private MensalidadeRepositorio mensalidades;
    private AlunoJpa mariana;
    private AlunoJpa pedro;
    private Plano mensal;

    @BeforeEach
    void configurar() {
        planos = new PlanoRepositorio(planosJpa);
        adesoes = new AdesaoPlanoRepositorio(adesoesJpa, alunosJpa, planosJpa);
        mensalidades = new MensalidadeRepositorio(mensalidadesJpa, adesoesJpa, alunosJpa);
        mariana = alunosJpa.save(AlunoJpaTeste.novo(entidades, "mariana-financeiro", "Mariana"));
        pedro = alunosJpa.save(AlunoJpaTeste.novo(entidades, "pedro-financeiro", "Pedro"));
        mensal = planos.incluir(Plano.cadastrar("Mensal", new BigDecimal("99.90"), 1));
    }

    private AlunoIdentificador aluno(AlunoJpa aluno) {
        return new AlunoIdentificador(aluno.getIdentificador().toString());
    }

    private void limparContexto() {
        entidades.flush();
        entidades.clear();
    }

    private Mensalidade primeiraMensalidade(AdesaoPlano adesao) {
        return mensalidades.incluir(Mensalidade.gerar(adesao, adesao.periodo(0), mensal.getValor()));
    }

    @Test
    void planoMantemValoresEConsultaDuplicidadeSemDiferenciarMaiusculas() {
        limparContexto();
        var salvo = planos.buscarPorIdentificador(mensal.getIdentificador().orElseThrow());
        assertEquals("Mensal", salvo.getNome());
        assertEquals(new BigDecimal("99.90"), salvo.getValor());
        assertTrue(planos.existeAtivoComNomeEDuracao("MENSAL", 1, Optional.empty()));
        assertFalse(planos.existeAtivoComNomeEDuracao("Mensal", 3, Optional.empty()));
        assertFalse(planos.existeAtivoComNomeEDuracao("Mensal", 1, mensal.getIdentificador()));
    }

    @Test
    void bancoImpedeDoisPlanosAtivosComMesmoNomeEDuracao() {
        assertThrows(DataIntegrityViolationException.class,
                () -> planosJpa.saveAndFlush(new PlanoJpa(null, "mensal", new BigDecimal("50"), 1, SituacaoPlano.ATIVO)));
    }

    @Test
    void planoInativoNaoBloqueiaNovoCadastroNoBanco() {
        planos.alterar(planos.bloquear(mensal.getIdentificador().orElseThrow()).inativar());
        planosJpa.saveAndFlush(new PlanoJpa(null, "Mensal", new BigDecimal("109.90"), 1, SituacaoPlano.ATIVO));
        assertEquals(2, planosJpa.count());
    }

    @Test
    void bancoImpedeDuasAdesoesAtivasDoMesmoAluno() {
        adesoes.incluir(AdesaoPlano.aderir(aluno(mariana), mensal, HOJE));
        limparContexto();
        assertThrows(DataIntegrityViolationException.class, () -> {
            adesoes.incluir(AdesaoPlano.aderir(aluno(mariana), mensal, HOJE));
            entidades.flush();
        });
    }

    @Test
    void trocaGravaOEncerramentoAntesDaNovaAdesao() {
        var atual = adesoes.incluir(AdesaoPlano.aderir(aluno(mariana), mensal, HOJE.minusMonths(1)));
        adesoes.alterar(atual.encerrarPorTroca(HOJE));
        var nova = adesoes.incluir(AdesaoPlano.aderir(aluno(mariana), mensal, HOJE));
        limparContexto();
        assertEquals(nova.getIdentificador(), adesoes.buscarAtivaDoAluno(aluno(mariana)).orElseThrow().getIdentificador());
        var encerrada = adesoes.buscarPorIdentificador(atual.getIdentificador().orElseThrow());
        assertEquals(SituacaoAdesao.ENCERRADA, encerrada.getSituacao());
        assertEquals(Optional.of(HOJE), encerrada.getEncerramento());
        assertEquals(1, adesoes.buscarAtivas().size());
    }

    @Test
    void mensalidadeDuplicadaNoMesmoPeriodoViolaORestricaoDoBanco() {
        var adesao = adesoes.incluir(AdesaoPlano.aderir(aluno(mariana), mensal, HOJE));
        primeiraMensalidade(adesao);
        assertTrue(mensalidades.existePorAdesaoEPeriodo(adesao.getIdentificador().orElseThrow(), HOJE));
        assertFalse(mensalidades.existePorAdesaoEPeriodo(adesao.getIdentificador().orElseThrow(), HOJE.plusMonths(1)));
        assertThrows(DataIntegrityViolationException.class, () -> {
            primeiraMensalidade(adesao);
            entidades.flush();
        });
    }

    @Test
    void pagamentoEEstornoSaoPreservadosNoHistorico() {
        var adesao = adesoes.incluir(AdesaoPlano.aderir(aluno(mariana), mensal, HOJE));
        var mensalidade = primeiraMensalidade(adesao);
        var identificador = mensalidade.getIdentificador().orElseThrow();
        mensalidades.alterar(mensalidades.bloquear(identificador).registrarPagamento(FormaPagamento.PIX,
                Instant.parse("2026-11-17T15:00:00Z"), HOJE));
        limparContexto();
        var paga = mensalidades.buscarPorIdentificador(identificador);
        assertEquals(SituacaoMensalidade.PAGA, paga.getSituacao());
        var pagamento = paga.getPagamentos().get(0).getIdentificador().orElseThrow();

        mensalidades.alterar(paga.estornarPagamento(pagamento, "Lançado em duplicidade", Instant.parse("2026-11-17T16:00:00Z")));
        mensalidades.alterar(mensalidades.bloquear(identificador).registrarPagamento(FormaPagamento.DINHEIRO,
                Instant.parse("2026-11-17T17:00:00Z"), HOJE));
        limparContexto();
        var historico = mensalidades.buscarPorIdentificador(identificador);
        assertEquals(SituacaoMensalidade.PAGA, historico.getSituacao());
        assertEquals(List.of(SituacaoPagamento.ESTORNADO, SituacaoPagamento.CONFIRMADO),
                historico.getPagamentos().stream().map(item -> item.getSituacao()).toList());
        assertEquals(Optional.of("Lançado em duplicidade"), historico.getPagamentos().get(0).getMotivoEstorno());
    }

    @Test
    void pesquisaSeparaMensalidadesAbertasAtrasadasEPagasERestringeOAluno() {
        var adesaoMariana = adesoes.incluir(AdesaoPlano.aderir(aluno(mariana), mensal, HOJE.minusMonths(1)));
        var vencida = mensalidades.incluir(Mensalidade.gerar(adesaoMariana, adesaoMariana.periodo(0), mensal.getValor()));
        mensalidades.incluir(Mensalidade.gerar(adesaoMariana, adesaoMariana.periodo(1), mensal.getValor()));
        var adesaoPedro = adesoes.incluir(AdesaoPlano.aderir(aluno(pedro), mensal, HOJE));
        var paga = primeiraMensalidade(adesaoPedro);
        mensalidades.alterar(paga.registrarPagamento(FormaPagamento.PIX, Instant.parse("2026-11-17T15:00:00Z"), HOJE));
        limparContexto();

        var relogio = Clock.fixed(HOJE.atTime(12, 0).toInstant(ZoneOffset.ofHours(-3)), ZoneOffset.UTC);
        var consultas = new MensalidadeAplicacaoRepositorio(mensalidadesJpa, seguranca, relogio);
        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(false);
        var atrasadas = consultas.pesquisar(consulta(Optional.of(SituacaoMensalidade.ATRASADA))).getProjecoes();
        assertEquals(List.of(vencida.getIdentificador().orElseThrow()), atrasadas.stream().map(item -> item.identificador()).toList());
        assertEquals(1, consultas.pesquisar(consulta(Optional.of(SituacaoMensalidade.ABERTA))).getTotal());
        assertEquals(1, consultas.pesquisar(consulta(Optional.of(SituacaoMensalidade.PAGA))).getTotal());
        assertEquals(3, consultas.pesquisar(consulta(Optional.empty())).getTotal());

        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(true);
        when(seguranca.getUsuario()).thenReturn("pedro-financeiro");
        var doPedro = consultas.pesquisar(consulta(Optional.empty())).getProjecoes();
        assertEquals(1, doPedro.size());
        assertEquals("Pedro", doPedro.get(0).alunoNome());
        assertEquals("Mensal", doPedro.get(0).planoNome());
    }

    @Test
    void pesquisaDePlanosContaAdesoesAtivas() {
        adesoes.incluir(AdesaoPlano.aderir(aluno(mariana), mensal, HOJE));
        adesoes.incluir(AdesaoPlano.aderir(aluno(pedro), mensal, HOJE));
        limparContexto();
        var consultas = new PlanoAplicacaoRepositorio(planosJpa);
        var resultado = consultas.pesquisar(new PlanoConsulta(Optional.of(PlanoOrdenacao.NOME), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of("mens"), Optional.of(SituacaoPlano.ATIVO)));
        assertEquals(1, resultado.getTotal());
        assertEquals(2, resultado.getProjecoes().get(0).adesoesAtivas());
    }

    private static MensalidadeConsulta consulta(Optional<SituacaoMensalidade> situacao) {
        return new MensalidadeConsulta(Optional.of(MensalidadeOrdenacao.VENCIMENTO), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), situacao);
    }
}
