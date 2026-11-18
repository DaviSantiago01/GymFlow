package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento;

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

public class ProblemaEquipamentoStepDefinitions {
    private AmbienteEquipamentos a;

    @Dado("a gestão de problemas disponível para um administrador")
    public void preparar() {
        a = new AmbienteEquipamentos();
    }

    @Então("o sistema rejeita problema de equipamento inativo ou com data inválida")
    public void regra1() {
        assertThrows(IllegalArgumentException.class, () -> a.ocorrencias.registrar(a.equipamentoId,
                a.dadosProblema(GravidadeProblema.BAIXA, a.hoje.plusDays(1))));
        when(a.equipamentos.bloquear(a.equipamentoId)).thenReturn(a.equipamento(SituacaoEquipamento.INATIVO));
        assertThrows(IllegalStateException.class, () -> a.ocorrencias.registrar(a.equipamentoId, a.problema().dados()));
    }

    @Então("o sistema interdita o equipamento ao alterar um problema para crítico")
    public void regra2() {
        a.ocorrencias.editar(a.problemaId, a.dadosProblema(GravidadeProblema.CRITICA, a.hoje));
        verify(a.equipamentos).alterar(argThat(e -> e.situacao() == SituacaoEquipamento.INTERDITADO));
        verify(a.registros).incluir(argThat(r -> r.situacaoNova() == SituacaoEquipamento.INTERDITADO));
    }

    @Então("o sistema exige solução ou justificativa e não permite editar problema encerrado")
    public void regra3() {
        assertThrows(IllegalArgumentException.class, () -> a.ocorrencias.resolver(a.problemaId, a.hoje, ""));
        assertThrows(IllegalArgumentException.class, () -> a.ocorrencias.cancelar(a.problemaId, ""));
        var resolvido = a.problema().resolver(a.hoje, "Ajuste concluído", a.hoje);
        when(a.problemas.buscarPorIdentificador(a.problemaId)).thenReturn(resolvido);
        assertThrows(IllegalStateException.class, () -> a.ocorrencias.editar(a.problemaId, a.problema().dados()));
    }

    @Então("o sistema bloqueia encerramento direto enquanto existe ordem ativa")
    public void regra4() {
        when(a.ordens.existeAtiva(a.problemaId)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> a.ocorrencias.resolver(a.problemaId, a.hoje, "Reparo"));
        assertThrows(IllegalStateException.class, () -> a.ocorrencias.cancelar(a.problemaId, "Duplicado"));
        verify(a.equipamentos, never()).alterar(any());
    }

}
