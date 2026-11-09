package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica;

import java.math.BigDecimal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class MedidaCorporalJpa {
    @NotNull @Enumerated(EnumType.STRING) @Column(name="MDC_INDICADOR") private IndicadorFisico indicador;
    @NotNull @Column(name="MDC_VALOR") private BigDecimal valor;

    protected MedidaCorporalJpa() { }

    public MedidaCorporalJpa(IndicadorFisico indicador, BigDecimal valor) {
        this.indicador = indicador;
        this.valor = valor;
    }

    public IndicadorFisico getIndicador() { return indicador; }
    public BigDecimal getValor() { return valor; }
}
