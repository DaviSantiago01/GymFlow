package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.ArmarioServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.ReservaArmarioServico;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.ui.Backend;
import dev.sauloaraujo.d5.framework.ui.seguranca.AutenticacaoControladorAbstrato;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping(Backend.PREFIXO+"autenticacao")
public class AutenticacaoControlador extends AutenticacaoControladorAbstrato {
    private final ArmarioServico armarios;
    private final ReservaArmarioServico reservas;
    public AutenticacaoControlador(ISegurancaServico segurancaServico, ArmarioServico armarios, ReservaArmarioServico reservas) {
        super(segurancaServico); this.armarios=armarios; this.reservas=reservas;
    }
    @Override protected AutenticacaoRecurso recurso() {
        return new AutenticacaoRecurso(segurancaServico.getUsuario(),armarios.criarAutorizacoes(Optional.empty()),reservas.criarAutorizacoes(Optional.empty()));
    }
}
