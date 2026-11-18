package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.problemaequipamento;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/problema-equipamento")
public class ProblemaEquipamentoControlador extends AgregadoControladorAbstrato<ProblemaEquipamentoIdentificador, ProblemaEquipamento,
        OperacaoEquipamentosAutorizacoes<ProblemaEquipamento>, ProblemaEquipamentoProjecao, ProblemaEquipamentoConsulta> {
    private final ProblemaEquipamentoServico servico;
    private final ProblemaEquipamentoAplicacaoServico consultas;

    public ProblemaEquipamentoControlador(ProblemaEquipamentoServico servico, ProblemaEquipamentoAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @GetMapping("{identificador}/visualizacao")
    public ProblemaEquipamentoProjecao visualizacao(@PathVariable ProblemaEquipamentoIdentificador identificador) {
        return consultas.projetar(identificador);
    }

    public record Relato(@Nullable String equipamento, @NotBlank String titulo, @NotBlank String relato,
            @NotNull GravidadeProblema gravidade, @NotNull Boolean risco, @Nullable String evidencia, @NotNull LocalDate dataRelato) {
        DadosProblema dados() {
            return new DadosProblema(titulo, relato, gravidade, risco, Optional.ofNullable(evidencia), dataRelato);
        }
    }

    public record Resolucao(@NotNull LocalDate data, @NotBlank String solucao) { }

    public record Cancelamento(@NotBlank String justificativa) { }

    @PostMapping("registrar")
    public ProblemaEquipamentoProjecao registrar(@Valid @RequestBody Relato requisicao) {
        var equipamento = requisicao.equipamento();
        if (equipamento == null) {
            throw new IllegalArgumentException("Informe o equipamento");
        }
        var problema = servico.registrar(new EquipamentoIdentificador(equipamento), requisicao.dados());
        return consultas.projetar(problema.identificador().orElseThrow());
    }

    @PostMapping("{identificador}/editar")
    public ProblemaEquipamentoProjecao editar(@PathVariable ProblemaEquipamentoIdentificador identificador,
            @Valid @RequestBody Relato requisicao) {
        servico.editar(identificador, requisicao.dados());
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/resolver")
    public ProblemaEquipamentoProjecao resolver(@PathVariable ProblemaEquipamentoIdentificador identificador,
            @Valid @RequestBody Resolucao requisicao) {
        servico.resolver(identificador, requisicao.data(), requisicao.solucao());
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/cancelar")
    public ProblemaEquipamentoProjecao cancelar(@PathVariable ProblemaEquipamentoIdentificador identificador,
            @Valid @RequestBody Cancelamento requisicao) {
        servico.cancelar(identificador, requisicao.justificativa());
        return consultas.projetar(identificador);
    }
}
