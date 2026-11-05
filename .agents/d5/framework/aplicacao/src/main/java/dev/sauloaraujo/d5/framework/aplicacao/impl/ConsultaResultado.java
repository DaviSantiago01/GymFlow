package dev.sauloaraujo.d5.framework.aplicacao.impl;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import dev.sauloaraujo.d5.framework.aplicacao.IConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

public class ConsultaResultado<P extends IProjecao<?>> implements IConsultaResultado<P> {
	private final List<P> projecoes;
	private final long total;

	public ConsultaResultado(List<P> projecoes, long total) {
		Objects.requireNonNull(projecoes);
		Validate.isTrue(total >= 0);
		this.projecoes = List.copyOf(projecoes);
		this.total = total;
	}

	@Override
	public List<P> getProjecoes() {
		return projecoes;
	}

	@Override
	public long getTotal() {
		return total;
	}
}
