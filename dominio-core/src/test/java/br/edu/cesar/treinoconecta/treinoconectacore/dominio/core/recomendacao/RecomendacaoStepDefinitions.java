package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.Disponibilidade;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.NivelExperiencia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.ObjetivoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.RegiaoCorporal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public final class RecomendacaoStepDefinitions {
    private final Instant agora = Instant.parse("2026-11-22T12:00:00Z");
    private PerfilAluno perfil;
    private Optional<String> incompatibilidade = Optional.empty();
    private RuntimeException falha;

    @Dado("um aluno sem perfil de treino nas recomendações")
    public void alunoSemPerfil() {
        perfil = PerfilAluno.vazio(new AlunoIdentificador("1"), agora);
    }

    @Dado("que o aluno tem o perfil {string} e nível {string}")
    public void alunoComPerfil(String objetivo, String nivel) {
        perfil = perfil.atualizarDadosDeTreino(ObjetivoAluno.valueOf(objetivo), NivelExperiencia.valueOf(nivel),
                new Disponibilidade(Set.of(DiaSemana.SEGUNDA), LocalTime.of(18, 0), LocalTime.of(21, 0)), agora);
    }

    @Dado("que o aluno registrou uma restrição no {string}")
    public void alunoComRestricao(String regiao) {
        perfil = perfil.registrarRestricao(RegiaoCorporal.valueOf(regiao), "Dor ao agachar", LocalDate.of(2026, 11, 1),
                agora);
    }

    @Quando("o aluno informar o objetivo {string}, o nível {string} e os dias {string} das {string} às {string}")
    public void informarDadosDeTreino(String objetivo, String nivel, String dias, String inicio, String fim) {
        executar(() -> {
            var escolhidos = EnumSet.noneOf(DiaSemana.class);
            Arrays.stream(dias.split(",")).filter(dia -> !dia.isBlank())
                    .forEach(dia -> escolhidos.add(DiaSemana.valueOf(dia.strip())));
            var disponibilidade = new Disponibilidade(escolhidos, LocalTime.parse(inicio), LocalTime.parse(fim));
            perfil = perfil.atualizarDadosDeTreino(ObjetivoAluno.valueOf(objetivo), NivelExperiencia.valueOf(nivel),
                    disponibilidade, agora);
        });
    }

    @Quando("o sistema avaliar um exercício de nível {string} para {string} contraindicado para {string}")
    public void avaliarExercicio(String nivel, String objetivo, String regioes) {
        var contraindicacoes = EnumSet.noneOf(RegiaoCorporal.class);
        Arrays.stream(regioes.split(",")).filter(regiao -> !regiao.isBlank())
                .forEach(regiao -> contraindicacoes.add(RegiaoCorporal.valueOf(regiao.strip())));
        var exercicio = new ExercicioClassificado(new ExercicioIdentificador("1"), "Agachamento livre",
                CategoriaExercicio.PERNAS, NivelExperiencia.valueOf(nivel), Set.of(ObjetivoAluno.valueOf(objetivo)),
                contraindicacoes);
        incompatibilidade = exercicio.incompatibilidadeCom(perfil);
    }

    @Quando("o aluno desativar as recomendações de {string}")
    public void desativarCategoria(String categoria) {
        executar(() -> perfil = perfil.definirPreferencias(Set.of(CategoriaRecomendacao.valueOf(categoria)), agora));
    }

    @Então("o perfil de treino está incompleto")
    public void perfilIncompleto() {
        assertFalse(perfil.completo());
    }

    @Então("o perfil de treino está completo")
    public void perfilCompleto() {
        assertNull(falha);
        assertTrue(perfil.completo());
    }

    @Então("faltam no perfil {string}")
    public void faltamNoPerfil(String campos) {
        assertEquals(campos, String.join(", ", perfil.camposPendentes()));
    }

    @Então("o perfil é rejeitado com a mensagem {string}")
    public void perfilRejeitado(String mensagem) {
        assertNotNull(falha);
        assertEquals(mensagem, falha.getMessage());
    }

    @Então("o exercício é descartado pelo motivo {string}")
    public void exercicioDescartado(String motivo) {
        assertEquals(Optional.of(motivo), incompatibilidade);
    }

    @Então("o exercício pode ser recomendado")
    public void exercicioCompativel() {
        assertEquals(Optional.empty(), incompatibilidade);
    }

    @Então("o aluno não recebe recomendações de {string}")
    public void naoRecebe(String categoria) {
        assertNull(falha);
        assertFalse(perfil.aceita(CategoriaRecomendacao.valueOf(categoria)));
    }

    @Então("o aluno continua recebendo recomendações de {string}")
    public void continuaRecebendo(String categoria) {
        assertTrue(perfil.aceita(CategoriaRecomendacao.valueOf(categoria)));
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException excecao) {
            falha = excecao;
        }
    }
}
