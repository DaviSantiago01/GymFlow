package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor.ProfessorServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.AvaliacaoProfessorServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.ArmarioServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.ReservaArmarioServico;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.ui.Backend;
import dev.sauloaraujo.d5.framework.ui.seguranca.AutenticacaoControladorAbstrato;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping(Backend.PREFIXO+"autenticacao")
public class AutenticacaoControlador extends AutenticacaoControladorAbstrato {
    private final br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.GrupoTreinoServico grupos;
    private final ArmarioServico armarios;
    private final ReservaArmarioServico reservas;
    private final ProfessorServico professores;
    private final AvaliacaoProfessorServico avaliacoes;
    public AutenticacaoControlador(ISegurancaServico segurancaServico, ArmarioServico armarios, ReservaArmarioServico reservas, ProfessorServico professores, AvaliacaoProfessorServico avaliacoes,br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino.GrupoTreinoServico grupos) {
        super(segurancaServico); this.grupos=grupos; this.armarios=armarios; this.reservas=reservas;this.professores=professores;this.avaliacoes=avaliacoes;
    }
    @Override protected AutenticacaoRecurso recurso() {
        return new AutenticacaoRecurso(segurancaServico.getUsuario(),armarios.criarAutorizacoes(Optional.empty()),reservas.criarAutorizacoes(Optional.empty()),professores.criarAutorizacoes(Optional.empty()),avaliacoes.criarAutorizacoes(Optional.empty()),grupos.criarAutorizacoes(Optional.empty()));
    }
}
