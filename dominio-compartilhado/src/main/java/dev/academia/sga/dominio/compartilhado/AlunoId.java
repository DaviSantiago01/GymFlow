package dev.academia.sga.dominio.compartilhado;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class AlunoId {
	private final int id;

	public AlunoId(int id) {
		isTrue(id > 0, "O id deve ser positivo");

		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof AlunoId) {
			var alunoId = (AlunoId) obj;
			return id == alunoId.id;
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
