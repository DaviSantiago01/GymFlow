package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.spi.IAvaliacaoFisicaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.spi.IMetaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.Personal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi.IPersonalRepositorio;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public final class AvaliacaoFisicaStepDefinitions {
    private static final String PESO_PADRAO = "80";
    private static final String ALTURA_PADRAO = "180";

    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private final IAvaliacaoFisicaRepositorio repositorio = mock(IAvaliacaoFisicaRepositorio.class);
    private final IAlunoRepositorio alunos = mock(IAlunoRepositorio.class);
    private final IPersonalRepositorio personais = mock(IPersonalRepositorio.class);
    private final IMetaRepositorio metas = mock(IMetaRepositorio.class);
    private final Map<String, AvaliacaoFisica> avaliacoes = new LinkedHashMap<>();
    private final Set<String> alunosCadastrados = new HashSet<>();
    private final Set<String> referenciasDeMeta = new HashSet<>();
    private AvaliacaoFisicaServico servico;
    private int proximoIdentificador = 1;
    private AvaliacaoFisica registrada;
    private AvaliacaoFisica original;
    private List<ComparacaoIndicador> comparacoes = List.of();
    private Optional<AvaliacaoFisica> referencia = Optional.empty();
    private Optional<AvaliacaoFisica> situacaoAtual = Optional.empty();
    private Optional<TendenciaIndicador> tendencia = Optional.empty();
    private List<PontoIndicador> pontos = List.of();
    private RuntimeException falha;

    public AvaliacaoFisicaStepDefinitions() {
        when(seguranca.getUsuario()).thenReturn("usuario");
        when(seguranca.possuiAutorizacao(anyString())).thenReturn(false);
        when(alunos.existePorIdentificador(any())).thenAnswer(
                chamada -> alunosCadastrados.contains(chamada.getArgument(0).toString()));
        when(alunos.buscarPorIdentificador(any())).thenAnswer(chamada -> {
            AlunoIdentificador aluno = chamada.getArgument(0);
            return new Aluno(Optional.of(aluno), "aluno" + aluno, "Aluno");
        });
        when(repositorio.existePorIdentificador(any())).thenAnswer(
                chamada -> avaliacoes.containsKey(chamada.getArgument(0).toString()));
        when(repositorio.buscarPorIdentificador(any())).thenAnswer(chamada -> avaliacoes.get(chamada.getArgument(0).toString()));
        when(repositorio.bloquear(any())).thenAnswer(chamada -> avaliacoes.get(chamada.getArgument(0).toString()));
        when(repositorio.buscarValidasPorAluno(any())).thenAnswer(chamada -> buscarValidas(chamada.getArgument(0)));
        when(repositorio.incluir(any())).thenAnswer(chamada -> guardar(chamada.getArgument(0)));
        doAnswer(chamada -> {
            AvaliacaoFisica alterada = chamada.getArgument(0);
            avaliacoes.put(alterada.getIdentificador().orElseThrow().toString(), alterada);
            return null;
        }).when(repositorio).alterar(any());
        when(metas.buscarAtivasPorAluno(any())).thenReturn(List.of());
        when(metas.existePorAvaliacaoReferencia(any())).thenAnswer(
                chamada -> referenciasDeMeta.contains(chamada.getArgument(0).toString()));
    }

    private List<AvaliacaoFisica> buscarValidas(AlunoIdentificador aluno) {
        var validas = new ArrayList<AvaliacaoFisica>();
        for (var avaliacao : avaliacoes.values()) {
            if (avaliacao.getAluno().equals(aluno) && avaliacao.isValida()) {
                validas.add(avaliacao);
            }
        }
        return validas;
    }

    private AvaliacaoFisica guardar(AvaliacaoFisica nova) {
        var identificador = new AvaliacaoFisicaIdentificador(String.valueOf(proximoIdentificador++));
        var salva = new AvaliacaoFisica(Optional.of(identificador), nova.getAluno(), nova.getPersonal(), nova.getData(),
                nova.getPeso(), nova.getAltura(), nova.getMedidas(), nova.getSituacao(), nova.getVersao(),
                nova.getAvaliacaoAnterior(), nova.getRegistro());
        avaliacoes.put(identificador.toString(), salva);
        return salva;
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException excecao) {
            falha = excecao;
        }
    }

    private AvaliacaoFisica avaliacaoDe(String data) {
        for (var avaliacao : avaliacoes.values()) {
            if (avaliacao.getData().equals(LocalDate.parse(data))) {
                return avaliacao;
            }
        }
        throw new IllegalArgumentException("Avaliação não cadastrada: " + data);
    }

    private AvaliacaoFisicaIdentificador identificadorDe(AvaliacaoFisica avaliacao) {
        return avaliacao.getIdentificador().orElseThrow();
    }

    private AvaliacaoFisica nova(String aluno, String data, String peso, List<MedidaCorporal> medidas) {
        return new AvaliacaoFisica(Optional.empty(), new AlunoIdentificador(aluno), new PersonalIdentificador("1"),
                LocalDate.parse(data), new BigDecimal(peso), new BigDecimal(ALTURA_PADRAO), medidas,
                SituacaoAvaliacao.VALIDA, 1, Optional.empty(), Instant.parse("2026-11-09T12:00:00Z"));
    }

    private ComparacaoIndicador comparacaoDe(String indicador) {
        for (var comparacao : comparacoes) {
            if (comparacao.indicador() == IndicadorFisico.valueOf(indicador)) {
                return comparacao;
            }
        }
        throw new AssertionError("Indicador sem comparação: " + indicador);
    }

    private static void conferirValor(String esperado, BigDecimal valor) {
        assertEquals(0, new BigDecimal(esperado).compareTo(valor), "valor obtido: " + valor);
    }

    @Dado("que estou autenticado como o personal {string} nas avaliações")
    public void autenticarPersonal(String personal) {
        when(seguranca.possuiAutorizacao(Autorizacao.PERSONAL.name())).thenReturn(true);
        when(personais.buscarPorUsuario("usuario")).thenReturn(
                new Personal(Optional.of(new PersonalIdentificador(personal)), "usuario", "Personal"));
    }

    @Dado("que existe o aluno {string} nas avaliações")
    public void cadastrarAluno(String aluno) {
        alunosCadastrados.add(aluno);
    }

    @Dado("que hoje é {string}")
    public void definirHoje(String data) {
        var relogio = Clock.fixed(Instant.parse(data + "T15:00:00Z"), ZoneOffset.UTC);
        servico = new AvaliacaoFisicaServico(seguranca, repositorio, alunos, personais, metas, relogio);
    }

    @Dado("uma avaliação do aluno {string} em {string} com {string} de {string}")
    public void cadastrarAvaliacao(String aluno, String data, String indicador, String valor) {
        guardar(nova(aluno, data, PESO_PADRAO,
                List.of(new MedidaCorporal(IndicadorFisico.valueOf(indicador), new BigDecimal(valor)))));
    }

    @Dado("uma avaliação do aluno {string} em {string} com peso {string}")
    public void cadastrarAvaliacaoComPeso(String aluno, String data, String peso) {
        guardar(nova(aluno, data, peso, List.of()));
    }

    @Dado("que a avaliação de {string} é referência de uma meta")
    public void marcarReferenciaDeMeta(String data) {
        referenciasDeMeta.add(identificadorDe(avaliacaoDe(data)).toString());
    }

    private void registrar(String aluno, LocalDate data, String peso, String altura, List<MedidaCorporal> medidas) {
        executar(() -> registrada = servico.registrar(new AlunoIdentificador(aluno), data, new BigDecimal(peso),
                new BigDecimal(altura), medidas));
    }

    @Quando("eu registrar para o aluno {string} em {string} peso {string} e altura {string}")
    public void registrarAvaliacao(String aluno, String data, String peso, String altura) {
        registrar(aluno, LocalDate.parse(data), peso, altura, List.of());
    }

    @Quando("eu registrar para o aluno {string} sem data peso {string} e altura {string}")
    public void registrarSemData(String aluno, String peso, String altura) {
        registrar(aluno, null, peso, altura, List.of());
    }

    @Quando("eu registrar para o aluno {string} em {string} com a medida {string} de {string}")
    public void registrarComMedida(String aluno, String data, String indicador, String valor) {
        executar(() -> registrada = servico.registrar(new AlunoIdentificador(aluno), LocalDate.parse(data),
                new BigDecimal(PESO_PADRAO), new BigDecimal(ALTURA_PADRAO),
                List.of(new MedidaCorporal(IndicadorFisico.valueOf(indicador), new BigDecimal(valor)))));
    }

    @Quando("eu comparar a avaliação de {string} com a de {string}")
    public void comparar(String atual, String anterior) {
        comparacoes = avaliacaoDe(atual).compararCom(avaliacaoDe(anterior));
    }

    @Quando("eu consultar a referência da avaliação de {string}")
    public void consultarReferencia(String data) {
        referencia = servico.buscarReferencia(avaliacaoDe(data), Optional.empty());
    }

    @Quando("eu consultar a tendência de {string} do aluno {string}")
    public void consultarTendencia(String indicador, String aluno) {
        tendencia = servico.consultarHistorico(new AlunoIdentificador(aluno)).tendencia(IndicadorFisico.valueOf(indicador));
    }

    @Quando("eu consultar o histórico de {string} do aluno {string}")
    public void consultarHistorico(String indicador, String aluno) {
        pontos = servico.consultarHistorico(new AlunoIdentificador(aluno)).pontos(IndicadorFisico.valueOf(indicador));
    }

    @Quando("eu consultar a situação atual do aluno {string}")
    public void consultarSituacao(String aluno) {
        situacaoAtual = servico.consultarHistorico(new AlunoIdentificador(aluno)).maisRecente();
    }

    private void corrigir(AvaliacaoFisica avaliacao, LocalDate data, String peso) {
        executar(() -> registrada = servico.corrigir(identificadorDe(avaliacao), data, new BigDecimal(peso),
                avaliacao.getAltura(), avaliacao.getMedidas()));
    }

    @Quando("eu corrigir a avaliação de {string} para o peso {string}")
    public void corrigirPeso(String data, String peso) {
        original = avaliacaoDe(data);
        corrigir(original, original.getData(), peso);
    }

    @Quando("eu corrigir a data da avaliação de {string} para {string}")
    public void corrigirData(String data, String novaData) {
        original = avaliacaoDe(data);
        corrigir(original, LocalDate.parse(novaData), PESO_PADRAO);
    }

    @Quando("eu corrigir a avaliação original para o peso {string}")
    public void corrigirOriginalDeNovo(String peso) {
        corrigir(original, original.getData(), peso);
    }

    @Quando("eu excluir a avaliação de {string}")
    public void excluir(String data) {
        executar(() -> servico.remover(identificadorDe(avaliacaoDe(data))));
    }

    @Então("a avaliação é registrada para o aluno {string}")
    public void conferirAluno(String aluno) {
        assertNull(falha);
        assertEquals(new AlunoIdentificador(aluno), registrada.getAluno());
    }

    @Então("a avaliação é rejeitada por aluno inexistente")
    public void conferirAlunoInexistente() {
        assertInstanceOf(AgregadoInexistenteExcecao.class, falha);
        verify(repositorio, never()).incluir(any());
    }

    @Então("a avaliação é rejeitada com a mensagem {string}")
    public void conferirMensagem(String mensagem) {
        assertNotNull(falha);
        assertEquals(mensagem, falha.getMessage());
    }

    @Então("a data da avaliação é {string}")
    public void conferirData(String data) {
        assertEquals(LocalDate.parse(data), registrada.getData());
    }

    @Então("o IMC da avaliação é {string}")
    public void conferirImc(String imc) {
        conferirValor(imc, registrada.getImc());
    }

    @Então("a classificação do IMC é {string}")
    public void conferirClassificacao(String classificacao) {
        assertEquals(ClassificacaoImc.valueOf(classificacao), registrada.getClassificacaoImc());
    }

    @Então("a comparação de {string} tem diferença absoluta {string} e percentual {string}")
    public void conferirDiferenca(String indicador, String absoluta, String percentual) {
        var comparacao = comparacaoDe(indicador);
        conferirValor(absoluta, comparacao.diferencaAbsoluta());
        conferirValor(percentual, comparacao.diferencaPercentual());
    }

    @Então("a evolução percentual de {string} é {string}")
    public void conferirEvolucao(String indicador, String evolucao) {
        conferirValor(evolucao, comparacaoDe(indicador).evolucaoPercentual());
    }

    @Então("a interpretação de {string} é {string}")
    public void conferirInterpretacao(String indicador, String interpretacao) {
        assertEquals(InterpretacaoEvolucao.valueOf(interpretacao), comparacaoDe(indicador).interpretacao());
    }

    @Então("a referência é a avaliação de {string}")
    public void conferirReferencia(String data) {
        assertEquals(LocalDate.parse(data), referencia.orElseThrow().getData());
    }

    @Então("não há avaliação de referência")
    public void conferirSemReferencia() {
        assertTrue(referencia.isEmpty());
    }

    @Então("o aluno {string} possui {int} avaliações válidas")
    public void conferirQuantidade(String aluno, int quantidade) {
        assertEquals(quantidade, buscarValidas(new AlunoIdentificador(aluno)).size());
    }

    @Então("nenhuma avaliação foi alterada")
    public void conferirSemAlteracao() {
        verify(repositorio, never()).alterar(any());
    }

    @Então("a tendência é {string}")
    public void conferirTendencia(String esperada) {
        assertEquals(TendenciaIndicador.valueOf(esperada), tendencia.orElseThrow());
    }

    @Então("não há tendência calculada")
    public void conferirSemTendencia() {
        assertTrue(tendencia.isEmpty());
    }

    @Então("os valores do histórico são {string}")
    public void conferirPontos(String valores) {
        var esperados = valores.split(",");
        assertEquals(esperados.length, pontos.size());
        for (int indice = 0; indice < esperados.length; indice++) {
            conferirValor(esperados[indice], pontos.get(indice).valor());
        }
    }

    @Então("a avaliação considerada é a de {string}")
    public void conferirSituacao(String data) {
        assertEquals(LocalDate.parse(data), situacaoAtual.orElseThrow().getData());
    }

    @Então("a correção está na versão {int} e referencia a avaliação original")
    public void conferirCorrecao(int versao) {
        assertEquals(versao, registrada.getVersao());
        assertEquals(original.getIdentificador(), registrada.getAvaliacaoAnterior());
    }

    @Então("a avaliação original está na situação {string}")
    public void conferirOriginal(String situacao) {
        assertEquals(SituacaoAvaliacao.valueOf(situacao),
                avaliacoes.get(identificadorDe(original).toString()).getSituacao());
    }

    @Então("a avaliação de {string} está na situação {string}")
    public void conferirSituacaoDaData(String data, String situacao) {
        assertEquals(SituacaoAvaliacao.valueOf(situacao), avaliacaoDe(data).getSituacao());
    }

    @Então("nenhum registro de avaliação foi apagado")
    public void conferirSemExclusaoFisica() {
        verify(repositorio, never()).excluir(any());
    }
}
