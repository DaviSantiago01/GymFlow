package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.SituacaoAdesao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.spi.IAdesaoPlanoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.spi.IMensalidadeRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.SituacaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.spi.IPlanoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

/** Repositórios em memória simulam a persistência; os serviços de domínio são os reais. */
public final class PlanosMensalidadesStepDefinitions {
    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private final IPlanoRepositorio planos = mock(IPlanoRepositorio.class);
    private final IAdesaoPlanoRepositorio adesoes = mock(IAdesaoPlanoRepositorio.class);
    private final IMensalidadeRepositorio mensalidades = mock(IMensalidadeRepositorio.class);
    private final IAlunoRepositorio alunos = mock(IAlunoRepositorio.class);
    private final Clock relogio = mock(Clock.class);
    private final Map<String, Plano> planosCadastrados = new LinkedHashMap<>();
    private final Map<String, AdesaoPlano> adesoesCadastradas = new LinkedHashMap<>();
    private final Map<String, Mensalidade> mensalidadesCadastradas = new LinkedHashMap<>();
    private final Set<String> alunosCadastrados = new HashSet<>();
    private final PlanoServico servicoPlano = new PlanoServico(seguranca, planos);
    private final AdesaoPlanoServico servicoAdesao = new AdesaoPlanoServico(seguranca, adesoes, planos, mensalidades, alunos,
            relogio);
    private final MensalidadeServico servicoMensalidade = new MensalidadeServico(seguranca, mensalidades, alunos, relogio);
    private String papel = Autorizacao.ADMINISTRADOR.name();
    private Instant agora = Instant.parse("2026-11-17T15:00:00Z");
    private int proximoIdentificador = 1;
    private long proximoPagamento = 1;
    private AdesaoPlano ultimaAdesao;
    private int geradas;
    private int concluidas;
    private RuntimeException falha;

    public PlanosMensalidadesStepDefinitions() {
        when(relogio.instant()).thenAnswer(chamada -> agora);
        when(seguranca.getUsuario()).thenReturn("usuario");
        when(seguranca.possuiAutorizacao(anyString())).thenAnswer(chamada -> papel.equals(chamada.getArgument(0)));
        when(alunos.existePorIdentificador(any())).thenAnswer(
                chamada -> alunosCadastrados.contains(chamada.getArgument(0).toString()));
        when(alunos.buscarPorIdentificador(any())).thenAnswer(chamada -> aluno(chamada.getArgument(0)));
        when(alunos.bloquearPorUsuario(anyString())).thenAnswer(chamada -> aluno(new AlunoIdentificador("1")));
        when(alunos.buscarPorUsuario(anyString())).thenAnswer(chamada -> aluno(new AlunoIdentificador("1")));
        configurarPlanos();
        configurarAdesoes();
        configurarMensalidades();
    }

    private static Aluno aluno(AlunoIdentificador identificador) {
        return new Aluno(Optional.of(identificador), "aluno" + identificador, "Aluno " + identificador);
    }

    private void configurarPlanos() {
        when(planos.existePorIdentificador(any())).thenAnswer(
                chamada -> planosCadastrados.containsKey(chamada.getArgument(0).toString()));
        when(planos.buscarPorIdentificador(any())).thenAnswer(chamada -> planosCadastrados.get(chamada.getArgument(0).toString()));
        when(planos.bloquear(any())).thenAnswer(chamada -> planosCadastrados.get(chamada.getArgument(0).toString()));
        when(planos.incluir(any())).thenAnswer(chamada -> {
            Plano novo = chamada.getArgument(0);
            var salvo = new Plano(Optional.of(new PlanoIdentificador(String.valueOf(proximoIdentificador++))), novo.getNome(),
                    novo.getValor(), novo.getDuracaoMeses(), novo.getSituacao());
            planosCadastrados.put(salvo.getIdentificador().orElseThrow().toString(), salvo);
            return salvo;
        });
        doAnswer(chamada -> {
            Plano alterado = chamada.getArgument(0);
            planosCadastrados.put(alterado.getIdentificador().orElseThrow().toString(), alterado);
            return null;
        }).when(planos).alterar(any());
        when(planos.existeAtivoComNomeEDuracao(anyString(), anyInt(), any())).thenAnswer(chamada -> {
            String nome = chamada.getArgument(0);
            int duracao = chamada.getArgument(1);
            Optional<PlanoIdentificador> ignorado = chamada.getArgument(2);
            return planosCadastrados.values().stream().anyMatch(plano -> plano.ativo()
                    && plano.getNome().equalsIgnoreCase(nome) && plano.getDuracaoMeses() == duracao
                    && !plano.getIdentificador().equals(ignorado));
        });
    }

    private void configurarAdesoes() {
        when(adesoes.existePorIdentificador(any())).thenAnswer(
                chamada -> adesoesCadastradas.containsKey(chamada.getArgument(0).toString()));
        when(adesoes.buscarPorIdentificador(any())).thenAnswer(chamada -> adesoesCadastradas.get(chamada.getArgument(0).toString()));
        when(adesoes.bloquear(any())).thenAnswer(chamada -> adesoesCadastradas.get(chamada.getArgument(0).toString()));
        when(adesoes.incluir(any())).thenAnswer(chamada -> {
            AdesaoPlano nova = chamada.getArgument(0);
            var salva = new AdesaoPlano(Optional.of(new AdesaoPlanoIdentificador(String.valueOf(proximoIdentificador++))),
                    nova.getAluno(), nova.getPlano(), nova.getPeriodicidadeMeses(), nova.getInicio(), nova.getEncerramento(),
                    nova.getSituacao(), nova.getQuantidadePeriodos());
            adesoesCadastradas.put(salva.getIdentificador().orElseThrow().toString(), salva);
            return salva;
        });
        doAnswer(chamada -> {
            AdesaoPlano alterada = chamada.getArgument(0);
            adesoesCadastradas.put(alterada.getIdentificador().orElseThrow().toString(), alterada);
            return null;
        }).when(adesoes).alterar(any());
        when(adesoes.buscarAtivaDoAluno(any())).thenAnswer(chamada -> adesoesCadastradas.values().stream()
                .filter(adesao -> adesao.ativa() && adesao.getAluno().equals(chamada.getArgument(0))).findFirst());
        when(adesoes.buscarAtivas()).thenAnswer(
                chamada -> adesoesCadastradas.values().stream().filter(AdesaoPlano::ativa).toList());
    }

    private void configurarMensalidades() {
        when(mensalidades.existePorIdentificador(any())).thenAnswer(
                chamada -> mensalidadesCadastradas.containsKey(chamada.getArgument(0).toString()));
        when(mensalidades.buscarPorIdentificador(any())).thenAnswer(
                chamada -> mensalidadesCadastradas.get(chamada.getArgument(0).toString()));
        when(mensalidades.bloquear(any())).thenAnswer(chamada -> mensalidadesCadastradas.get(chamada.getArgument(0).toString()));
        when(mensalidades.incluir(any())).thenAnswer(chamada -> guardar(chamada.getArgument(0),
                Optional.of(new MensalidadeIdentificador(String.valueOf(proximoIdentificador++)))));
        doAnswer(chamada -> {
            Mensalidade alterada = chamada.getArgument(0);
            guardar(alterada, alterada.getIdentificador());
            return null;
        }).when(mensalidades).alterar(any());
        when(mensalidades.existePorAdesaoEPeriodo(any(), any())).thenAnswer(chamada -> mensalidadesCadastradas.values().stream()
                .anyMatch(mensalidade -> mensalidade.getAdesao().equals(chamada.getArgument(0))
                        && mensalidade.getPeriodo().inicio().equals(chamada.getArgument(1))));
    }

    /** Simula a chave gerada pelo banco para pagamentos novos. */
    private Mensalidade guardar(Mensalidade mensalidade, Optional<MensalidadeIdentificador> identificador) {
        var pagamentos = new ArrayList<Pagamento>();
        for (var pagamento : mensalidade.getPagamentos()) {
            pagamentos.add(pagamento.getIdentificador().isPresent() ? pagamento
                    : new Pagamento(Optional.of(proximoPagamento++), pagamento.getValor(), pagamento.getForma(),
                            pagamento.getRegistro(), pagamento.getSituacao(), pagamento.getEstorno(),
                            pagamento.getMotivoEstorno()));
        }
        var salva = new Mensalidade(identificador, mensalidade.getAdesao(), mensalidade.getAluno(), mensalidade.getPeriodo(),
                mensalidade.getVencimento(), mensalidade.getValor(), mensalidade.getSituacao(), pagamentos);
        mensalidadesCadastradas.put(identificador.orElseThrow().toString(), salva);
        return salva;
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException excecao) {
            falha = excecao;
        }
    }

    private Plano planoPorNome(String nome) {
        return planosCadastrados.values().stream().filter(plano -> plano.getNome().equalsIgnoreCase(nome.strip()))
                .reduce((primeiro, segundo) -> segundo).orElseThrow();
    }

    private AdesaoPlano adesaoAoPlano(String nome) {
        var plano = planoPorNome(nome).getIdentificador().orElseThrow();
        return adesoesCadastradas.values().stream().filter(adesao -> adesao.getPlano().equals(plano))
                .reduce((primeiro, segundo) -> segundo).orElseThrow();
    }

    private AdesaoPlano adesaoAtivaDoAluno(String aluno) {
        return adesoesCadastradas.values().stream()
                .filter(adesao -> adesao.ativa() && adesao.getAluno().equals(new AlunoIdentificador(aluno))).findFirst()
                .orElseThrow();
    }

    private Mensalidade mensalidadeAtual() {
        return mensalidadesCadastradas.values().stream()
                .max(Comparator.comparing((Mensalidade mensalidade) -> mensalidade.getPeriodo().inicio())).orElseThrow();
    }

    private LocalDate hoje() {
        return agora.atZone(ParametrosAcademia.FUSO).toLocalDate();
    }

    private static Instant meioDia(String data) {
        return LocalDate.parse(data).atTime(12, 0).atZone(ParametrosAcademia.FUSO).toInstant();
    }

    @Dado("que estou autenticado como administrador no financeiro")
    public void autenticarAdministrador() {
        papel = Autorizacao.ADMINISTRADOR.name();
    }

    @Dado("que estou autenticado como aluno no financeiro")
    public void autenticarAluno() {
        papel = Autorizacao.USUARIO.name();
    }

    @Dado("que hoje é {string} no financeiro")
    public void definirHoje(String data) {
        agora = meioDia(data);
    }

    @Dado("que existe o aluno {string} no financeiro")
    public void cadastrarAluno(String aluno) {
        alunosCadastrados.add(aluno);
    }

    @Dado("o plano ativo {string} de {string} com duração de {int} meses")
    public void cadastrarPlanoExistente(String nome, String valor, int duracao) {
        servicoPlano.cadastrar(nome, new BigDecimal(valor), duracao);
    }

    @Dado("que o plano {string} foi inativado")
    public void inativarPlanoExistente(String nome) {
        servicoPlano.inativar(planoPorNome(nome).getIdentificador().orElseThrow());
    }

    @Dado("que o aluno {string} aderiu ao plano {string}")
    public void aderirExistente(String aluno, String plano) {
        ultimaAdesao = servicoAdesao.aderir(new AlunoIdentificador(aluno), planoPorNome(plano).getIdentificador().orElseThrow());
    }

    @Dado("que o aluno {string} aderiu ao plano {string} em {string}")
    public void aderirExistenteEm(String aluno, String plano, String data) {
        var atual = agora;
        agora = meioDia(data);
        aderirExistente(aluno, plano);
        agora = atual;
    }

    @Dado("que as mensalidades foram geradas")
    public void gerarExistentes() {
        servicoAdesao.gerarMensalidades();
    }

    @Dado("que o plano do aluno {string} foi cancelado")
    public void cancelarExistente(String aluno) {
        servicoAdesao.cancelar(adesaoAtivaDoAluno(aluno).getIdentificador().orElseThrow());
    }

    @Dado("que a mensalidade atual foi paga em {string}")
    public void pagarExistente(String forma) {
        servicoMensalidade.registrarPagamento(mensalidadeAtual().getIdentificador().orElseThrow(), FormaPagamento.valueOf(forma));
    }

    @Quando("eu cadastrar o plano {string} de {string} com duração de {int} meses")
    public void cadastrarPlano(String nome, String valor, int duracao) {
        executar(() -> servicoPlano.cadastrar(nome, new BigDecimal(valor), duracao));
    }

    @Quando("eu inativar o plano {string}")
    public void inativarPlano(String nome) {
        executar(() -> servicoPlano.inativar(planoPorNome(nome).getIdentificador().orElseThrow()));
    }

    @Quando("eu alterar o plano {string} para {string} de {string} com duração de {int} meses")
    public void alterarPlano(String atual, String nome, String valor, int duracao) {
        var identificador = planoPorNome(atual).getIdentificador().orElseThrow();
        executar(() -> servicoPlano.editar(identificador, nome, new BigDecimal(valor), duracao));
    }

    @Quando("o aluno {string} aderir ao plano {string}")
    public void aderir(String aluno, String plano) {
        executar(() -> ultimaAdesao = servicoAdesao.aderir(new AlunoIdentificador(aluno),
                planoPorNome(plano).getIdentificador().orElseThrow()));
    }

    @Quando("eu trocar o plano do aluno {string} para {string}")
    public void trocar(String aluno, String plano) {
        var adesao = adesaoAtivaDoAluno(aluno).getIdentificador().orElseThrow();
        executar(() -> ultimaAdesao = servicoAdesao.trocar(adesao, planoPorNome(plano).getIdentificador().orElseThrow()));
    }

    @Quando("eu cancelar o plano do aluno {string}")
    public void cancelar(String aluno) {
        var adesao = adesaoAtivaDoAluno(aluno).getIdentificador().orElseThrow();
        executar(() -> ultimaAdesao = servicoAdesao.cancelar(adesao));
    }

    @Quando("eu cancelar novamente a última adesão")
    public void cancelarNovamente() {
        executar(() -> servicoAdesao.cancelar(ultimaAdesao.getIdentificador().orElseThrow()));
    }

    @Quando("eu gerar as mensalidades")
    public void gerarMensalidades() {
        executar(() -> geradas = servicoAdesao.gerarMensalidades());
    }

    @Quando("eu registrar o pagamento da mensalidade atual em {string}")
    public void registrarPagamento(String forma) {
        var mensalidade = mensalidadeAtual().getIdentificador().orElseThrow();
        executar(() -> servicoMensalidade.registrarPagamento(mensalidade, FormaPagamento.valueOf(forma)));
    }

    @Quando("eu estornar o pagamento da mensalidade atual pelo motivo {string}")
    public void estornar(String motivo) {
        var mensalidade = mensalidadeAtual();
        var pagamento = mensalidade.getPagamentos().get(0).getIdentificador().orElseThrow();
        executar(() -> servicoMensalidade.estornarPagamento(mensalidade.getIdentificador().orElseThrow(), pagamento, motivo));
    }

    @Dado("que o aluno {string} aderiu ao plano {string} com recorrência de {int} cobranças")
    public void aderirComRecorrenciaExistente(String aluno, String plano, int quantidade) {
        ultimaAdesao = servicoAdesao.aderir(new AlunoIdentificador(aluno), planoPorNome(plano).getIdentificador().orElseThrow(),
                Optional.of(quantidade));
    }

    @Quando("o aluno {string} aderir ao plano {string} com recorrência de {int} cobranças")
    public void aderirComRecorrencia(String aluno, String plano, int quantidade) {
        executar(() -> ultimaAdesao = servicoAdesao.aderir(new AlunoIdentificador(aluno),
                planoPorNome(plano).getIdentificador().orElseThrow(), Optional.of(quantidade)));
    }

    @Quando("eu trocar o plano do aluno {string} para {string} com recorrência de {int} cobranças")
    public void trocarComRecorrencia(String aluno, String plano, int quantidade) {
        var adesao = adesaoAtivaDoAluno(aluno).getIdentificador().orElseThrow();
        executar(() -> ultimaAdesao = servicoAdesao.trocar(adesao, planoPorNome(plano).getIdentificador().orElseThrow(),
                Optional.of(quantidade)));
    }

    @Quando("a rotina do sistema concluir as adesões com prazo cumprido")
    public void concluirPrazosCumpridos() {
        executar(() -> concluidas = servicoAdesao.concluirPrazosCumpridos());
    }

    @Então("a adesão ao plano {string} prevê {int} cobranças até {string}")
    public void verificarRecorrencia(String plano, int quantidade, String termino) {
        assertNull(falha, () -> falha.getMessage());
        var adesao = adesaoAoPlano(plano);
        assertEquals(Optional.of(quantidade), adesao.getQuantidadePeriodos());
        assertEquals(Optional.of(LocalDate.parse(termino)), adesao.terminoPrevisto());
        assertEquals(quantidade, adesao.periodosContratados().size());
    }

    @Então("a adesão ao plano {string} não tem recorrência")
    public void verificarSemRecorrencia(String plano) {
        var adesao = adesaoAoPlano(plano);
        assertEquals(Optional.empty(), adesao.getQuantidadePeriodos());
        assertEquals(Optional.empty(), adesao.terminoPrevisto());
    }

    @Então("a adesão ao plano {string} está {string}")
    public void verificarSituacaoAdesao(String plano, String situacao) {
        assertNull(falha, () -> falha.getMessage());
        assertEquals(SituacaoAdesao.valueOf(situacao), adesaoAoPlano(plano).getSituacao());
    }

    @Então("foram concluídas {int} adesões")
    public void verificarConcluidas(int quantidade) {
        assertNull(falha, () -> falha.getMessage());
        assertEquals(quantidade, concluidas);
    }

    @Então("o plano {string} fica ativo com valor {string} e duração de {int} meses")
    public void verificarPlano(String nome, String valor, int duracao) {
        assertNull(falha, () -> falha.getMessage());
        var plano = planoPorNome(nome);
        assertEquals(SituacaoPlano.ATIVO, plano.getSituacao());
        assertEquals(new BigDecimal(valor), plano.getValor());
        assertEquals(duracao, plano.getDuracaoMeses());
    }

    @Então("a operação financeira é rejeitada com a mensagem {string}")
    public void verificarRejeicao(String mensagem) {
        assertNotNull(falha, "A operação deveria ter sido rejeitada");
        assertEquals(mensagem, falha.getMessage());
    }

    @Então("a adesão ao plano {string} fica {string} em {string}")
    public void verificarAdesaoFinalizada(String plano, String situacao, String data) {
        assertNull(falha, () -> falha.getMessage());
        var adesao = adesaoAoPlano(plano);
        assertEquals(SituacaoAdesao.valueOf(situacao), adesao.getSituacao());
        assertEquals(Optional.of(LocalDate.parse(data)), adesao.getEncerramento());
    }

    @Então("o aluno {string} tem uma única adesão ativa, no plano {string}, iniciada em {string}")
    public void verificarAdesaoAtiva(String aluno, String plano, String data) {
        var ativas = adesoesCadastradas.values().stream()
                .filter(adesao -> adesao.ativa() && adesao.getAluno().equals(new AlunoIdentificador(aluno))).toList();
        assertEquals(1, ativas.size());
        assertEquals(planoPorNome(plano).getIdentificador().orElseThrow(), ativas.get(0).getPlano());
        assertEquals(LocalDate.parse(data), ativas.get(0).getInicio());
    }

    @Então("existem {int} mensalidades da adesão ao plano {string}")
    public void verificarQuantidade(int quantidade, String plano) {
        assertNull(falha, () -> falha.getMessage());
        var adesao = adesaoAoPlano(plano).getIdentificador().orElseThrow();
        assertEquals(quantidade, mensalidadesCadastradas.values().stream()
                .filter(mensalidade -> mensalidade.getAdesao().equals(adesao)).count());
    }

    @Então("a mensalidade do período {string} a {string} vale {string} e vence em {string}")
    public void verificarMensalidade(String inicio, String fim, String valor, String vencimento) {
        List<Mensalidade> encontradas = mensalidadesCadastradas.values().stream()
                .filter(mensalidade -> mensalidade.getPeriodo().inicio().equals(LocalDate.parse(inicio))).toList();
        assertEquals(1, encontradas.size());
        var mensalidade = encontradas.get(0);
        assertEquals(LocalDate.parse(fim), mensalidade.getPeriodo().fim());
        assertEquals(new BigDecimal(valor), mensalidade.getValor());
        assertEquals(LocalDate.parse(vencimento), mensalidade.getVencimento());
    }

    @Então("foram geradas {int} mensalidades")
    public void verificarGeradas(int quantidade) {
        assertNull(falha, () -> falha.getMessage());
        assertEquals(quantidade, geradas);
    }

    @Então("a mensalidade atual está {string} com valor devido de {string}")
    public void verificarValorDevido(String situacao, String valor) {
        assertNull(falha, () -> falha.getMessage());
        var mensalidade = mensalidadeAtual();
        assertEquals(SituacaoMensalidade.valueOf(situacao.toUpperCase(Locale.ROOT)), mensalidade.situacaoEm(hoje()));
        assertEquals(new BigDecimal(valor), mensalidade.valorDevidoEm(hoje()));
    }

    @Então("a mensalidade atual está {string} com {int} pagamento {string} de {string}")
    public void verificarPagamento(String situacao, int quantidade, String situacaoPagamento, String valor) {
        assertNull(falha, () -> falha.getMessage());
        var mensalidade = mensalidadeAtual();
        assertEquals(SituacaoMensalidade.valueOf(situacao), mensalidade.situacaoEm(hoje()));
        assertEquals(quantidade, mensalidade.getPagamentos().size());
        var pagamento = mensalidade.getPagamentos().get(0);
        assertEquals(SituacaoPagamento.valueOf(situacaoPagamento), pagamento.getSituacao());
        assertEquals(new BigDecimal(valor), pagamento.getValor());
    }
}
