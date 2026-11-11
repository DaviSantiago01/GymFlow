package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.MedidaCorporal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.SituacaoAvaliacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.spi.IAvaliacaoFisicaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.spi.IMetaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.Personal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi.IPersonalRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public final class MetaStepDefinitions {
    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private final IMetaRepositorio repositorio = mock(IMetaRepositorio.class);
    private final IAvaliacaoFisicaRepositorio avaliacoes = mock(IAvaliacaoFisicaRepositorio.class);
    private final IAlunoRepositorio alunos = mock(IAlunoRepositorio.class);
    private final IPersonalRepositorio personais = mock(IPersonalRepositorio.class);
    private final Map<String, Meta> metas = new LinkedHashMap<>();
    private final List<AvaliacaoFisica> avaliacoesCadastradas = new ArrayList<>();
    private final Set<String> alunosCadastrados = new HashSet<>();
    private final AlunoIdentificador aluno = new AlunoIdentificador("1");
    private final PersonalIdentificador personal = new PersonalIdentificador("1");
    private MetaServico servico;
    private AvaliacaoFisicaServico servicoAvaliacao;
    private int proximaMeta = 1;
    private int proximaAvaliacao = 1;
    private Meta meta;
    private Meta metaAnterior;
    private BigDecimal percentual;
    private StatusCumprimento status;
    private RuntimeException falha;

    public MetaStepDefinitions() {
        when(seguranca.getUsuario()).thenReturn("usuario");
        when(seguranca.possuiAutorizacao(anyString())).thenReturn(false);
        when(alunos.existePorIdentificador(any())).thenAnswer(
                chamada -> alunosCadastrados.contains(chamada.getArgument(0).toString()));
        when(alunos.buscarPorIdentificador(any())).thenAnswer(chamada -> {
            AlunoIdentificador identificador = chamada.getArgument(0);
            return new Aluno(Optional.of(identificador), "aluno" + identificador, "Aluno");
        });
        when(avaliacoes.buscarValidasPorAluno(any())).thenAnswer(chamada -> new ArrayList<>(avaliacoesCadastradas));
        when(avaliacoes.incluir(any())).thenAnswer(chamada -> guardarAvaliacao(chamada.getArgument(0)));
        when(repositorio.existePorIdentificador(any())).thenAnswer(
                chamada -> metas.containsKey(chamada.getArgument(0).toString()));
        when(repositorio.buscarPorIdentificador(any())).thenAnswer(chamada -> metas.get(chamada.getArgument(0).toString()));
        when(repositorio.bloquear(any())).thenAnswer(chamada -> metas.get(chamada.getArgument(0).toString()));
        when(repositorio.incluir(any())).thenAnswer(chamada -> guardarMeta(chamada.getArgument(0)));
        doAnswer(chamada -> {
            Meta alterada = chamada.getArgument(0);
            metas.put(alterada.getIdentificador().orElseThrow().toString(), alterada);
            return null;
        }).when(repositorio).alterar(any());
        when(repositorio.existeAtivaNoPeriodo(any(), any(), any(), any(), any())).thenAnswer(chamada -> {
            IndicadorFisico indicador = chamada.getArgument(1);
            LocalDate inicio = chamada.getArgument(2);
            LocalDate fim = chamada.getArgument(3);
            Optional<MetaIdentificador> ignorada = chamada.getArgument(4);
            return existeSobreposicao(indicador, inicio, fim, ignorada);
        });
        when(repositorio.buscarAtivasPorAluno(any())).thenAnswer(chamada -> {
            var ativas = new ArrayList<Meta>();
            for (var candidata : metas.values()) {
                if (candidata.getSituacao() == SituacaoMeta.ATIVA) {
                    ativas.add(candidata);
                }
            }
            return ativas;
        });
    }

    private boolean existeSobreposicao(IndicadorFisico indicador, LocalDate inicio, LocalDate fim,
            Optional<MetaIdentificador> ignorada) {
        for (var candidata : metas.values()) {
            boolean mesmaMeta = candidata.getIdentificador().equals(ignorada);
            boolean sobrepoe = !candidata.getInicio().isAfter(fim) && !candidata.getFim().isBefore(inicio);
            if (!mesmaMeta && candidata.getSituacao() == SituacaoMeta.ATIVA && candidata.getIndicador() == indicador && sobrepoe) {
                return true;
            }
        }
        return false;
    }

    private Meta guardarMeta(Meta nova) {
        var identificador = new MetaIdentificador(String.valueOf(proximaMeta++));
        var salva = new Meta(Optional.of(identificador), nova.getAluno(), nova.getPersonal(), nova.getIndicador(),
                nova.getValorInicial(), nova.getValorDesejado(), nova.getInicio(), nova.getFim(),
                nova.getAvaliacaoReferencia(), nova.getSituacao(), nova.getDataAtingida());
        metas.put(identificador.toString(), salva);
        return salva;
    }

    private AvaliacaoFisica guardarAvaliacao(AvaliacaoFisica nova) {
        var identificador = new AvaliacaoFisicaIdentificador(String.valueOf(proximaAvaliacao++));
        var salva = new AvaliacaoFisica(Optional.of(identificador), nova.getAluno(), nova.getPersonal(), nova.getData(),
                nova.getPeso(), nova.getAltura(), nova.getMedidas(), nova.getSituacao(), nova.getVersao(),
                nova.getAvaliacaoAnterior(), nova.getRegistro());
        avaliacoesCadastradas.add(salva);
        return salva;
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException excecao) {
            falha = excecao;
        }
    }

    private Meta metaAtualizada() {
        return metas.get(meta.getIdentificador().orElseThrow().toString());
    }

    private static List<MedidaCorporal> medida(String indicador, String valor) {
        return List.of(new MedidaCorporal(IndicadorFisico.valueOf(indicador), new BigDecimal(valor)));
    }

    private static void conferirValor(String esperado, BigDecimal valor) {
        assertEquals(0, new BigDecimal(esperado).compareTo(valor), "valor obtido: " + valor);
    }

    @Dado("que estou autenticado como o personal {string} nas metas")
    public void autenticarPersonal(String identificador) {
        when(seguranca.possuiAutorizacao(Autorizacao.PERSONAL.name())).thenReturn(true);
        when(personais.buscarPorUsuario("usuario")).thenReturn(
                new Personal(Optional.of(new PersonalIdentificador(identificador)), "usuario", "Personal"));
    }

    @Dado("que existe o aluno {string} nas metas")
    public void cadastrarAluno(String identificador) {
        alunosCadastrados.add(identificador);
    }

    @Dado("que a data atual nas metas é {string}")
    public void definirHoje(String data) {
        var relogio = Clock.fixed(Instant.parse(data + "T15:00:00Z"), ZoneOffset.UTC);
        servico = new MetaServico(seguranca, repositorio, avaliacoes, alunos, personais, relogio);
        servicoAvaliacao = new AvaliacaoFisicaServico(seguranca, avaliacoes, alunos, personais, repositorio, relogio);
    }

    @Dado("que o aluno {string} tem uma avaliação em {string} com {string} de {string}")
    public void cadastrarAvaliacao(String identificador, String data, String indicador, String valor) {
        guardarAvaliacao(new AvaliacaoFisica(Optional.empty(), new AlunoIdentificador(identificador), personal,
                LocalDate.parse(data), new BigDecimal("80"), new BigDecimal("180"), medida(indicador, valor),
                SituacaoAvaliacao.VALIDA, 1, Optional.empty(), Instant.parse("2026-11-01T12:00:00Z")));
    }

    @Dado("uma meta ativa de {string} com valor {string} de {string} a {string}")
    public void cadastrarMetaAtiva(String indicador, String valor, String inicio, String fim) {
        meta = servico.criar(aluno, IndicadorFisico.valueOf(indicador), new BigDecimal(valor), LocalDate.parse(inicio),
                LocalDate.parse(fim));
    }

    @Dado("uma meta de {string} partindo de {string} com valor desejado {string}")
    public void montarMeta(String indicador, String inicial, String desejado) {
        meta = new Meta(Optional.empty(), aluno, personal, IndicadorFisico.valueOf(indicador), new BigDecimal(inicial),
                new BigDecimal(desejado), LocalDate.parse("2026-11-01"), LocalDate.parse("2026-12-31"),
                new AvaliacaoFisicaIdentificador("1"), SituacaoMeta.ATIVA, Optional.empty());
    }

    @Quando("eu criar uma meta de {string} com valor {string} de {string} a {string}")
    public void criarMeta(String indicador, String valor, String inicio, String fim) {
        executar(() -> meta = servico.criar(aluno, IndicadorFisico.valueOf(indicador), new BigDecimal(valor),
                LocalDate.parse(inicio), LocalDate.parse(fim)));
    }

    @Quando("eu encerrar a meta")
    public void encerrarMeta() {
        executar(() -> servico.encerrar(meta.getIdentificador().orElseThrow()));
    }

    @Quando("eu substituir a meta pelo valor {string} de {string} a {string}")
    public void substituirMeta(String valor, String inicio, String fim) {
        executar(() -> {
            metaAnterior = meta;
            meta = servico.substituir(meta.getIdentificador().orElseThrow(), new BigDecimal(valor), LocalDate.parse(inicio),
                    LocalDate.parse(fim));
        });
    }

    @Quando("eu ajustar a meta para o valor {string} de {string} a {string}")
    public void ajustarMeta(String valor, String inicio, String fim) {
        executar(() -> meta = servico.ajustar(meta.getIdentificador().orElseThrow(), new BigDecimal(valor),
                LocalDate.parse(inicio), LocalDate.parse(fim)));
    }

    @Quando("eu calcular o cumprimento com o valor atual {string}")
    public void calcularCumprimento(String valor) {
        percentual = meta.percentualCumprimento(new BigDecimal(valor));
        status = meta.statusCumprimento(new BigDecimal(valor));
    }

    @Quando("uma nova avaliação em {string} registrar {string} de {string}")
    public void registrarAvaliacao(String data, String indicador, String valor) {
        alunosCadastrados.add(aluno.toString());
        executar(() -> servicoAvaliacao.registrar(aluno, LocalDate.parse(data), new BigDecimal("80"), new BigDecimal("180"),
                medida(indicador, valor)));
    }

    @Então("a meta fica ativa partindo do valor {string}")
    public void conferirMetaAtiva(String valor) {
        assertNull(falha);
        assertEquals(SituacaoMeta.ATIVA, metaAtualizada().getSituacao());
        conferirValor(valor, metaAtualizada().getValorInicial());
    }

    @Então("a meta é rejeitada com a mensagem {string}")
    public void conferirMensagem(String mensagem) {
        assertNotNull(falha);
        assertEquals(mensagem, falha.getMessage());
    }

    @Então("a meta anterior está na situação {string}")
    public void conferirMetaAnterior(String situacao) {
        assertEquals(SituacaoMeta.valueOf(situacao),
                metas.get(metaAnterior.getIdentificador().orElseThrow().toString()).getSituacao());
    }

    @Então("o valor desejado da meta é {string}")
    public void conferirValorDesejado(String valor) {
        conferirValor(valor, metaAtualizada().getValorDesejado());
    }

    @Então("o percentual de cumprimento é {string}")
    public void conferirPercentual(String esperado) {
        conferirValor(esperado, percentual);
    }

    @Então("o status da meta é {string}")
    public void conferirStatus(String esperado) {
        assertEquals(StatusCumprimento.valueOf(esperado), status);
    }

    @Então("a meta está atingida em {string}")
    public void conferirMetaAtingida(String data) {
        assertNull(falha);
        assertEquals(SituacaoMeta.ATINGIDA, metaAtualizada().getSituacao());
        assertEquals(LocalDate.parse(data), metaAtualizada().getDataAtingida().orElseThrow());
    }

    @Então("a meta continua ativa")
    public void conferirMetaContinuaAtiva() {
        assertNull(falha);
        assertEquals(SituacaoMeta.ATIVA, metaAtualizada().getSituacao());
    }
}
