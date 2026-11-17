package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.Armario;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoAutorizacoes;
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
    private final AgregadoAutorizacoes<Aluno> aluno;
    private final PlanoDeTreinoAutorizacoes planoDeTreino;
    private final ExecucaoDeExercicioAutorizacoes execucaoDeExercicio;
    private final AvaliacaoFisicaAutorizacoes avaliacaoFisica;
    private final MetaAutorizacoes meta;
    private final AgregadoAutorizacoes<Professor> professor;
    private final AgregadoAutorizacoes<AvaliacaoProfessor> avaliacaoProfessor;
    public AutenticacaoRecurso(String usuario, AgregadoAutorizacoes<Armario> armario, AgregadoAutorizacoes<ReservaArmario> reservaArmario,
            AgregadoAutorizacoes<Aluno> aluno, PlanoDeTreinoAutorizacoes planoDeTreino, ExecucaoDeExercicioAutorizacoes execucaoDeExercicio,
            AvaliacaoFisicaAutorizacoes avaliacaoFisica, MetaAutorizacoes meta,
            AgregadoAutorizacoes<Professor> professor, AgregadoAutorizacoes<AvaliacaoProfessor> avaliacaoProfessor,
            AgregadoAutorizacoes<GrupoTreino> grupoTreino) {
        super(usuario); this.grupoTreino=grupoTreino; this.armario=armario; this.reservaArmario=reservaArmario;
        this.aluno=aluno; this.planoDeTreino=planoDeTreino; this.execucaoDeExercicio=execucaoDeExercicio;
        this.avaliacaoFisica=avaliacaoFisica; this.meta=meta;
        this.professor=professor; this.avaliacaoProfessor=avaliacaoProfessor;
    }
    public AgregadoAutorizacoes<Professor> getProfessor() { return professor; }
    public AgregadoAutorizacoes<AvaliacaoProfessor> getAvaliacaoProfessor() { return avaliacaoProfessor; }
    public AgregadoAutorizacoes<Armario> getArmario() { return armario; }
    public AgregadoAutorizacoes<ReservaArmario> getReservaArmario() { return reservaArmario; }
    public AgregadoAutorizacoes<Aluno> getAluno() { return aluno; }
    public PlanoDeTreinoAutorizacoes getPlanoDeTreino() { return planoDeTreino; }
    public ExecucaoDeExercicioAutorizacoes getExecucaoDeExercicio() { return execucaoDeExercicio; }
    public AvaliacaoFisicaAutorizacoes getAvaliacaoFisica() { return avaliacaoFisica; }
    public MetaAutorizacoes getMeta() { return meta; }
}
