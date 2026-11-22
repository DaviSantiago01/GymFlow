package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetiva;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.Meta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreino;

/** Dados do aluno e da academia consultados para gerar as recomendações. */
public record ContextoRecomendacao(PerfilAluno perfil, LocalDate hoje, Optional<PlanoDeTreino> planoAtivo,
        Map<ExercicioIdentificador, List<ExecucaoDeExercicio>> execucoes, List<AvaliacaoFisica> avaliacoes,
        List<Meta> metasAtivas, List<LocalDateTime> entradasDoAluno, List<LocalDateTime> entradasDaAcademia,
        List<ExercicioClassificado> catalogo, List<AulaColetiva> aulas, List<AulaColetiva> inscricoes,
        Optional<Plano> planoContratado, Optional<LocalDate> inicioAdesao, List<Plano> planosAtivos) {
    public ContextoRecomendacao {
        Objects.requireNonNull(perfil);
        Objects.requireNonNull(hoje);
        Objects.requireNonNull(planoAtivo);
        execucoes = Map.copyOf(execucoes);
        avaliacoes = List.copyOf(avaliacoes);
        metasAtivas = List.copyOf(metasAtivas);
        entradasDoAluno = List.copyOf(entradasDoAluno);
        entradasDaAcademia = List.copyOf(entradasDaAcademia);
        catalogo = List.copyOf(catalogo);
        aulas = List.copyOf(aulas);
        inscricoes = List.copyOf(inscricoes);
        Objects.requireNonNull(planoContratado);
        Objects.requireNonNull(inicioAdesao);
        planosAtivos = List.copyOf(planosAtivos);
    }

    public Optional<ExercicioClassificado> classificacao(ExercicioIdentificador exercicio) {
        return catalogo.stream().filter(item -> item.exercicio().equals(exercicio)).findFirst();
    }
}
