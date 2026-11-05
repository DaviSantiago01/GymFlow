package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.seguranca;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.Armario;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.ReservaArmario;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.seguranca.AutenticacaoRecursoAbstrato;
public class AutenticacaoRecurso extends AutenticacaoRecursoAbstrato {
    private final AgregadoAutorizacoes<Armario> armario;
    private final AgregadoAutorizacoes<ReservaArmario> reservaArmario;
    public AutenticacaoRecurso(String usuario, AgregadoAutorizacoes<Armario> armario, AgregadoAutorizacoes<ReservaArmario> reservaArmario) {
        super(usuario); this.armario=armario; this.reservaArmario=reservaArmario;
    }
    public AgregadoAutorizacoes<Armario> getArmario() { return armario; }
    public AgregadoAutorizacoes<ReservaArmario> getReservaArmario() { return reservaArmario; }
}
