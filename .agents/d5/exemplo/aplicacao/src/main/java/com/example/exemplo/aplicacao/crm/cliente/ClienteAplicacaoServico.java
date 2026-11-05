package com.example.exemplo.aplicacao.crm.cliente;

import com.example.exemplo.aplicacao.crm.cliente.spi.IClienteAplicacaoRepositorio;
import com.example.exemplo.dominio.crm.cliente.Cliente;
import com.example.exemplo.dominio.crm.cliente.ClienteIdentificador;
import com.example.exemplo.dominio.crm.cliente.ClienteServico;

import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;

public final class ClienteAplicacaoServico extends
		AgregadoAplicacaoServicoAbstrato<ClienteIdentificador, Cliente, AgregadoAutorizacoes<Cliente>, ClienteProjecao, ClienteConsulta> {
	public ClienteAplicacaoServico(IClienteAplicacaoRepositorio repositorio, ClienteServico servico) {
		super(repositorio, servico);
	}
}
