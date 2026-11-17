package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor;

import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.professor.spi.IProfessorAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.AvaliacaoProfessorAplicacaoServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
public final class ProfessorAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<ProfessorIdentificador,Professor,AgregadoAutorizacoes<Professor>,ProfessorProjecao,ProfessorConsulta> {
    private final IProfessorAplicacaoRepositorio consultas;
    private final ProfessorServico servico;
    private final AvaliacaoProfessorAplicacaoServico avaliacoes;
    public ProfessorAplicacaoServico(IProfessorAplicacaoRepositorio consultas,ProfessorServico servico,AvaliacaoProfessorAplicacaoServico avaliacoes) {
        super(consultas,servico);this.consultas=Objects.requireNonNull(consultas);this.servico=Objects.requireNonNull(servico);this.avaliacoes=Objects.requireNonNull(avaliacoes);
    }
    public ProfessorProjecao detalhe(ProfessorIdentificador id) { servico.buscar(id);return consultas.detalhe(id); }
    public PerfilProfessor perfil(ProfessorIdentificador id) { return new PerfilProfessor(detalhe(id),avaliacoes.recentes(id)); }
}
