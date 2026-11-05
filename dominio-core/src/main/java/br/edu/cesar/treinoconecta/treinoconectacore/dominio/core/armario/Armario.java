package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

public final class Armario implements IAgregado<ArmarioCodigo> {
    private final ArmarioCodigo identificador;
    private final String localizacao;
    private final String descricao;
    public Armario(ArmarioCodigo identificador, String localizacao, String descricao) {
        this.identificador = Objects.requireNonNull(identificador);
        this.localizacao = Validate.notBlank(localizacao, "Localização obrigatória");
        this.descricao = Validate.notBlank(descricao, "Descrição obrigatória");
        Validate.isTrue(localizacao.length() <= 150 && descricao.length() <= 250, "Texto muito longo");
    }
    public Optional<ArmarioCodigo> getIdentificador() { return Optional.of(identificador); }
    public String getLocalizacao() { return localizacao; }
    public String getDescricao() { return descricao; }
}
