package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento;

import java.util.Optional;

import dev.sauloaraujo.d5.framework.dominio.AgregadoExistenteExcecao;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EquipamentoStepDefinitions {
    private AmbienteEquipamentos a;

    @Dado("a gestão de cadastro e gestão disponível para um administrador")
    public void preparar() {
        a = new AmbienteEquipamentos();
    }

    @Então("o sistema valida os dados obrigatórios e o cadastro começa disponível")
    public void regra1() {
        var criado = a.cadastro.cadastrar(a.dados(a.hoje), AreaEquipamento.CARDIO);
        assertEquals(SituacaoEquipamento.DISPONIVEL, criado.situacao());
        assertEquals("EQ-1", criado.dados().patrimonio());
        assertThrows(IllegalArgumentException.class, () -> a.cadastro.cadastrar(a.dados(a.hoje.plusDays(1)), AreaEquipamento.CARDIO));
        assertThrows(IllegalArgumentException.class, () -> new DadosEquipamento("EQ", Optional.empty(), "Esteira",
                TipoEquipamento.CARDIO, "Marca", "Modelo", a.hoje, 0));
    }

    @Então("o sistema rejeita patrimônio e série duplicados e permite manter os valores do próprio equipamento")
    public void regra2() {
        when(a.equipamentos.existePatrimonio("EQ-1", Optional.empty())).thenReturn(true);
        assertThrows(AgregadoExistenteExcecao.class, () -> a.cadastro.cadastrar(a.dados(a.hoje), AreaEquipamento.CARDIO));
        when(a.equipamentos.existePatrimonio("EQ-1", Optional.empty())).thenReturn(false);
        when(a.equipamentos.existeSerie("S-1", Optional.empty())).thenReturn(true);
        assertThrows(AgregadoExistenteExcecao.class, () -> a.cadastro.cadastrar(a.dados(a.hoje), AreaEquipamento.CARDIO));
        a.cadastro.editar(a.equipamentoId, a.dados(a.hoje.minusDays(25)));
        verify(a.equipamentos).existeSerie("S-1", Optional.of(a.equipamentoId));
    }

    @Então("o sistema preserva área e situação durante a edição e rejeita edição de inativos")
    public void regra3() {
        when(a.equipamentos.bloquear(a.equipamentoId)).thenReturn(a.equipamento(SituacaoEquipamento.INTERDITADO));
        a.cadastro.editar(a.equipamentoId, a.dados(a.hoje.minusDays(25)));
        verify(a.equipamentos).alterar(argThat(e -> e.area() == AreaEquipamento.CARDIO && e.situacao() == SituacaoEquipamento.INTERDITADO));
        when(a.equipamentos.bloquear(a.equipamentoId)).thenReturn(a.equipamento(SituacaoEquipamento.INATIVO));
        assertThrows(IllegalStateException.class, () -> a.cadastro.editar(a.equipamentoId, a.dados(a.hoje.minusDays(25))));
    }

    @Então("o sistema exclui sem histórico e preserva registros ao inativar")
    public void regra4() {
        a.cadastro.excluir(a.equipamentoId);
        verify(a.equipamentos).excluir(a.equipamentoId);
        when(a.registros.existePorEquipamento(a.equipamentoId)).thenReturn(true);
        var bloqueio = assertThrows(IllegalStateException.class, () -> a.cadastro.excluir(a.equipamentoId));
        assertEquals("O equipamento possui histórico. Inative o cadastro.", bloqueio.getMessage());
        when(a.problemas.existeAberto(a.equipamentoId)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> a.cadastro.inativar(a.equipamentoId));
        when(a.problemas.existeAberto(a.equipamentoId)).thenReturn(false);
        a.cadastro.inativar(a.equipamentoId);
        verify(a.registros).incluir(argThat(r -> r.situacaoNova() == SituacaoEquipamento.INATIVO));
    }

}
