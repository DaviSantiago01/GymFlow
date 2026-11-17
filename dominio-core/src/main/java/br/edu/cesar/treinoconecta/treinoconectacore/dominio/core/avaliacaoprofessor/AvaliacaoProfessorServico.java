package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor;

import java.time.Clock;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.spi.ITreinoRepositorio;
import java.util.*;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.spi.IProfessorRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.spi.IAvaliacaoProfessorRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.*;
import dev.sauloaraujo.d5.framework.dominio.spi.*;
public final class AvaliacaoProfessorServico extends AgregadoServicoAbstrato<AvaliacaoProfessorIdentificador,AvaliacaoProfessor,AgregadoAutorizacoes<AvaliacaoProfessor>> {
    private final IAvaliacaoProfessorRepositorio avaliacoes;
    private final IAlunoRepositorio alunos;
    private final ITreinoRepositorio treinos;
    private final Clock relogio;
    public AvaliacaoProfessorServico(ISegurancaServico seguranca, IAvaliacaoProfessorRepositorio avaliacoes,IAlunoRepositorio alunos,ITreinoRepositorio treinos,Clock relogio) {
        super(seguranca,avaliacoes,"Avaliação %s existente","Avaliação %s inexistente");
        this.avaliacoes=Objects.requireNonNull(avaliacoes);this.alunos=Objects.requireNonNull(alunos);
        this.treinos=Objects.requireNonNull(treinos);this.relogio=Objects.requireNonNull(relogio);
    }
    public AgregadoAutorizacoes<AvaliacaoProfessor> criarAutorizacoes(Optional<AvaliacaoProfessorIdentificador> id) {
        Objects.requireNonNull(id);
        boolean permitido=getSegurancaServico().possuiAutorizacao("USUARIO");
        if(permitido && id.isPresent()) permitido=avaliacoes.buscarPorIdentificador(id.get()).aluno().equals(alunos.buscarPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow());
        return new AgregadoAutorizacoes<>(false,permitido,false,false);
    }
    private AlunoIdentificador autor() {
        if(!getSegurancaServico().possuiAutorizacao("USUARIO")) throw new NaoAutorizadoExcecao("Acesso exclusivo de alunos");
        return alunos.bloquearPorUsuario(getSegurancaServico().getUsuario()).getIdentificador().orElseThrow();
    }
    public AvaliacaoProfessor avaliar(TreinoIdentificador treino,NotasAvaliacao notas,Optional<String> comentario) {
        var aluno=autor();var aula=treinos.bloquear(Objects.requireNonNull(treino));aula.exigirAvaliavel(aluno,relogio.instant());
        var nova=AvaliacaoProfessor.criar(aluno,aula.professor(),treino,notas,comentario,relogio.instant());
        Validate.validState(!avaliacoes.jaAvaliou(treino),"Você já avaliou este treino. Uma avaliação excluída não pode ser recriada");
        avaliacoes.registrarParticipacao(treino);
        return avaliacoes.incluir(nova);
    }
    public AvaliacaoProfessor editar(AvaliacaoProfessorIdentificador id,NotasAvaliacao notas,Optional<String> comentario) {
        var aluno=autor();var atual=avaliacoes.bloquear(Objects.requireNonNull(id));
        var alterada=atual.editar(aluno,notas,comentario,relogio.instant());avaliacoes.alterar(alterada);return alterada;
    }
    public void remover(AvaliacaoProfessorIdentificador id) {
        var aluno=autor();var atual=avaliacoes.bloquear(Objects.requireNonNull(id));atual.exigirAutor(aluno);
        avaliacoes.excluir(id);
    }
}
