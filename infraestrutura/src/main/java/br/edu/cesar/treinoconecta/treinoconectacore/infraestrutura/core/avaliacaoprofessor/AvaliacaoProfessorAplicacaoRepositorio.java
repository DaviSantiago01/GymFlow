package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaoprofessor;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.TreinoIdentificador;
import java.util.*;
import org.springframework.data.domain.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.spi.IAvaliacaoProfessorAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.AvaliacaoProfessorIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;
public final class AvaliacaoProfessorAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<AvaliacaoProfessorProjecao,AvaliacaoProfessorOrdenacao,AvaliacaoProfessorConsulta> implements IAvaliacaoProfessorAplicacaoRepositorio {
    private final IAvaliacaoProfessorRepositorioJpa jpa;
    private final ISegurancaServico seguranca;
    public AvaliacaoProfessorAplicacaoRepositorio(IAvaliacaoProfessorRepositorioJpa jpa,ISegurancaServico seguranca) { this.jpa=Objects.requireNonNull(jpa);this.seguranca=Objects.requireNonNull(seguranca); }
    protected Page<AvaliacaoProfessorProjecao> pesquisar(AvaliacaoProfessorConsulta consulta,Pageable pagina) { return jpa.minhas(seguranca.getUsuario(),PageRequest.of(pagina.getPageNumber(),pagina.getPageSize())); }
    protected String paraPropriedadeOrdenacao(AvaliacaoProfessorOrdenacao ordenacao) { return "identificador"; }
    public AvaliacaoProfessorProjecao detalhe(AvaliacaoProfessorIdentificador id,String usuario) { return jpa.detalhe(Long.valueOf(id.toString()),usuario).orElseThrow(()->new NoSuchElementException("Avaliação não encontrada para este aluno")); }
    public Optional<AvaliacaoProfessorProjecao> minha(TreinoIdentificador treino,String usuario) { return jpa.minha(Long.valueOf(treino.toString()),usuario); }
    public List<AvaliacaoProfessorProjecao> recentes(ProfessorIdentificador professor,String usuario) { return jpa.recentes(Long.valueOf(professor.toString()),usuario,PageRequest.of(0,10)); }
}
