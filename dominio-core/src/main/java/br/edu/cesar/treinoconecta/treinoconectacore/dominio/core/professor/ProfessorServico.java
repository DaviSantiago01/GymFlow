package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor;

import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.spi.IProfessorRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.*;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
public final class ProfessorServico extends AgregadoServicoAbstrato<ProfessorIdentificador,Professor,AgregadoAutorizacoes<Professor>> {
    public ProfessorServico(ISegurancaServico seguranca,IProfessorRepositorio repositorio) { super(seguranca,repositorio,"Professor %s existente","Professor %s inexistente"); }
    public AgregadoAutorizacoes<Professor> criarAutorizacoes(Optional<ProfessorIdentificador> id) {
        Objects.requireNonNull(id);
        return new AgregadoAutorizacoes<>(false,getSegurancaServico().possuiAutorizacao("USUARIO"),false,false);
    }
}
