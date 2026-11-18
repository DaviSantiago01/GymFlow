package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.ordemmanutencao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento.*;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public record OrdemManutencaoProjecao(OrdemManutencaoIdentificador identificador,
        ProblemaEquipamentoIdentificador problema,
        String tecnico,
        String diagnostico,
        LocalDate abertura,
        String responsavel,
        SituacaoOrdem situacao,
        Optional<LocalDate> inicio,
        Optional<LocalDate> termino,
        Optional<BigDecimal> custoEstimado,
        Optional<BigDecimal> custoReal,
        Optional<String> servicoExecutado,
        Optional<String> justificativa,
        EquipamentoIdentificador equipamento,
        String problemaTitulo)
        implements IProjecao<OrdemManutencao> {
    public OrdemManutencaoProjecao(Long identificador,
            Long problema,
            String tecnico,
            String diagnostico,
            LocalDate abertura,
            String responsavel,
            SituacaoOrdem situacao,
            @Nullable LocalDate inicio,
            @Nullable LocalDate termino,
            @Nullable BigDecimal custoEstimado,
            @Nullable BigDecimal custoReal,
            @Nullable String servicoExecutado,
            @Nullable String justificativa,
            Long equipamento,
            String problemaTitulo) {
        this(new OrdemManutencaoIdentificador(identificador.toString()),
                new ProblemaEquipamentoIdentificador(problema.toString()),
                tecnico,
                diagnostico,
                abertura,
                responsavel,
                situacao,
                Optional.ofNullable(inicio),
                Optional.ofNullable(termino),
                Optional.ofNullable(custoEstimado),
                Optional.ofNullable(custoReal),
                Optional.ofNullable(servicoExecutado),
                Optional.ofNullable(justificativa),
                new EquipamentoIdentificador(equipamento.toString()),
                problemaTitulo);
    }

}
