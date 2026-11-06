package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.grupotreino;

import java.util.Objects;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;
public final class NomeGrupo extends ObjetoValorStringAbstrato {
    public static final int TAMANHO=60;
    public NomeGrupo(String valor){super(Objects.requireNonNull(valor,"Nome obrigatório").strip());Validate.isTrue(!toString().isBlank() && toString().length()<=TAMANHO,"Informe um nome de 1 a 60 caracteres");}
}
