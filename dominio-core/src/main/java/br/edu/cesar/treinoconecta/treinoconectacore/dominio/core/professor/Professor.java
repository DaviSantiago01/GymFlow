package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.professor;

import java.util.*;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;
public record Professor(Optional<ProfessorIdentificador> identificador, String nome, String especialidade,
        String biografia, boolean demonstracao) implements IAgregado<ProfessorIdentificador> {
    public Professor {
        Objects.requireNonNull(identificador); Validate.notBlank(nome,"Nome obrigatório");
        Objects.requireNonNull(especialidade); Objects.requireNonNull(biografia);
        Validate.isTrue(nome.length()<=100 && especialidade.length()<=120 && biografia.length()<=1000,"Perfil muito longo");
    }
    public Optional<ProfessorIdentificador> getIdentificador() { return identificador; }
}
