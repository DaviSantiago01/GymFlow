package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.spi.IExercicioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.spi.IExecucaoDeExercicioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.ObjetivoTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Serie;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.SituacaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.Treino;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.spi.IPlanoDeTreinoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public final class ExecucaoDeExercicioStepDefinitions {
    private static final Instant AGORA = Instant.parse("2026-11-09T12:00:00Z");

    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private final IExecucaoDeExercicioRepositorio repositorio = mock(IExecucaoDeExercicioRepositorio.class);
    private final IPlanoDeTreinoRepositorio planos = mock(IPlanoDeTreinoRepositorio.class);
    private final IExercicioRepositorio exercicios = mock(IExercicioRepositorio.class);
    private final IAlunoRepositorio alunos = mock(IAlunoRepositorio.class);
    private final ExecucaoDeExercicioServico servico = new ExecucaoDeExercicioServico(seguranca, repositorio, planos,
            exercicios, alunos, Clock.fixed(AGORA, ZoneOffset.UTC));

    private final Map<String, PlanoDeTreino> planosCadastrados = new HashMap<>();
    private final Map<String, Exercicio> exerciciosCadastrados = new HashMap<>();
    private final List<ExecucaoDeExercicio> execucoes = new ArrayList<>();
    private AlunoIdentificador aluno;
    private ExecucaoDeExercicio registrada;
    private Optional<SugestaoProgressao> sugestao = Optional.empty();
    private HistoricoDeCarga historico;
    private String exercicioConsultado;
    private RuntimeException falha;

    public ExecucaoDeExercicioStepDefinitions() {
        when(seguranca.getUsuario()).thenReturn("usuario");
        when(seguranca.possuiAutorizacao(anyString())).thenReturn(false);
        when(planos.existePorIdentificador(any())).thenAnswer(
                chamada -> planosCadastrados.containsKey(chamada.getArgument(0).toString()));
        when(planos.buscarPorIdentificador(any())).thenAnswer(
                chamada -> planosCadastrados.get(chamada.getArgument(0).toString()));
        when(planos.existeAtivoPorAluno(any())).thenAnswer(chamada -> buscarAtivo(chamada.getArgument(0)) != null);
        when(planos.buscarAtivoPorAluno(any())).thenAnswer(chamada -> buscarAtivo(chamada.getArgument(0)));
        when(exercicios.existePorIdentificador(any())).thenAnswer(
                chamada -> exerciciosCadastrados.containsKey(chamada.getArgument(0).toString()));
        when(exercicios.buscarPorIdentificador(any())).thenAnswer(
                chamada -> exerciciosCadastrados.get(chamada.getArgument(0).toString()));
        when(repositorio.incluir(any())).thenAnswer(chamada -> {
            ExecucaoDeExercicio execucao = chamada.getArgument(0);
            execucoes.add(execucao);
            return execucao;
        });
        when(repositorio.buscarPorAlunoEExercicio(any(), any())).thenAnswer(chamada -> {
            AlunoIdentificador alunoConsultado = chamada.getArgument(0);
            ExercicioIdentificador exercicio = chamada.getArgument(1);
            var encontradas = new ArrayList<ExecucaoDeExercicio>();
            for (var execucao : execucoes) {
                if (execucao.getAluno().equals(alunoConsultado) && execucao.getExercicio().equals(exercicio)) {
                    encontradas.add(execucao);
                }
            }
            return encontradas;
        });
    }

    private PlanoDeTreino buscarAtivo(AlunoIdentificador alunoConsultado) {
        for (var plano : planosCadastrados.values()) {
            if (plano.getAluno().equals(alunoConsultado) && plano.getSituacao() == SituacaoPlano.ATIVO) {
                return plano;
            }
        }
        return null;
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException excecao) {
            falha = excecao;
        }
    }

    private static PersonalIdentificador personal() {
        return new PersonalIdentificador("1");
    }

    private static void conferirLista(String esperado, List<String> valores) {
        assertEquals(List.of(esperado.split(",")), valores);
    }

    private ExercicioIdentificador exercicioConsultado() {
        return new ExercicioIdentificador(exercicioConsultado);
    }

    private Exercicio exercicioDoHistorico() {
        return exerciciosCadastrados.get(exercicioConsultado);
    }

    private static String texto(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    @Dado("que estou autenticado como o aluno {string}")
    public void autenticarAluno(String identificador) {
        aluno = new AlunoIdentificador(identificador);
        when(seguranca.possuiAutorizacao(Autorizacao.USUARIO.name())).thenReturn(true);
        when(alunos.buscarPorUsuario("usuario")).thenReturn(new Aluno(Optional.of(aluno), "usuario", "Aluno"));
    }

    @Dado("o exercício {string} da categoria {string} com carga máxima {string} e sem incremento próprio")
    public void cadastrarExercicio(String identificador, String categoria, String cargaMaxima) {
        exerciciosCadastrados.put(identificador, new Exercicio(Optional.of(new ExercicioIdentificador(identificador)),
                "Exercício", CategoriaExercicio.valueOf(categoria), new BigDecimal(cargaMaxima), Optional.empty()));
    }

    @Dado("o exercício {string} da categoria {string} com carga máxima {string} e incremento próprio de {string}")
    public void cadastrarExercicioComIncremento(String identificador, String categoria, String cargaMaxima,
            String incremento) {
        exerciciosCadastrados.put(identificador,
                new Exercicio(Optional.of(new ExercicioIdentificador(identificador)), "Exercício",
                        CategoriaExercicio.valueOf(categoria), new BigDecimal(cargaMaxima),
                        Optional.of(new BigDecimal(incremento))));
    }

    @Dado("o plano {string} do aluno {string} na situação {string} com o exercício {string} na {string} em {int} séries de {int} a {int} repetições")
    public void cadastrarPlano(String plano, String dono, String situacao, String exercicio, String dia, int series,
            int minimo, int maximo) {
        var serie = new Serie(new ExercicioIdentificador(exercicio), series, minimo, maximo, new BigDecimal("40"), 60);
        var treino = new Treino("A", DiaSemana.valueOf(dia), ObjetivoTreino.HIPERTROFIA, List.of(serie));
        var situacaoPlano = SituacaoPlano.valueOf(situacao);
        planosCadastrados.put(plano,
                new PlanoDeTreino(Optional.of(new PlanoDeTreinoIdentificador(plano)), new AlunoIdentificador(dono),
                        personal(), "Plano", 1, List.of(treino), situacaoPlano, 1, Optional.empty(), AGORA,
                        situacaoPlano == SituacaoPlano.ENCERRADO ? Optional.of(AGORA) : Optional.empty()));
    }

    @Dado("as execuções do exercício {string} no plano {string}")
    public void cadastrarExecucoes(String exercicio, String plano, DataTable tabela) {
        for (var linha : tabela.asMaps()) {
            execucoes.add(new ExecucaoDeExercicio(Optional.empty(), aluno, new PlanoDeTreinoIdentificador(plano),
                    DiaSemana.SEGUNDA, new ExercicioIdentificador(exercicio), Instant.parse(linha.get("data")),
                    Integer.parseInt(linha.get("series")), Integer.parseInt(linha.get("repeticoes")),
                    new BigDecimal(linha.get("carga"))));
        }
    }

    @Quando("eu registrar no plano {string} na {string} o exercício {string} com {int} séries, {int} repetições e carga {string}")
    public void registrar(String plano, String dia, String exercicio, int series, int repeticoes, String carga) {
        executar(() -> registrada = servico.registrar(new PlanoDeTreinoIdentificador(plano), DiaSemana.valueOf(dia),
                new ExercicioIdentificador(exercicio), series, repeticoes, new BigDecimal(carga)));
    }

    @Dado("que o plano {string} foi encerrado")
    @Quando("o plano {string} for encerrado")
    public void encerrarPlano(String plano) {
        planosCadastrados.put(plano, planosCadastrados.get(plano).encerrar(personal(), AGORA));
    }

    @Quando("o plano {string} for revisado para uma nova versão")
    public void revisarPlano(String plano) {
        var atual = planosCadastrados.get(plano);
        var revisado = atual.revisar(personal(), "Nova versão", 1, atual.getTreinos(), AGORA);
        planosCadastrados.put(plano, atual.substituir(personal(), AGORA));
        planosCadastrados.put("99", revisado);
    }

    @Quando("eu consultar a sugestão de progressão do exercício {string}")
    public void consultarSugestao(String exercicio) {
        sugestao = servico.sugerirProgressao(aluno, new ExercicioIdentificador(exercicio));
    }

    @Quando("eu consultar o histórico do exercício {string}")
    public void consultarHistorico(String exercicio) {
        exercicioConsultado = exercicio;
        historico = servico.consultarHistorico(aluno, exercicioConsultado());
    }

    @Então("a execução é registrada")
    public void conferirRegistro() {
        assertNull(falha);
        assertNotNull(registrada);
        verify(repositorio).incluir(any());
    }

    @Então("a execução é rejeitada porque só o plano ativo aceita execuções")
    public void conferirPlanoNaoAtivo() {
        assertInstanceOf(IllegalStateException.class, falha);
        assertEquals(PlanoDeTreinoServico.PLANO_NAO_ATIVO, falha.getMessage());
        verify(repositorio, never()).incluir(any());
    }

    @Então("a execução é rejeitada porque o plano está encerrado")
    public void conferirPlanoEncerrado() {
        assertInstanceOf(IllegalStateException.class, falha);
        assertEquals(PlanoDeTreinoServico.PLANO_ENCERRADO, falha.getMessage());
    }

    @Então("a execução é rejeitada por falta de vínculo com o plano")
    public void conferirVinculo() {
        assertInstanceOf(NaoAutorizadoExcecao.class, falha);
        assertEquals(PlanoDeTreinoServico.PLANO_DE_OUTRO_ALUNO, falha.getMessage());
    }

    @Então("a execução é rejeitada porque o exercício {string} não pertence ao treino de {string}")
    public void conferirExercicioForaDoTreino(String exercicio, String dia) {
        assertEquals(PlanoDeTreinoServico.EXERCICIO_FORA_DO_TREINO_MODELO.formatted(exercicio, dia), falha.getMessage());
    }

    @Então("a execução é rejeitada com a mensagem {string}")
    public void conferirMensagem(String mensagem) {
        assertNotNull(falha);
        assertEquals(mensagem, falha.getMessage());
        verify(repositorio, never()).incluir(any());
    }

    @Então("a execução guarda o exercício {string}, {int} séries, {int} repetições, carga {string} e a data atual")
    public void conferirDados(String exercicio, int series, int repeticoes, String carga) {
        assertEquals(new ExercicioIdentificador(exercicio), registrada.getExercicio());
        assertEquals(series, registrada.getSeries());
        assertEquals(repeticoes, registrada.getRepeticoes());
        assertEquals(0, new BigDecimal(carga).compareTo(registrada.getCarga()));
        assertEquals(AGORA, registrada.getData());
    }

    @Então("o histórico do exercício {string} tem {int} execuções")
    public void conferirTamanhoHistorico(String exercicio, int quantidade) {
        exercicioConsultado = exercicio;
        assertEquals(quantidade, servico.consultarHistorico(aluno, exercicioConsultado()).execucoes().size());
    }

    @Então("nenhuma execução foi alterada ou excluída")
    public void conferirPreservacao() {
        verify(repositorio, never()).alterar(any());
        verify(repositorio, never()).excluir(any());
    }

    @Então("a carga sugerida é {string} sem atingir a progressão máxima")
    public void conferirSugestao(String carga) {
        assertTrue(sugestao.isPresent());
        assertEquals(0, new BigDecimal(carga).compareTo(sugestao.get().cargaSugerida()));
        assertFalse(sugestao.get().progressaoMaximaAtingida());
    }

    @Então("a carga sugerida é {string} com a progressão máxima atingida")
    public void conferirProgressaoMaxima(String carga) {
        assertTrue(sugestao.isPresent());
        assertEquals(0, new BigDecimal(carga).compareTo(sugestao.get().cargaSugerida()));
        assertTrue(sugestao.get().progressaoMaximaAtingida());
    }

    @Então("não há sugestão de progressão")
    public void conferirSemSugestao() {
        assertTrue(sugestao.isEmpty());
    }

    @Então("as cargas em ordem cronológica são {string}")
    public void conferirCargas(String cargas) {
        var valores = new ArrayList<String>();
        for (var execucao : historico.execucoes()) {
            valores.add(texto(execucao.getCarga()));
        }
        conferirLista(cargas, valores);
    }

    @Então("as repetições em ordem cronológica são {string}")
    public void conferirRepeticoes(String repeticoes) {
        var valores = new ArrayList<String>();
        for (var execucao : historico.execucoes()) {
            valores.add(String.valueOf(execucao.getRepeticoes()));
        }
        conferirLista(repeticoes, valores);
    }

    @Então("as séries em ordem cronológica são {string}")
    public void conferirSeries(String series) {
        var valores = new ArrayList<String>();
        for (var execucao : historico.execucoes()) {
            valores.add(String.valueOf(execucao.getSeries()));
        }
        conferirLista(series, valores);
    }

    @Então("a execução continua vinculada ao plano {string} com carga {string}")
    public void conferirVinculoMantido(String plano, String carga) {
        assertEquals(1, execucoes.size());
        assertEquals(new PlanoDeTreinoIdentificador(plano), execucoes.get(0).getPlano());
        assertEquals(0, new BigDecimal(carga).compareTo(execucoes.get(0).getCarga()));
    }

    @Então("a maior carga válida é {string}")
    public void conferirMaiorCarga(String carga) {
        var maior = historico.maiorCargaValida(exercicioDoHistorico());
        assertTrue(maior.isPresent());
        assertEquals(0, new BigDecimal(carga).compareTo(maior.get()));
    }

    @Então("não há maior carga válida")
    public void conferirSemMaiorCarga() {
        assertTrue(historico.maiorCargaValida(exercicioDoHistorico()).isEmpty());
    }
}
