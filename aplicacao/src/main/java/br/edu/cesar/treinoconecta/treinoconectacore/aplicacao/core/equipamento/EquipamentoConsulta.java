package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.equipamento;

import java.util.Objects;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.*;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class EquipamentoConsulta extends Consulta<EquipamentoOrdenacao> {
    private final Optional<String> termo;
    private final Optional<SituacaoEquipamento> situacao;
    private final Optional<AreaEquipamento> area;
    private final Optional<TipoEquipamento> tipo;

    public EquipamentoConsulta(Optional<EquipamentoOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina,
            Optional<String> termo, Optional<SituacaoEquipamento> situacao, Optional<AreaEquipamento> area, Optional<TipoEquipamento> tipo) {
        super(ordenacao.or(() -> Optional.of(EquipamentoOrdenacao.NOME)), descendente, indicePagina, tamanhoPagina);
        this.termo = Objects.requireNonNull(termo);
        this.situacao = Objects.requireNonNull(situacao);
        this.area = Objects.requireNonNull(area);
        this.tipo = Objects.requireNonNull(tipo);
    }

    public Optional<String> getTermo() {
        return termo;
    }

    public Optional<SituacaoEquipamento> getSituacao() {
        return situacao;
    }

    public Optional<AreaEquipamento> getArea() {
        return area;
    }

    public Optional<TipoEquipamento> getTipo() {
        return tipo;
    }
}
