package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.armario;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.ReservaArmarioServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.reserva.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.RespostaTemporal;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping(Backend.PREFIXO+"core/armario")
public class ArmarioControlador extends AgregadoControladorAbstrato<ArmarioCodigo,Armario,AgregadoAutorizacoes<Armario>,ArmarioProjecao,ArmarioConsulta> {
    private final ArmarioAplicacaoServico consultas;
    private final ReservaArmarioServico reservas;
    private final ReservaArmarioAplicacaoServico consultasReservas;
    public ArmarioControlador(ArmarioServico servico, ArmarioAplicacaoServico consultas, ReservaArmarioServico reservas, ReservaArmarioAplicacaoServico consultasReservas) {
        super(servico,consultas); this.consultas=consultas; this.reservas=reservas; this.consultasReservas=consultasReservas;
    }
    @GetMapping("{identificador}/visualizacao")
    public RespostaTemporal<ArmarioProjecao> visualizacao(@PathVariable ArmarioCodigo identificador) { return consultas.detalhe(identificador); }
    @GetMapping("{identificador}/horarios")
    public RespostaTemporal<AgendaArmario> horarios(@PathVariable ArmarioCodigo identificador,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.util.Optional<java.time.LocalDate> data) {
        return consultas.horarios(identificador,data);
    }
    @PostMapping("{identificador}/reservar")
    public RespostaTemporal<ReservaArmarioProjecao> reservar(@PathVariable ArmarioCodigo identificador, @jakarta.validation.Valid @RequestBody ReservaRequisicao requisicao) {
        var reserva=reservas.reservar(identificador,requisicao.data(),requisicao.hora().intValueExact(),requisicao.duracaoHoras().intValueExact());
        return consultasReservas.detalhe(reserva.getIdentificador().orElseThrow());
    }
}
