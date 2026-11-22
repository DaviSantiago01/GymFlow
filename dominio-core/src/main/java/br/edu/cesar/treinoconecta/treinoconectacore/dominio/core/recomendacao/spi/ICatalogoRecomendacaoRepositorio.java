package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.spi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.ExercicioClassificado;

/** Consultas de apoio às recomendações: catálogo de exercícios, entradas registradas e planos ofertados. */
public interface ICatalogoRecomendacaoRepositorio {
    List<ExercicioClassificado> exerciciosClassificados();

    List<LocalDateTime> entradasDoAluno(AlunoIdentificador aluno, LocalDate desde);

    List<LocalDateTime> entradasDaAcademia(LocalDate desde, LocalDate ate);

    List<Plano> planosAtivos();
}
