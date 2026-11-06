package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorIdentificador;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;
public interface IProfessorAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<ProfessorProjecao,ProfessorConsulta> {
    ProfessorProjecao detalhe(ProfessorIdentificador id);
}
