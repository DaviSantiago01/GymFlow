package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao;

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

public class OrdemManutencaoStepDefinitions {
    private AmbienteEquipamentos a;

    @Dado("a gestão de ordens de manutenção disponível para um administrador")
    public void preparar() {
        a = new AmbienteEquipamentos();
    }

    @Então("o sistema só abre ordem para problema aberto e sem outra ordem ativa")
    public void regra1() {
        assertEquals(SituacaoOrdem.ABERTA, a.manutencao.abrir(a.problemaId, a.dadosOrdem()).situacao());
        when(a.ordens.existeAtiva(a.problemaId)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> a.manutencao.abrir(a.problemaId, a.dadosOrdem()));
        when(a.ordens.existeAtiva(a.problemaId)).thenReturn(false);
        when(a.problemas.buscarPorIdentificador(a.problemaId)).thenReturn(a.problema().cancelar(a.hoje, "Duplicado"));
        assertThrows(IllegalStateException.class, () -> a.manutencao.abrir(a.problemaId, a.dadosOrdem()));
    }

    @Então("o sistema valida data técnico e diagnóstico para iniciar a manutenção")
    public void regra2() {
        assertThrows(IllegalArgumentException.class, () -> new DadosOrdem("", "Reparo", Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new DadosOrdem("Ana", "", Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> a.manutencao.iniciar(a.ordemId, a.hoje.minusDays(1)));
        a.manutencao.iniciar(a.ordemId, a.hoje);
        verify(a.equipamentos).alterar(argThat(e -> e.situacao() == SituacaoEquipamento.EM_MANUTENCAO));
        verify(a.ordens).alterar(argThat(o -> o.situacao() == SituacaoOrdem.EM_ANDAMENTO));
    }

    @Então("o sistema conclui com serviço válido resolve o problema e mantém a situação do equipamento")
    public void regra3() {
        when(a.ordens.buscarPorIdentificador(a.ordemId)).thenReturn(a.ordem().iniciar(a.hoje, a.hoje));
        assertThrows(IllegalArgumentException.class, () -> a.manutencao.concluir(a.ordemId, a.hoje, "", Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> a.manutencao.concluir(a.ordemId, a.hoje.minusDays(1), "Reparo", Optional.empty()));
        a.manutencao.concluir(a.ordemId, a.hoje, "Rolamento substituído", Optional.empty());
        verify(a.ordens).alterar(argThat(o -> o.situacao() == SituacaoOrdem.CONCLUIDA));
        verify(a.problemas).alterar(argThat(p -> p.situacao() == SituacaoProblema.RESOLVIDO));
        verify(a.equipamentos, never()).alterar(any());
    }

    @Então("o sistema exige justificativa no cancelamento e mantém o problema aberto")
    public void regra4() {
        assertThrows(IllegalArgumentException.class, () -> a.manutencao.cancelar(a.ordemId, ""));
        a.manutencao.cancelar(a.ordemId, "Serviço reagendado");
        verify(a.ordens).alterar(argThat(o -> o.situacao() == SituacaoOrdem.CANCELADA));
        verify(a.problemas, never()).alterar(any());
    }

    @Então("o sistema rejeita custos negativos ou com mais de duas casas decimais")
    public void regra5() {
        assertThrows(IllegalArgumentException.class, () -> new DadosOrdem("Ana", "Reparo", Optional.of(new BigDecimal("-1"))));
        assertThrows(IllegalArgumentException.class, () -> DadosOrdem.validarCusto(Optional.of(new BigDecimal("1.001"))));
        assertEquals(Optional.of(new BigDecimal("20.00")), DadosOrdem.validarCusto(Optional.of(new BigDecimal("20"))));
    }

}
