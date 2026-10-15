package dev.academia.sga.infraestrutura.persistencia.jpa.matricula;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.stereotype.Component;

import dev.academia.sga.dominio.compartilhado.AlunoId;
import dev.academia.sga.dominio.matricula.aluno.Aluno;
import dev.academia.sga.dominio.matricula.aluno.Email;

// cada contexto tem o seu mapeador, assim contexto novo nao mexe neste
@Component
class MatriculaJpaMapeador extends ModelMapper {
	MatriculaJpaMapeador() {
		var configuracao = getConfiguration();
		configuracao.setFieldMatchingEnabled(true);
		configuracao.setFieldAccessLevel(AccessLevel.PRIVATE);

		addConverter(new AbstractConverter<AlunoJpa, Aluno>() {
			@Override
			protected Aluno convert(AlunoJpa source) {
				var id = map(source.id, AlunoId.class);
				var emailContato = map(source.emailContato, Email.class);
				return new Aluno(id, source.nome, emailContato);
			}
		});

		addConverter(new AbstractConverter<Integer, AlunoId>() {
			@Override
			protected AlunoId convert(Integer source) {
				return new AlunoId(source);
			}
		});

		addConverter(new AbstractConverter<String, Email>() {
			@Override
			protected Email convert(String source) {
				return new Email(source);
			}
		});
	}

	@Override
	public <D> D map(Object source, Class<D> destinationType) {
		return source != null ? super.map(source, destinationType) : null;
	}
}
