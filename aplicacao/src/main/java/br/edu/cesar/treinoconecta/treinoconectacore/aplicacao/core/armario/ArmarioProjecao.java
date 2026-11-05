package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.Armario;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;
public record ArmarioProjecao(String identificador, String localizacao, String descricao,
        boolean disponivel) implements IProjecao<Armario> { }
