package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.treino;

import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.spi.ITreinoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.professor.IProfessorRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;
public final class TreinoRepositorio extends AgregadoRepositorioAbstrato<TreinoIdentificador,Treino,Long,TreinoJpa> implements ITreinoRepositorio {
    private final ITreinoRepositorioJpa jpa;private final IAlunoRepositorioJpa alunos;private final IProfessorRepositorioJpa professores;
    public TreinoRepositorio(ITreinoRepositorioJpa jpa,IAlunoRepositorioJpa alunos,IProfessorRepositorioJpa professores){super(jpa,"Treino %s inexistente");this.jpa=Objects.requireNonNull(jpa);this.alunos=Objects.requireNonNull(alunos);this.professores=Objects.requireNonNull(professores);}
    protected Long paraIdentificadorJpa(TreinoIdentificador id){return Long.valueOf(id.toString());}
    protected TreinoJpa paraJpa(Treino t){return new TreinoJpa(t.identificador().map(this::paraIdentificadorJpa).orElse(null),alunos.getReferenceById(Long.valueOf(t.aluno().toString())),professores.getReferenceById(Long.valueOf(t.professor().toString())),t.descricao(),t.inicio(),t.termino(),t.concluido(),t.demonstracao());}
    protected Treino paraAgregado(TreinoJpa t){return new Treino(Optional.of(new TreinoIdentificador(t.getIdentificador().toString())),new AlunoIdentificador(t.getAluno().getIdentificador().toString()),new ProfessorIdentificador(t.getProfessor().getIdentificador().toString()),t.getDescricao(),t.getInicio(),t.getTermino(),t.isConcluido(),t.isDemonstracao());}
    public Treino bloquear(TreinoIdentificador id){return paraAgregado(jpa.bloquear(paraIdentificadorJpa(id)).orElseThrow(()->new NoSuchElementException("Treino inexistente")));}
}
