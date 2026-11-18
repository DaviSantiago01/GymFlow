package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.*;
import dev.sauloaraujo.d5.framework.dominio.AgregadoExistenteExcecao;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegistroEquipamentoStepDefinitions {
    private AmbienteEquipamentos a;

    @Dado("a gestão de situação e inspeções disponível para um administrador")
    public void preparar() {
        a = new AmbienteEquipamentos();
    }

    @Então("o sistema preserva área anterior e usuário ao movimentar um equipamento ativo")
    public void regra1() {
        a.inspecoes.movimentar(a.equipamentoId, AreaEquipamento.FUNCIONAL, SituacaoEquipamento.DISPONIVEL, Optional.empty());
        verify(a.registros).incluir(argThat(r -> r.areaAnterior() == AreaEquipamento.CARDIO && r.areaNova() == AreaEquipamento.FUNCIONAL
                && r.data().equals(a.hoje) && r.responsavel().equals("gestor")));
        when(a.equipamentos.bloquear(a.equipamentoId)).thenReturn(a.equipamento(SituacaoEquipamento.INATIVO));
        assertThrows(IllegalStateException.class, () -> a.inspecoes.movimentar(a.equipamentoId, AreaEquipamento.FUNCIONAL,
                SituacaoEquipamento.INTERDITADO, Optional.empty()));
    }

    @Então("o sistema calcula prazo pela aquisição e depois pela última inspeção")
    public void regra2() {
        var atual = a.equipamento(SituacaoEquipamento.DISPONIVEL);
        assertEquals(a.hoje.plusDays(5), atual.proximaInspecao());
        assertEquals("PROXIMA", atual.prazoInspecao(a.hoje));
        assertEquals("ATRASADA", atual.prazoInspecao(a.hoje.plusDays(6)));
        var inspecionado = atual.inspecionar(atual.area(), atual.situacao(), a.hoje);
        assertEquals(a.hoje.plusDays(30), inspecionado.proximaInspecao());
        assertEquals("EM_DIA", inspecionado.prazoInspecao(a.hoje));
    }

    @Então("o sistema rejeita data retroativa e interdita quando a inspeção é reprovada")
    public void regra3() {
        var atual = a.equipamento(SituacaoEquipamento.DISPONIVEL);
        when(a.equipamentos.bloquear(a.equipamentoId)).thenReturn(atual.inspecionar(atual.area(), atual.situacao(), a.hoje));
        assertThrows(IllegalArgumentException.class, () -> inspecionar(ResultadoInspecao.APROVADO, a.hoje.minusDays(1)));
        inspecionar(ResultadoInspecao.REPROVADO, a.hoje);
        verify(a.equipamentos).alterar(argThat(e -> e.situacao() == SituacaoEquipamento.INTERDITADO));
    }

    @Então("o sistema exige inspeção aprovada na liberação e bloqueia pendências de segurança")
    public void regra4() {
        when(a.equipamentos.bloquear(a.equipamentoId)).thenReturn(a.equipamento(SituacaoEquipamento.INTERDITADO));
        assertThrows(IllegalStateException.class, () -> a.inspecoes.movimentar(a.equipamentoId, AreaEquipamento.CARDIO,
                SituacaoEquipamento.DISPONIVEL, Optional.empty()));
        when(a.ordens.existeAtivaNoEquipamento(a.equipamentoId)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> inspecionar(ResultadoInspecao.APROVADO, a.hoje));
        when(a.ordens.existeAtivaNoEquipamento(a.equipamentoId)).thenReturn(false);
        when(a.problemas.existeRiscoAberto(a.equipamentoId)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> inspecionar(ResultadoInspecao.APROVADO, a.hoje));
        when(a.problemas.existeRiscoAberto(a.equipamentoId)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> inspecionar(ResultadoInspecao.APROVADO, a.hoje.minusDays(1)));
        inspecionar(ResultadoInspecao.APROVADO, a.hoje);
        verify(a.equipamentos).alterar(argThat(e -> e.situacao() == SituacaoEquipamento.DISPONIVEL));
    }

    private void inspecionar(ResultadoInspecao resultado, LocalDate data) {
        a.inspecoes.inspecionar(a.equipamentoId, data, resultado, AreaEquipamento.CARDIO,
                SituacaoEquipamento.DISPONIVEL, Optional.empty());
    }
}
