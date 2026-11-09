package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.Personal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IPersonalRepositorio extends IAgregadoRepositorio<PersonalIdentificador, Personal> {
    Personal buscarPorUsuario(String usuario);
}
