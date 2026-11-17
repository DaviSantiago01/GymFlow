package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.academia;

import java.util.*;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
public record Academia(Optional<AcademiaIdentificador> identificador,String nome) implements IAgregado<AcademiaIdentificador> {
    public Academia { Objects.requireNonNull(identificador); org.apache.commons.lang3.Validate.notBlank(nome,"Nome obrigatório"); }
    public Optional<AcademiaIdentificador> getIdentificador(){return identificador;}
}
