package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.registroequipamento;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/registro-equipamento")
public class RegistroEquipamentoControlador extends AgregadoControladorAbstrato<RegistroEquipamentoIdentificador, RegistroEquipamento,
        OperacaoEquipamentosAutorizacoes<RegistroEquipamento>, RegistroEquipamentoProjecao, RegistroEquipamentoConsulta> {
    private final RegistroEquipamentoServico servico;
    private final RegistroEquipamentoAplicacaoServico consultas;

    public RegistroEquipamentoControlador(RegistroEquipamentoServico servico, RegistroEquipamentoAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @GetMapping("{identificador}/visualizacao")
    public RegistroEquipamentoProjecao visualizacao(@PathVariable RegistroEquipamentoIdentificador identificador) {
        return consultas.projetar(identificador);
    }

    public record Movimento(@NotNull AreaEquipamento area, @NotNull SituacaoEquipamento situacao,
            @Nullable String observacao) { }

    public record Inspecao(@NotNull LocalDate data, @NotNull ResultadoInspecao resultado,
            @NotNull AreaEquipamento area, @NotNull SituacaoEquipamento situacao, @Nullable String observacao) { }

    @PostMapping("{equipamento}/movimentar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void movimentar(@PathVariable EquipamentoIdentificador equipamento, @Valid @RequestBody Movimento requisicao) {
        servico.movimentar(equipamento, requisicao.area(), requisicao.situacao(), Optional.ofNullable(requisicao.observacao()));
    }

    @PostMapping("{equipamento}/inspecionar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inspecionar(@PathVariable EquipamentoIdentificador equipamento, @Valid @RequestBody Inspecao requisicao) {
        servico.inspecionar(equipamento, requisicao.data(), requisicao.resultado(), requisicao.area(), requisicao.situacao(),
                Optional.ofNullable(requisicao.observacao()));
    }
}
