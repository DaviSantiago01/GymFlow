package dev.sauloaraujo.d5.framework.ui;

import com.fasterxml.jackson.annotation.JsonValue;

abstract class ObjetoValorStringMixin {
	@JsonValue
	@Override
	public abstract String toString();
}