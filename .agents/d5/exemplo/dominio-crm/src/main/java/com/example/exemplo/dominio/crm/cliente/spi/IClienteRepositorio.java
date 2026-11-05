package com.example.exemplo.dominio.crm.cliente.spi;

import java.util.Optional;

import com.example.exemplo.dominio.crm.cliente.Cliente;
import com.example.exemplo.dominio.crm.cliente.ClienteIdentificador;
import com.example.exemplo.dominio.crm.cliente.Cpf;

import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IClienteRepositorio extends IAgregadoRepositorio<ClienteIdentificador, Cliente> {
	boolean existePorCpf(Cpf cpf, Optional<ClienteIdentificador> identificadorIgnorado);
}
