package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.CategoriaExercicio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.exercicio.ExercicioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.NivelExperiencia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.ObjetivoAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.PerfilAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.perfilaluno.RegiaoCorporal;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/**
 * Exercício com a classificação usada nas recomendações. O grupo muscular é a categoria do exercício; nível, objetivos e
 * contraindicações vêm do cadastro de classificação.
 */
public record ExercicioClassificado(ExercicioIdentificador exercicio, String nome, CategoriaExercicio grupoMuscular,
        NivelExperiencia nivel, Set<ObjetivoAluno> objetivos, Set<RegiaoCorporal> contraindicacoes)
        implements IObjetoValor {
    public ExercicioClassificado {
        Objects.requireNonNull(exercicio);
        Objects.requireNonNull(nome);
        Objects.requireNonNull(grupoMuscular);
        Objects.requireNonNull(nivel);
        objetivos = Set.copyOf(objetivos);
        contraindicacoes = Set.copyOf(contraindicacoes);
    }

    /** RN004 e RN005: motivo pelo qual o exercício não serve para o aluno, ou vazio quando é compatível. */
    public Optional<String> incompatibilidadeCom(PerfilAluno perfil) {
        Objects.requireNonNull(perfil);
        var restricao = perfil.restricaoConflitante(contraindicacoes);
        if (restricao.isPresent()) {
            return Optional.of("Contraindicado para a restrição em " + restricao.get().regiao().getDescricao().toLowerCase());
        }
        if (perfil.getNivel().filter(nivelAluno -> !nivelAluno.comporta(nivel)).isPresent()) {
            return Optional.of("Nível acima do nível do aluno");
        }
        if (perfil.getObjetivo().filter(objetivo -> !objetivos.contains(objetivo)).isPresent()) {
            return Optional.of("Não é adequado ao objetivo do aluno");
        }
        return Optional.empty();
    }
}
