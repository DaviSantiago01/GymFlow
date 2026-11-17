package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.TreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;
public interface IAvaliacaoProfessorRepositorio extends IAgregadoRepositorio<AvaliacaoProfessorIdentificador,AvaliacaoProfessor> {
    boolean jaAvaliou(TreinoIdentificador treino);
    void registrarParticipacao(TreinoIdentificador treino);
    AvaliacaoProfessor bloquear(AvaliacaoProfessorIdentificador id);
}
