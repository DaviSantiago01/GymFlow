package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.TreinoIdentificador;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.AvaliacaoProfessorIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;
public interface IAvaliacaoProfessorAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<AvaliacaoProfessorProjecao,AvaliacaoProfessorConsulta> {
    AvaliacaoProfessorProjecao detalhe(AvaliacaoProfessorIdentificador id,String usuario);
    Optional<AvaliacaoProfessorProjecao> minha(TreinoIdentificador treino,String usuario);
    List<AvaliacaoProfessorProjecao> recentes(ProfessorIdentificador professor,String usuario);
}
