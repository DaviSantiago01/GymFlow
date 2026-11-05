package com.example.exemplo.dominio.crm.cliente;

import org.apache.commons.lang3.Validate;

import dev.sauloaraujo.d5.framework.dominio.IIdentificador;
import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;

public final class ClienteIdentificador extends ObjetoValorStringAbstrato implements IIdentificador {
	public ClienteIdentificador(String valor) {
		super(Validate.notBlank(valor, "Identificador do cliente em branco"));
	}
}
