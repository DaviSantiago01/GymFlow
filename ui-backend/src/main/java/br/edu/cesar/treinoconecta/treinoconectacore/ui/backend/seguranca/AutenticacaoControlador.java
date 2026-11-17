package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.AvaliacaoProfessorServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.ArmarioServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.execucaodeexercicio.ExecucaoDeExercicioServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.ReservaArmarioServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.mensalidade.MensalidadeServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.vinculopersonal.VinculoPersonalAlunoServico;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.ui.Backend;
import dev.sauloaraujo.d5.framework.ui.seguranca.AutenticacaoControladorAbstrato;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping(Backend.PREFIXO+"autenticacao")
public class AutenticacaoControlador extends AutenticacaoControladorAbstrato {
    private final br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.GrupoTreinoServico grupos;
    private final ArmarioServico armarios;
    private final ReservaArmarioServico reservas;
    private final AlunoServico alunos;
    private final PlanoDeTreinoServico planos;
    private final ExecucaoDeExercicioServico execucoes;
    private final AvaliacaoFisicaServico avaliacoes;
    private final MetaServico metas;
    private final ProfessorServico professores;
    private final AvaliacaoProfessorServico avaliacoesProfessor;
    private final PlanoServico planosAcademia;
    private final AdesaoPlanoServico adesoes;
    private final MensalidadeServico mensalidades;
    private final ProfissionalServico profissionais;
    private final VinculoPersonalAlunoServico vinculos;
    public AutenticacaoControlador(ISegurancaServico segurancaServico, ArmarioServico armarios, ReservaArmarioServico reservas,
            AlunoServico alunos, PlanoDeTreinoServico planos, ExecucaoDeExercicioServico execucoes,
            AvaliacaoFisicaServico avaliacoes, MetaServico metas,
            ProfessorServico professores, AvaliacaoProfessorServico avaliacoesProfessor,
            br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.GrupoTreinoServico grupos,
            PlanoServico planosAcademia, AdesaoPlanoServico adesoes, MensalidadeServico mensalidades,
            ProfissionalServico profissionais, VinculoPersonalAlunoServico vinculos) {
        super(segurancaServico); this.grupos=grupos; this.armarios=armarios; this.reservas=reservas;
        this.alunos=alunos; this.planos=planos; this.execucoes=execucoes; this.avaliacoes=avaliacoes; this.metas=metas;
        this.professores=professores; this.avaliacoesProfessor=avaliacoesProfessor;
        this.planosAcademia=planosAcademia; this.adesoes=adesoes; this.mensalidades=mensalidades;
        this.profissionais=profissionais; this.vinculos=vinculos;
    }
    @Override protected AutenticacaoRecurso recurso() {
        return new AutenticacaoRecurso(segurancaServico.getUsuario(),armarios.criarAutorizacoes(Optional.empty()),reservas.criarAutorizacoes(Optional.empty()),
                alunos.criarAutorizacoes(Optional.empty()),planos.criarAutorizacoes(Optional.empty()),execucoes.criarAutorizacoes(Optional.empty()),
                avaliacoes.criarAutorizacoes(Optional.empty()),metas.criarAutorizacoes(Optional.empty()),
                professores.criarAutorizacoes(Optional.empty()),avaliacoesProfessor.criarAutorizacoes(Optional.empty()),
                grupos.criarAutorizacoes(Optional.empty()),
                planosAcademia.criarAutorizacoes(Optional.empty()),adesoes.criarAutorizacoes(Optional.empty()),
                mensalidades.criarAutorizacoes(Optional.empty()),profissionais.criarAutorizacoes(Optional.empty()),
                vinculos.criarAutorizacoes(Optional.empty()));
    }
}
