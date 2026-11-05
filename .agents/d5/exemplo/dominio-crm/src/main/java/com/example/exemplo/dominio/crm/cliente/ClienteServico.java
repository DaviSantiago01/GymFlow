package com.example.exemplo.dominio.crm.cliente;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import com.example.exemplo.dominio.Autorizacao;
import com.example.exemplo.dominio.crm.cliente.spi.IClienteRepositorio;
import com.example.exemplo.dominio.geografia.municipio.Municipio;
import com.example.exemplo.dominio.geografia.municipio.spi.IMunicipioRepositorio;

import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoAutorizacoes;
import dev.sauloaraujo.d5.framework.dominio.impl.AgregadoServicoAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;

public final class ClienteServico
		extends AgregadoServicoAbstrato<ClienteIdentificador, Cliente, AgregadoAutorizacoes<Cliente>> {
	public static final String IDENTIFICADOR_INEXISTENTE_MODELO = "Não existe cliente com o identificador %s";
	public static final String IDENTIFICADOR_EXISTENTE_MODELO = "Já existe um cliente com o identificador %s";
	public static final String CPF_EXISTENTE_MODELO = "Já existe um cliente com o CPF %s";
	public static final String IDENTIFICADOR_INFORMADO = "O identificador do cliente é gerado na inclusão e não deve ser informado";

	private final IClienteRepositorio repositorio;
	private final IMunicipioRepositorio municipioRepositorio;

	public ClienteServico(ISegurancaServico segurancaServico, IClienteRepositorio repositorio,
			IMunicipioRepositorio municipioRepositorio) {
		super(segurancaServico, repositorio, IDENTIFICADOR_EXISTENTE_MODELO, IDENTIFICADOR_INEXISTENTE_MODELO);

		this.repositorio = Objects.requireNonNull(repositorio);
		this.municipioRepositorio = Objects.requireNonNull(municipioRepositorio);
	}

	@Override
	public AgregadoAutorizacoes<Cliente> criarAutorizacoes(Optional<ClienteIdentificador> identificador) {
		Objects.requireNonNull(identificador);

		var administrador = getSegurancaServico().possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
		return new AgregadoAutorizacoes<>(administrador, true, administrador, administrador);
	}

	@Override
	protected void validarInclusao(Cliente agregado) {
		Objects.requireNonNull(agregado);

		Validate.isTrue(agregado.getIdentificador().isEmpty(), IDENTIFICADOR_INFORMADO);
	}

	@Override
	protected void validarInclusaoAlteracao(Cliente agregado) {
		Objects.requireNonNull(agregado);

		validarInexistencia(cpf -> repositorio.existePorCpf(cpf, agregado.getIdentificador()), agregado.getCpf(),
				CPF_EXISTENTE_MODELO);

		var municipio = agregado.getEndereco().getMunicipio();
		Validate.validState(!municipioRepositorio.buscarPorIdentificador(municipio).isExtinto(),
				Municipio.EXTINTO_MODELO, municipio);
	}
}
