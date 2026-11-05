package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario;

import java.time.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.PeriodoReserva;
import org.apache.commons.lang3.Validate;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.RespostaTemporal;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.spi.IArmarioAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.*;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
public final class ArmarioAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<ArmarioCodigo,Armario,AgregadoAutorizacoes<Armario>,ArmarioProjecao,ArmarioConsulta> {
    private final IArmarioAplicacaoRepositorio repositorio;
    private final ArmarioServico servico;
    private final Clock relogio;
    private final ISegurancaServico seguranca;
    public ArmarioAplicacaoServico(IArmarioAplicacaoRepositorio repositorio, ArmarioServico servico, Clock relogio, ISegurancaServico seguranca) {
        super(repositorio,servico); this.repositorio=Objects.requireNonNull(repositorio);
        this.servico=Objects.requireNonNull(servico); this.relogio=Objects.requireNonNull(relogio); this.seguranca=Objects.requireNonNull(seguranca);
    }
    
    public RespostaTemporal<ArmarioProjecao> detalhe(ArmarioCodigo codigo) {
        servico.buscar(codigo);
        var agora=relogio.instant();
        return new RespostaTemporal<>(repositorio.detalhe(codigo,agora),agora);
    }

    public RespostaTemporal<AgendaArmario> horarios(ArmarioCodigo codigo, Optional<LocalDate> dataEscolhida) {
        servico.buscar(codigo);
        Objects.requireNonNull(dataEscolhida);
        var agora = relogio.instant();
        var hoje = agora.atZone(PeriodoReserva.FUSO).toLocalDate();
        var data = dataEscolhida.orElse(hoje);
        Validate.isTrue(!data.isBefore(hoje) && data.getYear() <= 9999, "Escolha uma data a partir de hoje");
        var inicioDia = data.atTime(PeriodoReserva.ABERTURA,0).atZone(PeriodoReserva.FUSO).toInstant();
        var fimDia = data.atTime(PeriodoReserva.FECHAMENTO,0).atZone(PeriodoReserva.FUSO).toInstant();
        var ocupados = repositorio.periodosOcupados(codigo,inicioDia,fimDia);
        var horarios = new ArrayList<HorarioDisponivel>();
        for (int hora = PeriodoReserva.ABERTURA; hora < PeriodoReserva.FECHAMENTO; hora++) {
            var bloco = PeriodoReserva.criar(data,hora,1);
            var duracoes = new ArrayList<Integer>();
            if (!bloco.inicio().isBefore(agora)) {
                for (int duracao=1; duracao<=3 && hora+duracao<=PeriodoReserva.FECHAMENTO; duracao++) {
                    var periodo = PeriodoReserva.criar(data,hora,duracao);
                    if (ocupados.stream().anyMatch(o -> o.inicio().isBefore(periodo.termino()) && o.termino().isAfter(periodo.inicio()))) break;
                    duracoes.add(duracao);
                }
            }
            horarios.add(new HorarioDisponivel(hora,bloco.inicio(),bloco.termino(),!duracoes.isEmpty(),List.copyOf(duracoes)));
        }
        return new RespostaTemporal<>(new AgendaArmario(data,hoje,PeriodoReserva.FUSO.getId(),List.copyOf(horarios)),agora);
    }
}

