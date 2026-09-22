package dev.academia.sga.dominio.treinamento.exercicio;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class ExercicioId {
	private final int id;

	public ExercicioId(int id) {
		isTrue(id > 0, "O id deve ser positivo");

		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ExercicioId) {
			var exercicioId = (ExercicioId) obj;
			return id == exercicioId.id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}
}
