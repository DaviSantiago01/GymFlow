package dev.sauloaraujo.d5.framework.aplicacao;

import java.util.List;

public interface IConsultaResultado<P extends IProjecao<?>> {
	List<P> getProjecoes();

	long getTotal();
}