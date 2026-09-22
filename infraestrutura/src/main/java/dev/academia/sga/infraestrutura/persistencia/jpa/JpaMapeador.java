package dev.academia.sga.infraestrutura.persistencia.jpa;

import java.util.List;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dev.academia.sga.dominio.administracao.aluno.Aluno;
import dev.academia.sga.dominio.administracao.aluno.AlunoId;
import dev.academia.sga.dominio.administracao.aluno.Email;
import dev.academia.sga.dominio.administracao.instrutor.Instrutor;
import dev.academia.sga.dominio.administracao.instrutor.InstrutorId;
import dev.academia.sga.dominio.treinamento.exercicio.Exercicio;
import dev.academia.sga.dominio.treinamento.exercicio.ExercicioId;
import dev.academia.sga.dominio.treinamento.treino.Treino;
import dev.academia.sga.dominio.treinamento.treino.TreinoId;

@Component
class JpaMapeador extends ModelMapper {
	private @Autowired AlunoJpaRepository alunoRepositorio;
	private @Autowired ExercicioJpaRepository exercicioRepositorio;
	private @Autowired InstrutorJpaRepository instrutorRepositorio;

	JpaMapeador() {
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

		addConverter(new AbstractConverter<AlunoJpa, AlunoId>() {
			@Override
			protected AlunoId convert(AlunoJpa source) {
				return map(source.id, AlunoId.class);
			}
		});

		addConverter(new AbstractConverter<AlunoId, AlunoJpa>() {
			@Override
			protected AlunoJpa convert(AlunoId source) {
				return alunoRepositorio.findById(source.getId()).get();
			}
		});

		addConverter(new AbstractConverter<String, Email>() {
			@Override
			protected Email convert(String source) {
				return new Email(source);
			}
		});

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

		addConverter(new AbstractConverter<InstrutorJpa, InstrutorId>() {
			@Override
			protected InstrutorId convert(InstrutorJpa source) {
				return map(source.id, InstrutorId.class);
			}
		});

		addConverter(new AbstractConverter<InstrutorId, InstrutorJpa>() {
			@Override
			protected InstrutorJpa convert(InstrutorId source) {
				return instrutorRepositorio.findById(source.getId()).get();
			}
		});

		addConverter(new AbstractConverter<ExercicioJpa, Exercicio>() {
			@Override
			protected Exercicio convert(ExercicioJpa source) {
				var id = map(source.id, ExercicioId.class);
				return new Exercicio(id, source.nome, source.grupoMuscular);
			}
		});

		addConverter(new AbstractConverter<Integer, ExercicioId>() {
			@Override
			protected ExercicioId convert(Integer source) {
				return new ExercicioId(source);
			}
		});

		addConverter(new AbstractConverter<ExercicioJpa, ExercicioId>() {
			@Override
			protected ExercicioId convert(ExercicioJpa source) {
				return map(source.id, ExercicioId.class);
			}
		});

		addConverter(new AbstractConverter<ExercicioId, ExercicioJpa>() {
			@Override
			protected ExercicioJpa convert(ExercicioId source) {
				return exercicioRepositorio.findById(source.getId()).get();
			}
		});

		addConverter(new AbstractConverter<TreinoJpa, Treino>() {
			@Override
			protected Treino convert(TreinoJpa source) {
				var id = map(source.id, TreinoId.class);
				var aluno = map(source.aluno, AlunoId.class);
				var instrutor = map(source.instrutor, InstrutorId.class);
				List<ExercicioId> exercicios = map(source.exercicios, new TypeToken<List<ExercicioId>>() {
				}.getType());
				return new Treino(id, source.nome, source.objetivo, aluno, instrutor, exercicios);
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
