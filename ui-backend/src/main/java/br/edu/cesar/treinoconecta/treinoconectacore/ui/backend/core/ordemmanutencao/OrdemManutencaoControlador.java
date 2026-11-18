package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.ordemmanutencao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.ProblemaEquipamentoIdentificador;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/ordem-manutencao")
public class OrdemManutencaoControlador extends AgregadoControladorAbstrato<OrdemManutencaoIdentificador, OrdemManutencao,
        OperacaoEquipamentosAutorizacoes<OrdemManutencao>, OrdemManutencaoProjecao, OrdemManutencaoConsulta> {
    private final OrdemManutencaoServico servico;
    private final OrdemManutencaoAplicacaoServico consultas;

    public OrdemManutencaoControlador(OrdemManutencaoServico servico, OrdemManutencaoAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @GetMapping("{identificador}/visualizacao")
    public OrdemManutencaoProjecao visualizacao(@PathVariable OrdemManutencaoIdentificador identificador) {
        return consultas.projetar(identificador);
    }

    public record Manutencao(@Nullable String problema, @NotBlank String tecnico, @NotBlank String diagnostico,
            @Nullable BigDecimal custoEstimado) {
        DadosOrdem dados() {
            return new DadosOrdem(tecnico, diagnostico, Optional.ofNullable(custoEstimado));
        }
    }

    public record Inicio(@NotNull LocalDate data) { }

    public record Conclusao(@NotNull LocalDate data, @NotBlank String servicoExecutado,
            @Nullable BigDecimal custoReal) { }

    public record Cancelamento(@NotBlank String justificativa) { }

    @PostMapping("abrir")
    public OrdemManutencaoProjecao abrir(@Valid @RequestBody Manutencao requisicao) {
        var problema = requisicao.problema();
        if (problema == null) {
            throw new IllegalArgumentException("Informe o problema");
        }
        var ordem = servico.abrir(new ProblemaEquipamentoIdentificador(problema), requisicao.dados());
        return consultas.projetar(ordem.identificador().orElseThrow());
    }

    @PostMapping("{identificador}/editar")
    public OrdemManutencaoProjecao editar(@PathVariable OrdemManutencaoIdentificador identificador,
            @Valid @RequestBody Manutencao requisicao) {
        servico.editar(identificador, requisicao.dados());
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/iniciar")
    public OrdemManutencaoProjecao iniciar(@PathVariable OrdemManutencaoIdentificador identificador,
            @Valid @RequestBody Inicio requisicao) {
        servico.iniciar(identificador, requisicao.data());
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/concluir")
    public OrdemManutencaoProjecao concluir(@PathVariable OrdemManutencaoIdentificador identificador,
            @Valid @RequestBody Conclusao requisicao) {
        servico.concluir(identificador, requisicao.data(), requisicao.servicoExecutado(), Optional.ofNullable(requisicao.custoReal()));
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/cancelar")
    public OrdemManutencaoProjecao cancelar(@PathVariable OrdemManutencaoIdentificador identificador,
            @Valid @RequestBody Cancelamento requisicao) {
        servico.cancelar(identificador, requisicao.justificativa());
        return consultas.projetar(identificador);
    }
}
