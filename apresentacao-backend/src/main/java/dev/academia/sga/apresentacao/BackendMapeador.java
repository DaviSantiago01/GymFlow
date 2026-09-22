package dev.academia.sga.apresentacao;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import dev.academia.sga.dominio.administracao.aluno.AlunoId;
import dev.academia.sga.dominio.administracao.instrutor.InstrutorId;
import dev.academia.sga.dominio.treinamento.exercicio.ExercicioId;
import dev.academia.sga.dominio.treinamento.treino.TreinoId;

@Component
public class BackendMapeador extends ModelMapper {
	BackendMapeador() {
		addConverter(new AbstractConverter<Integer, AlunoId>() {
			@Override
			protected AlunoId convert(Integer source) {
				return new AlunoId(source);
			}
		});

		addConverter(new AbstractConverter<Integer, InstrutorId>() {
			@Override
			protected InstrutorId convert(Integer source) {
				return new InstrutorId(source);
			}
		});

		addConverter(new AbstractConverter<Integer, ExercicioId>() {
			@Override
			protected ExercicioId convert(Integer source) {
				return new ExercicioId(source);
			}
		});

		addConverter(new AbstractConverter<Integer, TreinoId>() {
			@Override
			protected TreinoId convert(Integer source) {
				return new TreinoId(source);
			}
		});
	}

	@Override
	public <D> D map(Object source, Class<D> destinationType) {
		return source != null ? super.map(source, destinationType) : null;
	}
}
