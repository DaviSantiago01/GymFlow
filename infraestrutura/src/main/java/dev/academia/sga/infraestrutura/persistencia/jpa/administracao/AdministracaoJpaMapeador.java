package dev.academia.sga.infraestrutura.persistencia.jpa.administracao;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.stereotype.Component;

import dev.academia.sga.dominio.administracao.instrutor.Instrutor;
import dev.academia.sga.dominio.compartilhado.InstrutorId;

@Component
class AdministracaoJpaMapeador extends ModelMapper {
	AdministracaoJpaMapeador() {
		var configuracao = getConfiguration();
		configuracao.setFieldMatchingEnabled(true);
		configuracao.setFieldAccessLevel(AccessLevel.PRIVATE);

		addConverter(new AbstractConverter<InstrutorJpa, Instrutor>() {
			@Override
			protected Instrutor convert(InstrutorJpa source) {
				var id = map(source.id, InstrutorId.class);
				return new Instrutor(id, source.nome, source.especialidade);
			}
		});

		addConverter(new AbstractConverter<Integer, InstrutorId>() {
			@Override
			protected InstrutorId convert(Integer source) {
				return new InstrutorId(source);
			}
		});
	}

	@Override
	public <D> D map(Object source, Class<D> destinationType) {
		return source != null ? super.map(source, destinationType) : null;
	}
}
