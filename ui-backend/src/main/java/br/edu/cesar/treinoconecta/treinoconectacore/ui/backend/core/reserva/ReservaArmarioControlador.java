package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.reserva;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.reserva.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.RespostaTemporal;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping(Backend.PREFIXO+"core/reserva-armario")
public class ReservaArmarioControlador extends AgregadoControladorAbstrato<ReservaArmarioIdentificador,ReservaArmario,AgregadoAutorizacoes<ReservaArmario>,ReservaArmarioProjecao,ReservaArmarioConsulta> {
    private final ReservaArmarioServico comandos;
    private final ReservaArmarioAplicacaoServico consultas;
    public ReservaArmarioControlador(ReservaArmarioServico comandos, ReservaArmarioAplicacaoServico consultas) { super(comandos,consultas); this.comandos=comandos; this.consultas=consultas; }
    @GetMapping("acompanhamento")
    public RespostaTemporal<Optional<ReservaArmarioProjecao>> ativa() { return consultas.ativa(); }
    @GetMapping({"{identificador}/visualizacao","{identificador}/confirmacao"})
    public RespostaTemporal<ReservaArmarioProjecao> detalhe(@PathVariable ReservaArmarioIdentificador identificador) { return consultas.detalhe(identificador); }
    @PostMapping("{identificador}/cancelar")
    public RespostaTemporal<ReservaArmarioProjecao> cancelar(@PathVariable ReservaArmarioIdentificador identificador) { comandos.cancelar(identificador); return consultas.detalhe(identificador); }
    @PostMapping("{identificador}/encerrar")
    public RespostaTemporal<ReservaArmarioProjecao> encerrar(@PathVariable ReservaArmarioIdentificador identificador) { comandos.encerrar(identificador); return consultas.detalhe(identificador); }
}
