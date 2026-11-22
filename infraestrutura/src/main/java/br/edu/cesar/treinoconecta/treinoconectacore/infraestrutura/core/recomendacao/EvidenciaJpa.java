package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.recomendacao;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class EvidenciaJpa {
    @NotNull @Column(name="REV_ROTULO") private String rotulo;
    @NotNull @Column(name="REV_VALOR") private BigDecimal valor;
    @Column(name="REV_DESTAQUE") private boolean destaque;

    protected EvidenciaJpa() { }

    public EvidenciaJpa(String rotulo, BigDecimal valor, boolean destaque) {
        this.rotulo = rotulo;
        this.valor = valor;
        this.destaque = destaque;
    }

    public String getRotulo() { return rotulo; }
    public BigDecimal getValor() { return valor; }
    public boolean isDestaque() { return destaque; }
}
