package com.example.exemplo.infraestrutura.crm.cliente;

import java.util.Objects;
import java.util.Optional;

import com.example.exemplo.dominio.crm.cliente.Cliente;
import com.example.exemplo.dominio.crm.cliente.ClienteIdentificador;
import com.example.exemplo.dominio.crm.cliente.ClienteServico;
import com.example.exemplo.dominio.crm.cliente.Cpf;
import com.example.exemplo.dominio.crm.cliente.Endereco;
import com.example.exemplo.dominio.crm.cliente.EnderecoCep;
import com.example.exemplo.dominio.crm.cliente.spi.IClienteRepositorio;
import com.example.exemplo.dominio.geografia.municipio.MunicipioCodigoIbge;
import com.example.exemplo.infraestrutura.geografia.municipio.IMunicipioRepositorioJpa;

import dev.sauloaraujo.d5.framework.dominio.AgregadoInexistenteExcecao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public class ClienteRepositorio extends AgregadoRepositorioAbstrato<ClienteIdentificador, Cliente, Long, ClienteJpa>
		implements IClienteRepositorio {
	private final IClienteRepositorioJpa repositorioJpa;
	private final IMunicipioRepositorioJpa municipioRepositorioJpa;

	public ClienteRepositorio(IClienteRepositorioJpa repositorioJpa, IMunicipioRepositorioJpa municipioRepositorioJpa) {
		super(repositorioJpa, ClienteServico.IDENTIFICADOR_INEXISTENTE_MODELO);

		this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
		this.municipioRepositorioJpa = Objects.requireNonNull(municipioRepositorioJpa);
	}

	@Override
	protected ClienteJpa paraJpa(Cliente agregado) {
		Objects.requireNonNull(agregado);

		var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
		var endereco = agregado.getEndereco();
		var municipio = municipioRepositorioJpa.getReferenceById(endereco.getMunicipio().toString());
		return new ClienteJpa(identificador, agregado.getCpf().toString(), agregado.getNome(), endereco.getLogradouro(),
				endereco.getNumero(), endereco.getComplemento().orElse(null), endereco.getBairro(),
				endereco.getCep().toString(), municipio);
	}

	@Override
	protected Cliente paraAgregado(ClienteJpa agregadoJpa) {
		Objects.requireNonNull(agregadoJpa);

		var identificador = new ClienteIdentificador(Objects.requireNonNull(agregadoJpa.getIdentificador()).toString());
		var endereco = new Endereco(agregadoJpa.getLogradouro(), agregadoJpa.getNumero(),
				Optional.ofNullable(agregadoJpa.getComplemento()), agregadoJpa.getBairro(),
				new EnderecoCep(agregadoJpa.getCep()),
				new MunicipioCodigoIbge(agregadoJpa.getMunicipio().getIdentificador()));
		return new Cliente(Optional.of(identificador), new Cpf(agregadoJpa.getCpf()), agregadoJpa.getNome(), endereco);
	}

	@Override
	protected Long paraIdentificadorJpa(ClienteIdentificador identificador) {
		Objects.requireNonNull(identificador);

		return converter(identificador).orElseThrow(() -> {
			var mensagem = ClienteServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador);
			return new AgregadoInexistenteExcecao(mensagem);
		});
	}

	private static Optional<Long> converter(ClienteIdentificador identificador) {
		try {
			return Optional.of(Long.valueOf(identificador.toString()));
		} catch (NumberFormatException excecao) {
			return Optional.empty();
		}
	}

	@Override
	public boolean existePorIdentificador(ClienteIdentificador identificador) {
		Objects.requireNonNull(identificador);

		return converter(identificador).isPresent() && super.existePorIdentificador(identificador);
	}

	@Override
	public boolean existePorCpf(Cpf cpf, Optional<ClienteIdentificador> identificadorIgnorado) {
		Objects.requireNonNull(cpf);
		Objects.requireNonNull(identificadorIgnorado);

		var identificadorIgnoradoJpa = identificadorIgnorado.map(this::paraIdentificadorJpa).orElse(null);
		return repositorioJpa.existePorCpf(cpf.toString(), identificadorIgnoradoJpa);
	}
}
