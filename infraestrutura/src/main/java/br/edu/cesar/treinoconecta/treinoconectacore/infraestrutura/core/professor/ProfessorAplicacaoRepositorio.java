package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.professor;

import java.util.*;
import org.springframework.data.domain.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor.spi.IProfessorAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorIdentificador;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;
public final class ProfessorAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<ProfessorProjecao,ProfessorOrdenacao,ProfessorConsulta> implements IProfessorAplicacaoRepositorio {
    private final IProfessorRepositorioJpa jpa;
    public ProfessorAplicacaoRepositorio(IProfessorRepositorioJpa jpa) { this.jpa=Objects.requireNonNull(jpa); }
    protected Page<ProfessorProjecao> pesquisar(ProfessorConsulta consulta,Pageable pagina) {
        return jpa.pesquisar(paraLike(consulta.getBusca()),PageRequest.of(pagina.getPageNumber(),pagina.getPageSize(),pagina.getSort().isSorted()?pagina.getSort():Sort.by("nome").and(Sort.by("identificador"))));
    }
    protected String paraPropriedadeOrdenacao(ProfessorOrdenacao ordenacao) { return "identificador"; }
    public ProfessorProjecao detalhe(ProfessorIdentificador id) { return jpa.detalhe(Long.valueOf(id.toString())).orElseThrow(()->new NoSuchElementException("Professor inexistente")); }
}
