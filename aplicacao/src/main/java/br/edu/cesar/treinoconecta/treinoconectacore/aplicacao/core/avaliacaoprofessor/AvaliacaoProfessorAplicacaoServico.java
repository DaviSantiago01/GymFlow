package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor;

import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.TreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.spi.IAvaliacaoProfessorRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaoprofessor.spi.IAvaliacaoProfessorAplicacaoRepositorio;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.spi.*;
public final class AvaliacaoProfessorAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<AvaliacaoProfessorIdentificador,AvaliacaoProfessor,AgregadoAutorizacoes<AvaliacaoProfessor>,AvaliacaoProfessorProjecao,AvaliacaoProfessorConsulta> {
    private final IAvaliacaoProfessorAplicacaoRepositorio consultas;
    private final IAvaliacaoProfessorRepositorio registros;
    private final IAlunoRepositorio alunos;
    private final ISegurancaServico seguranca;
    public AvaliacaoProfessorAplicacaoServico(IAvaliacaoProfessorAplicacaoRepositorio consultas,AvaliacaoProfessorServico servico,
            IAvaliacaoProfessorRepositorio registros,IAlunoRepositorio alunos,ISegurancaServico seguranca) {
        super(consultas,servico);this.consultas=Objects.requireNonNull(consultas);this.registros=Objects.requireNonNull(registros);
        this.alunos=Objects.requireNonNull(alunos);this.seguranca=Objects.requireNonNull(seguranca);
    }
    private String usuario() {
        if(!seguranca.possuiAutorizacao("USUARIO")) throw new NaoAutorizadoExcecao("Acesso exclusivo de alunos");
        return seguranca.getUsuario();
    }
    public AvaliacaoProfessorProjecao detalhe(AvaliacaoProfessorIdentificador id) { return consultas.detalhe(id,usuario()); }
    public MinhaAvaliacao minha(TreinoIdentificador treino) {
        var usuario=usuario();var aluno=alunos.buscarPorUsuario(usuario).getIdentificador().orElseThrow();
        return new MinhaAvaliacao(consultas.minha(treino,usuario),registros.jaAvaliou(treino));
    }
    public List<AvaliacaoProfessorProjecao> recentes(ProfessorIdentificador professor) { return consultas.recentes(professor,usuario()); }
}
