package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import java.util.List;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.Equipamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.OperacaoEquipamentosAutorizacoes;

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
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoAutorizacoes;
public class AutenticacaoRecurso extends AutenticacaoRecursoAbstrato {
    private final List<Autorizacao> autorizacoes;
    private final OperacaoEquipamentosAutorizacoes<Equipamento> equipamento;
    private final PlanoAutorizacoes plano;
    private final AdesaoPlanoAutorizacoes adesaoPlano;
    private final MensalidadeAutorizacoes mensalidade;
    private final ProfissionalAutorizacoes profissional;
    private final VinculoPersonalAlunoAutorizacoes vinculoPersonalAluno;
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
    public AutenticacaoRecurso(String usuario, List<Autorizacao> autorizacoes,
            AgregadoAutorizacoes<Armario> armario, AgregadoAutorizacoes<ReservaArmario> reservaArmario,
            AgregadoAutorizacoes<Aluno> aluno, PlanoDeTreinoAutorizacoes planoDeTreino, ExecucaoDeExercicioAutorizacoes execucaoDeExercicio,
            AvaliacaoFisicaAutorizacoes avaliacaoFisica, MetaAutorizacoes meta,
            AgregadoAutorizacoes<Professor> professor, AgregadoAutorizacoes<AvaliacaoProfessor> avaliacaoProfessor,
            AgregadoAutorizacoes<GrupoTreino> grupoTreino, PlanoAutorizacoes plano, AdesaoPlanoAutorizacoes adesaoPlano,
            MensalidadeAutorizacoes mensalidade, ProfissionalAutorizacoes profissional,
            VinculoPersonalAlunoAutorizacoes vinculoPersonalAluno,
            OperacaoEquipamentosAutorizacoes<Equipamento> equipamento) {
        super(usuario); this.grupoTreino=grupoTreino; this.armario=armario; this.reservaArmario=reservaArmario;
        this.autorizacoes = List.copyOf(autorizacoes);
        this.aluno=aluno; this.planoDeTreino=planoDeTreino; this.execucaoDeExercicio=execucaoDeExercicio;
        this.avaliacaoFisica=avaliacaoFisica; this.meta=meta;
        this.professor=professor; this.avaliacaoProfessor=avaliacaoProfessor;
        this.plano=plano; this.adesaoPlano=adesaoPlano; this.mensalidade=mensalidade;
        this.profissional=profissional; this.vinculoPersonalAluno=vinculoPersonalAluno;
        this.equipamento = equipamento;
    }
    public List<Autorizacao> getAutorizacoes() { return autorizacoes; }
    public OperacaoEquipamentosAutorizacoes<Equipamento> getEquipamento() { return equipamento; }
    public PlanoAutorizacoes getPlano() { return plano; }
    public AdesaoPlanoAutorizacoes getAdesaoPlano() { return adesaoPlano; }
    public MensalidadeAutorizacoes getMensalidade() { return mensalidade; }
    public ProfissionalAutorizacoes getProfissional() { return profissional; }
    public VinculoPersonalAlunoAutorizacoes getVinculoPersonalAluno() { return vinculoPersonalAluno; }
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
