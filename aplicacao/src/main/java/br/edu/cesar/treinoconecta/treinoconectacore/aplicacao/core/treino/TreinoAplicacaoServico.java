package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino;

import java.time.Clock;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.spi.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.spi.*;
public final class TreinoAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<TreinoIdentificador,Treino,AgregadoAutorizacoes<Treino>,TreinoProjecao,TreinoConsulta> {
    private final ITreinoAplicacaoRepositorio consultas;private final TreinoServico servico;
    private final ISegurancaServico seguranca;private final IAlunoRepositorio alunos;
    private final Optional<ITreinoDemonstracaoRepositorio> demonstracao;private final Clock relogio;
    public TreinoAplicacaoServico(ITreinoAplicacaoRepositorio consultas,TreinoServico servico,ISegurancaServico seguranca,IAlunoRepositorio alunos,Optional<ITreinoDemonstracaoRepositorio> demonstracao,Clock relogio){
        super(consultas,servico);this.consultas=Objects.requireNonNull(consultas);this.servico=Objects.requireNonNull(servico);this.seguranca=Objects.requireNonNull(seguranca);this.alunos=Objects.requireNonNull(alunos);this.demonstracao=Objects.requireNonNull(demonstracao);this.relogio=Objects.requireNonNull(relogio);
    }
    public TreinoProjecao detalhe(TreinoIdentificador id){servico.buscar(id);return consultas.detalhe(id,seguranca.getUsuario());}
    public boolean demonstracaoDisponivel(){return demonstracao.isPresent() && seguranca.possuiAutorizacao("USUARIO");}
    public void carregarDemonstracao(){
        if(!demonstracaoDisponivel()) throw new NaoAutorizadoExcecao("Demonstração indisponível neste ambiente");
        var aluno=alunos.bloquearPorUsuario(seguranca.getUsuario()).getIdentificador().orElseThrow();demonstracao.orElseThrow().cadastrar(aluno,relogio.instant());
    }
}
