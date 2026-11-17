package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.professor;

import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.spi.IProfessorRepositorio;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;
public final class ProfessorRepositorio extends AgregadoRepositorioAbstrato<ProfessorIdentificador,Professor,Long,ProfessorJpa> implements IProfessorRepositorio {
    public ProfessorRepositorio(IProfessorRepositorioJpa jpa) { super(jpa,"Professor %s inexistente"); }
    protected Long paraIdentificadorJpa(ProfessorIdentificador id) { return Long.valueOf(id.toString()); }
    protected ProfessorJpa paraJpa(Professor p) { return new ProfessorJpa(p.identificador().map(this::paraIdentificadorJpa).orElse(null),p.nome(),p.especialidade(),p.biografia(),p.demonstracao()); }
    protected Professor paraAgregado(ProfessorJpa p) { return new Professor(Optional.of(new ProfessorIdentificador(p.getIdentificador().toString())),p.getNome(),p.getEspecialidade(),p.getBiografia(),p.isDemonstracao()); }
}
