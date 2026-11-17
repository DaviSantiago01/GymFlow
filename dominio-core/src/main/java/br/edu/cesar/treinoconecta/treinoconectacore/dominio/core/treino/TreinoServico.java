package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino;

import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.spi.ITreinoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.*;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
public final class TreinoServico extends AgregadoServicoAbstrato<TreinoIdentificador,Treino,AgregadoAutorizacoes<Treino>> {
    private final ITreinoRepositorio treinos;private final IAlunoRepositorio alunos;
    public TreinoServico(ISegurancaServico seguranca,ITreinoRepositorio treinos,IAlunoRepositorio alunos){super(seguranca,treinos,"Treino %s existente","Treino %s inexistente");this.treinos=Objects.requireNonNull(treinos);this.alunos=Objects.requireNonNull(alunos);}
    public AgregadoAutorizacoes<Treino> criarAutorizacoes(Optional<TreinoIdentificador> id){
        Objects.requireNonNull(id);boolean permitido=getSegurancaServico().possuiAutorizacao("USUARIO");
        if(permitido && id.isPresent()) permitido=treinos.buscarPorIdentificador(id.get()).aluno().equals(alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow());
        return new AgregadoAutorizacoes<>(false,permitido,false,false);
    }
}
