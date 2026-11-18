package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.equipamento;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/equipamento")
public class EquipamentoControlador extends AgregadoControladorAbstrato<EquipamentoIdentificador, Equipamento,
        OperacaoEquipamentosAutorizacoes<Equipamento>, EquipamentoProjecao, EquipamentoConsulta> {
    private final EquipamentoServico servico;
    private final EquipamentoAplicacaoServico consultas;

    public EquipamentoControlador(EquipamentoServico servico, EquipamentoAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @GetMapping("{identificador}/visualizacao")
    public EquipamentoProjecao visualizacao(@PathVariable EquipamentoIdentificador identificador) {
        return consultas.projetar(identificador);
    }

    @GetMapping("{identificador}/operacoes")
    public Operacoes operacoes(@PathVariable EquipamentoIdentificador identificador) {
        var equipamento = consultas.projetar(identificador);
        return new Operacoes(equipamento, !servico.temHistorico(identificador), servico.hoje());
    }

    public record Operacoes(EquipamentoProjecao equipamento, boolean podeExcluir, LocalDate hoje) { }

    public record Cadastro(@NotBlank String patrimonio, @Nullable String serie, @NotBlank String nome,
            @NotNull TipoEquipamento tipo, @NotBlank String marca, @NotBlank String modelo,
            @NotNull LocalDate aquisicao, @NotNull Integer periodicidade, @Nullable AreaEquipamento area) {
        DadosEquipamento dados() {
            return new DadosEquipamento(patrimonio, Optional.ofNullable(serie), nome, tipo, marca, modelo,
                    aquisicao, periodicidade);
        }
    }

    @PostMapping("cadastrar")
    public EquipamentoProjecao cadastrar(@Valid @RequestBody Cadastro requisicao) {
        var area = requisicao.area();
        if (area == null) {
            throw new IllegalArgumentException("Informe a área");
        }
        var equipamento = servico.cadastrar(requisicao.dados(), area);
        return consultas.projetar(equipamento.identificador().orElseThrow());
    }

    @PostMapping("{identificador}/editar")
    public EquipamentoProjecao editar(@PathVariable EquipamentoIdentificador identificador,
            @Valid @RequestBody Cadastro requisicao) {
        servico.editar(identificador, requisicao.dados());
        return consultas.projetar(identificador);
    }

    @PostMapping("{identificador}/inativar")
    public EquipamentoProjecao inativar(@PathVariable EquipamentoIdentificador identificador) {
        servico.inativar(identificador);
        return consultas.projetar(identificador);
    }
}
