package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ordemmanutencao;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.DadosOperacionais;

public record DadosOrdem(String tecnico, String diagnostico, Optional<BigDecimal> custoEstimado)
        implements dev.sauloaraujo.d5.framework.dominio.IObjetoValor {
    public DadosOrdem {
        tecnico = DadosOperacionais.texto(tecnico, 100, "Técnico");
        diagnostico = DadosOperacionais.texto(diagnostico, 800, "Diagnóstico");
        custoEstimado = validarCusto(custoEstimado);
    }

    public static Optional<BigDecimal> validarCusto(Optional<BigDecimal> custo) {
        return Objects.requireNonNull(custo).map(valor -> {
            Validate.isTrue(valor.signum() >= 0 && valor.stripTrailingZeros().scale() <= 2
                    && valor.compareTo(new BigDecimal("9999999999.99")) <= 0, "Informe um custo válido com até duas casas decimais");
            return valor.setScale(2);
        });
    }
}
