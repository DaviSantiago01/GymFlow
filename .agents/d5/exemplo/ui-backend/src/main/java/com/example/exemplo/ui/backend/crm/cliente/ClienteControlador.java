package com.example.exemplo.ui.backend.crm.cliente;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exemplo.aplicacao.crm.cliente.ClienteAplicacaoServico;
import com.example.exemplo.aplicacao.crm.cliente.ClienteConsulta;
import com.example.exemplo.aplicacao.crm.cliente.ClienteProjecao;
import com.example.exemplo.dominio.crm.cliente.Cliente;
import com.example.exemplo.dominio.crm.cliente.ClienteIdentificador;
import com.example.exemplo.dominio.crm.cliente.ClienteServico;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "crm/cliente")
public class ClienteControlador extends
		AgregadoControladorAbstrato<ClienteIdentificador, Cliente, AgregadoAutorizacoes<Cliente>, ClienteProjecao, ClienteConsulta> {
	public ClienteControlador(ClienteServico servico, ClienteAplicacaoServico servicoAplicacao) {
		super(servico, servicoAplicacao);
	}
}
