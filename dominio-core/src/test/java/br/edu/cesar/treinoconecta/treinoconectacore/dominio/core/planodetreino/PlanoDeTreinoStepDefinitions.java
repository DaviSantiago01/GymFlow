package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
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
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.Exercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.spi.IExercicioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.Personal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi.IPersonalRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.spi.IPlanoDeTreinoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public final class PlanoDeTreinoStepDefinitions {
    private static final Instant AGORA = Instant.parse("2026-11-09T12:00:00Z");

    private final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    private final IPlanoDeTreinoRepositorio repositorio = mock(IPlanoDeTreinoRepositorio.class);
    private final IAlunoRepositorio alunos = mock(IAlunoRepositorio.class);
    private final IPersonalRepositorio personais = mock(IPersonalRepositorio.class);
    private final IExercicioRepositorio exercicios = mock(IExercicioRepositorio.class);
    private final PlanoDeTreinoServico servico = new PlanoDeTreinoServico(seguranca, repositorio, alunos, personais,
            exercicios, Clock.fixed(AGORA, ZoneOffset.UTC));

    private final Map<String, PlanoDeTreino> planos = new HashMap<>();
    private final Set<String> alunosCadastrados = new HashSet<>();
    private final Map<String, String> nomesDosExercicios = new HashMap<>();
    private final Map<String, RascunhoTreino> rascunhos = new LinkedHashMap<>();
    private int proximoIdentificador = 1;
    private PlanoDeTreino plano;
    private PlanoDeTreino planoAnterior;
    private RuntimeException falha;

    private static final class RascunhoTreino {
        private final DiaSemana dia;
        private final ObjetivoTreino objetivo;
        private final List<Serie> series = new ArrayList<>();

        private RascunhoTreino(DiaSemana dia, ObjetivoTreino objetivo) {
            this.dia = dia;
            this.objetivo = objetivo;
        }
    }

    public PlanoDeTreinoStepDefinitions() {
        when(seguranca.getUsuario()).thenReturn("usuario");
        when(seguranca.possuiAutorizacao(anyString())).thenReturn(false);
        when(alunos.existePorIdentificador(any())).thenAnswer(
                chamada -> alunosCadastrados.contains(chamada.getArgument(0).toString()));
        when(alunos.buscarPorIdentificador(any())).thenAnswer(chamada -> {
            AlunoIdentificador identificador = chamada.getArgument(0);
            return new Aluno(Optional.of(identificador), "aluno" + identificador, "Aluno");
        });
        when(exercicios.existePorIdentificador(any())).thenAnswer(
                chamada -> nomesDosExercicios.containsKey(chamada.getArgument(0).toString()));
        when(exercicios.buscarPorIdentificador(any())).thenAnswer(chamada -> {
            ExercicioIdentificador identificador = chamada.getArgument(0);
            return new Exercicio(Optional.of(identificador), nomesDosExercicios.get(identificador.toString()),
                    CategoriaExercicio.PEITO, new BigDecimal("200"), Optional.empty());
        });
        when(repositorio.existePorIdentificador(any())).thenAnswer(
                chamada -> planos.containsKey(chamada.getArgument(0).toString()));
        when(repositorio.buscarPorIdentificador(any())).thenAnswer(chamada -> planos.get(chamada.getArgument(0).toString()));
        when(repositorio.bloquear(any())).thenAnswer(chamada -> planos.get(chamada.getArgument(0).toString()));
        when(repositorio.existeAtivoPorAluno(any())).thenAnswer(chamada -> buscarAtivo(chamada.getArgument(0)) != null);
        when(repositorio.buscarAtivoPorAluno(any())).thenAnswer(chamada -> buscarAtivo(chamada.getArgument(0)));
        when(repositorio.incluir(any())).thenAnswer(chamada -> guardar(chamada.getArgument(0)));
        doAnswer(chamada -> {
            PlanoDeTreino alterado = chamada.getArgument(0);
            planos.put(alterado.getIdentificador().orElseThrow().toString(), alterado);
            return null;
        }).when(repositorio).alterar(any());
    }

    private PlanoDeTreino buscarAtivo(AlunoIdentificador aluno) {
        for (var candidato : planos.values()) {
            if (candidato.getAluno().equals(aluno) && candidato.getSituacao() == SituacaoPlano.ATIVO) {
                return candidato;
            }
        }
        return null;
    }

    private PlanoDeTreino guardar(PlanoDeTreino novo) {
        var identificador = new PlanoDeTreinoIdentificador(String.valueOf(proximoIdentificador++));
        var salvo = new PlanoDeTreino(Optional.of(identificador), novo.getAluno(), novo.getPersonal(), novo.getNome(),
                novo.getFrequenciaSemanal(), novo.getTreinos(), novo.getSituacao(), novo.getVersao(),
                novo.getPlanoAnterior(), novo.getCriacao(), novo.getEncerramento());
        planos.put(identificador.toString(), salvo);
        return salvo;
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException excecao) {
            falha = excecao;
        }
    }

    private static Serie serie(String exercicio, int series, int minimo, int maximo, String carga, int intervalo) {
        return new Serie(new ExercicioIdentificador(exercicio), series, minimo, maximo, new BigDecimal(carga), intervalo);
    }

    private List<Treino> treinosDosRascunhos() {
        var treinos = new ArrayList<Treino>();
        for (var entrada : rascunhos.entrySet()) {
            var rascunho = entrada.getValue();
            treinos.add(new Treino(entrada.getKey(), rascunho.dia, rascunho.objetivo, rascunho.series));
        }
        return treinos;
    }

    private static Treino treinoCompleto(String nome, DiaSemana dia) {
        return new Treino(nome, dia, ObjetivoTreino.HIPERTROFIA, List.of(serie("1", 4, 8, 12, "40", 60)));
    }

    private PlanoDeTreino guardarPlano(String aluno, String personal, int frequencia, List<Treino> treinos,
            SituacaoPlano situacao) {
        return guardar(new PlanoDeTreino(Optional.empty(), new AlunoIdentificador(aluno),
                new PersonalIdentificador(personal), "Plano", frequencia, treinos, situacao, 1, Optional.empty(), AGORA,
                situacao == SituacaoPlano.ENCERRADO ? Optional.of(AGORA) : Optional.empty()));
    }

    private void lembrarPlanoAnterior() {
        if (plano != null) {
            planoAnterior = plano;
        }
    }

    private PlanoDeTreino planoAtualizado(PlanoDeTreino referencia) {
        return planos.get(referencia.getIdentificador().orElseThrow().toString());
    }

    @Dado("que estou autenticado como o personal {string}")
    public void autenticarPersonal(String personal) {
        when(seguranca.possuiAutorizacao(Autorizacao.PERSONAL.name())).thenReturn(true);
        when(personais.buscarPorUsuario("usuario")).thenReturn(
                new Personal(Optional.of(new PersonalIdentificador(personal)), "usuario", "Personal"));
    }

    @Dado("que existe o aluno {string}")
    public void cadastrarAluno(String aluno) {
        alunosCadastrados.add(aluno);
    }

    @Dado("que existe o exercício {string} chamado {string}")
    public void cadastrarExercicio(String exercicio, String nome) {
        nomesDosExercicios.put(exercicio, nome);
    }

    @Dado("um treino {string} na {string} com objetivo {string}")
    public void criarRascunhoDeTreino(String nome, String dia, String objetivo) {
        rascunhos.put(nome, new RascunhoTreino(DiaSemana.valueOf(dia), ObjetivoTreino.valueOf(objetivo)));
    }

    @Dado("no treino {string} o exercício {string} com {int} séries, de {int} a {int} repetições, carga {string} e intervalo {int}")
    public void adicionarSerie(String treino, String exercicio, int series, int minimo, int maximo, String carga,
            int intervalo) {
        rascunhos.get(treino).series.add(serie(exercicio, series, minimo, maximo, carga, intervalo));
    }

    @Dado("um rascunho do aluno {string} com o treino {string} na {string} e frequência {int}")
    public void criarRascunho(String aluno, String treino, String dia, int frequencia) {
        lembrarPlanoAnterior();
        plano = guardarPlano(aluno, "1", frequencia, List.of(treinoCompleto(treino, DiaSemana.valueOf(dia))),
                SituacaoPlano.RASCUNHO);
    }

    @Dado("um rascunho vazio do aluno {string} com frequência {int}")
    public void criarRascunhoVazio(String aluno, int frequencia) {
        plano = guardarPlano(aluno, "1", frequencia, List.of(), SituacaoPlano.RASCUNHO);
    }

    @Dado("um rascunho do aluno {string} com o treino sem exercícios {string} na {string} e frequência {int}")
    public void criarRascunhoSemExercicios(String aluno, String treino, String dia, int frequencia) {
        var semExercicios = new Treino(treino, DiaSemana.valueOf(dia), ObjetivoTreino.HIPERTROFIA, List.of());
        plano = guardarPlano(aluno, "1", frequencia, List.of(semExercicios), SituacaoPlano.RASCUNHO);
    }

    @Dado("um plano ativo do aluno {string} sob responsabilidade do personal {string}")
    public void criarPlanoAtivo(String aluno, String personal) {
        plano = guardarPlano(aluno, personal, 1, List.of(treinoCompleto("A", DiaSemana.SEGUNDA)), SituacaoPlano.ATIVO);
    }

    @Dado("um plano encerrado do aluno {string} sob responsabilidade do personal {string}")
    public void criarPlanoEncerrado(String aluno, String personal) {
        plano = guardarPlano(aluno, personal, 1, List.of(treinoCompleto("A", DiaSemana.SEGUNDA)),
                SituacaoPlano.ENCERRADO);
    }

    @Quando("eu incluir no treino {string} o exercício {string} com {int} séries, de {int} a {int} repetições, carga {string} e intervalo {int}")
    public void tentarIncluirSerie(String treino, String exercicio, int series, int minimo, int maximo, String carga,
            int intervalo) {
        executar(() -> {
            rascunhos.get(treino).series.add(serie(exercicio, series, minimo, maximo, carga, intervalo));
            servico.criar(new AlunoIdentificador("1"), "Plano", 1, treinosDosRascunhos());
        });
    }

    @Quando("eu criar o plano {string} para o aluno {string} com frequência {int}")
    public void criarPlano(String nome, String aluno, int frequencia) {
        executar(() -> plano = servico.criar(new AlunoIdentificador(aluno), nome, frequencia, treinosDosRascunhos()));
    }

    @Quando("eu ativar o plano")
    public void ativarPlano() {
        executar(() -> plano = servico.ativar(plano.getIdentificador().orElseThrow()));
    }

    @Quando("eu substituir o plano atual pelo rascunho")
    public void substituirPlano() {
        executar(() -> plano = servico.substituir(plano.getIdentificador().orElseThrow()));
    }

    @Quando("eu encerrar o plano")
    public void encerrarPlano() {
        executar(() -> plano = servico.encerrar(plano.getIdentificador().orElseThrow()));
    }

    @Quando("eu alterar o plano para o nome {string} com frequência {int}")
    public void alterarPlano(String nome, int frequencia) {
        executar(() -> {
            planoAnterior = plano;
            plano = servico.revisar(plano.getIdentificador().orElseThrow(), nome, frequencia,
                    List.of(treinoCompleto("A", DiaSemana.SEGUNDA)));
        });
    }

    @Então("o plano pertence ao aluno {string}")
    public void conferirAluno(String aluno) {
        assertEquals(new AlunoIdentificador(aluno), plano.getAluno());
    }

    @Então("a situação do plano é {string}")
    public void conferirSituacao(String situacao) {
        assertEquals(SituacaoPlano.valueOf(situacao), planoAtualizado(plano).getSituacao());
    }

    @Então("a situação do plano anterior é {string}")
    public void conferirSituacaoAnterior(String situacao) {
        assertEquals(SituacaoPlano.valueOf(situacao), planoAtualizado(planoAnterior).getSituacao());
    }

    @Então("o plano é rejeitado por aluno inexistente")
    public void conferirAlunoInexistente() {
        assertInstanceOf(AgregadoInexistenteExcecao.class, falha);
        verify(repositorio, never()).incluir(any());
    }

    @Então("o plano é rejeitado com a mensagem {string}")
    public void conferirMensagem(String mensagem) {
        assertNotNull(falha);
        assertEquals(mensagem, falha.getMessage());
    }

    @Então("o plano é rejeitado com a mensagem de plano vazio")
    public void conferirPlanoVazio() {
        assertInstanceOf(IllegalStateException.class, falha);
        assertEquals(PlanoDeTreinoServico.PLANO_VAZIO, falha.getMessage());
    }

    @Então("o plano é rejeitado com a mensagem de treino sem exercícios {string}")
    public void conferirTreinoSemExercicios(String treino) {
        assertEquals(PlanoDeTreinoServico.TREINO_SEM_EXERCICIOS_MODELO.formatted(treino), falha.getMessage());
    }

    @Então("o plano é rejeitado com a mensagem de exercício {string} repetido no treino {string}")
    public void conferirExercicioRepetido(String exercicio, String treino) {
        assertEquals(PlanoDeTreinoServico.EXERCICIO_REPETIDO_MODELO.formatted(exercicio, treino), falha.getMessage());
    }

    @Então("o plano é rejeitado com a mensagem de plano ativo existente")
    public void conferirPlanoAtivoExistente() {
        assertEquals(PlanoDeTreinoServico.PLANO_ATIVO_EXISTENTE, falha.getMessage());
    }

    @Então("o plano é rejeitado por pertencer a outro personal")
    public void conferirOutroPersonal() {
        assertInstanceOf(NaoAutorizadoExcecao.class, falha);
        assertEquals(PlanoDeTreinoServico.PLANO_DE_OUTRO_PERSONAL, falha.getMessage());
    }

    @Então("o plano é rejeitado com a mensagem de frequência divergente de {int} para {int}")
    public void conferirFrequencia(int declarada, int cadastrada) {
        assertEquals(PlanoDeTreinoServico.FREQUENCIA_DIVERGENTE_MODELO.formatted(declarada, cadastrada),
                falha.getMessage());
    }

    @Então("o plano é rejeitado com a mensagem de plano não alterável")
    public void conferirNaoAlteravel() {
        assertEquals(PlanoDeTreinoServico.PLANO_NAO_ALTERAVEL, falha.getMessage());
    }

    @Então("o plano continua consultável")
    public void conferirConsultavel() {
        var consultado = servico.buscar(plano.getIdentificador().orElseThrow());
        assertEquals(SituacaoPlano.ENCERRADO, consultado.getSituacao());
        verify(repositorio, never()).excluir(any());
    }

    @Então("a exclusão do plano não é autorizada")
    public void conferirExclusao() {
        assertThrows(NaoAutorizadoExcecao.class, () -> servico.excluir(plano.getIdentificador().orElseThrow()));
    }

    @Então("o plano está na versão {int} e referencia a versão anterior")
    public void conferirVersao(int versao) {
        assertEquals(versao, plano.getVersao());
        assertTrue(plano.getPlanoAnterior().isPresent());
        assertEquals(planoAnterior.getIdentificador(), plano.getPlanoAnterior());
    }
}
