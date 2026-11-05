package com.example.exemplo.aplicacao.crm.cliente.spi;

import com.example.exemplo.aplicacao.crm.cliente.ClienteConsulta;
import com.example.exemplo.aplicacao.crm.cliente.ClienteProjecao;

import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;

public interface IClienteAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<ClienteProjecao, ClienteConsulta> {
}
