package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.Armario;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.ReservaArmario;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.Professor;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.AvaliacaoProfessor;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.seguranca.AutenticacaoRecursoAbstrato;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.GrupoTreino;
public class AutenticacaoRecurso extends AutenticacaoRecursoAbstrato {
    private final AgregadoAutorizacoes<GrupoTreino> grupoTreino;
    public AgregadoAutorizacoes<GrupoTreino> getGrupoTreino(){return grupoTreino;}
    private final AgregadoAutorizacoes<Armario> armario;
    private final AgregadoAutorizacoes<ReservaArmario> reservaArmario;
    private final AgregadoAutorizacoes<Professor> professor;
    private final AgregadoAutorizacoes<AvaliacaoProfessor> avaliacaoProfessor;
    public AutenticacaoRecurso(String usuario, AgregadoAutorizacoes<Armario> armario, AgregadoAutorizacoes<ReservaArmario> reservaArmario, AgregadoAutorizacoes<Professor> professor, AgregadoAutorizacoes<AvaliacaoProfessor> avaliacaoProfessor,AgregadoAutorizacoes<GrupoTreino> grupoTreino) {
        super(usuario); this.grupoTreino=grupoTreino; this.armario=armario; this.reservaArmario=reservaArmario;this.professor=professor;this.avaliacaoProfessor=avaliacaoProfessor;
    }
    public AgregadoAutorizacoes<Professor> getProfessor() { return professor; }
    public AgregadoAutorizacoes<AvaliacaoProfessor> getAvaliacaoProfessor() { return avaliacaoProfessor; }
    public AgregadoAutorizacoes<Armario> getArmario() { return armario; }
    public AgregadoAutorizacoes<ReservaArmario> getReservaArmario() { return reservaArmario; }
}
