package dev.academia.sga.dominio.treino.treino;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class TreinoId {
	private final int id;

	public TreinoId(int id) {
		isTrue(id > 0, "O id deve ser positivo");

		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof TreinoId) {
			var treinoId = (TreinoId) obj;
			return id == treinoId.id;
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
