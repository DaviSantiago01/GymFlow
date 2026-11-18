
package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.spi.IEquipamentoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.spi.IOrdemManutencaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.spi.IProblemaEquipamentoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.spi.IRegistroEquipamentoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

import static org.mockito.Mockito.*;

public final class AmbienteEquipamentos {
    public final LocalDate hoje = LocalDate.of(2026, 11, 17);
    public final EquipamentoIdentificador equipamentoId = new EquipamentoIdentificador("1");
    public final ProblemaEquipamentoIdentificador problemaId = new ProblemaEquipamentoIdentificador("2");
    public final OrdemManutencaoIdentificador ordemId = new OrdemManutencaoIdentificador("3");
    public final ISegurancaServico seguranca = mock(ISegurancaServico.class);
    public final IEquipamentoRepositorio equipamentos = mock(IEquipamentoRepositorio.class);
    public final IRegistroEquipamentoRepositorio registros = mock(IRegistroEquipamentoRepositorio.class);
    public final IProblemaEquipamentoRepositorio problemas = mock(IProblemaEquipamentoRepositorio.class);
    public final IOrdemManutencaoRepositorio ordens = mock(IOrdemManutencaoRepositorio.class);
    public final EquipamentoServico cadastro;
    public final RegistroEquipamentoServico inspecoes;
    public final ProblemaEquipamentoServico ocorrencias;
    public final OrdemManutencaoServico manutencao;

    public AmbienteEquipamentos() {
        var relogio = Clock.fixed(hoje.atStartOfDay(Equipamento.FUSO).toInstant(), ZoneOffset.UTC);
        when(seguranca.getUsuario()).thenReturn("gestor");
        when(seguranca.possuiAutorizacao("ADMINISTRADOR")).thenReturn(true);
        cadastro = new EquipamentoServico(seguranca, equipamentos, registros, problemas, relogio);
        inspecoes = new RegistroEquipamentoServico(seguranca, registros, cadastro, equipamentos, problemas, ordens);
        ocorrencias = new ProblemaEquipamentoServico(seguranca, problemas, equipamentos, ordens, cadastro);
        manutencao = new OrdemManutencaoServico(seguranca, ordens, problemas, equipamentos, cadastro);
        when(equipamentos.bloquear(equipamentoId)).thenReturn(equipamento(SituacaoEquipamento.DISPONIVEL));
        when(equipamentos.existePorIdentificador(equipamentoId)).thenReturn(true);
        when(equipamentos.incluir(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        when(problemas.identificarEquipamento(problemaId)).thenReturn(equipamentoId);
        when(problemas.buscarPorIdentificador(problemaId)).thenReturn(problema());
        when(ordens.identificarProblema(ordemId)).thenReturn(problemaId);
        when(ordens.buscarPorIdentificador(ordemId)).thenReturn(ordem());
        when(ordens.incluir(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    public DadosEquipamento dados(LocalDate aquisicao) {
        return new DadosEquipamento("eq-1", Optional.of("s-1"), "Esteira", TipoEquipamento.CARDIO, "Marca", "Modelo", aquisicao, 30);
    }

    public Equipamento equipamento(SituacaoEquipamento situacao) {
        return new Equipamento(Optional.of(equipamentoId), dados(hoje.minusDays(25)), AreaEquipamento.CARDIO,
                situacao, Optional.empty());
    }

    public DadosProblema dadosProblema(GravidadeProblema gravidade, LocalDate data) {
        return new DadosProblema("Ruído", "Ruído durante o uso", gravidade, false, Optional.empty(), data);
    }

    public ProblemaEquipamento problema() {
        return new ProblemaEquipamento(Optional.of(problemaId), equipamentoId, dadosProblema(GravidadeProblema.BAIXA, hoje),
                "gestor", SituacaoProblema.ABERTO, Optional.empty(), Optional.empty());
    }

    public DadosOrdem dadosOrdem() {
        return new DadosOrdem("Técnica Ana", "Avaliar rolamento", Optional.empty());
    }

    public OrdemManutencao ordem() {
        return OrdemManutencao.abrir(problemaId, dadosOrdem(), hoje, "gestor");
    }
}
