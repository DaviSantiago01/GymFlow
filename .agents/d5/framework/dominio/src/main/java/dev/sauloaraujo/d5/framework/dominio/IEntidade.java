package dev.sauloaraujo.d5.framework.dominio;

import java.util.Optional;

public interface IEntidade<I extends IIdentificador> {
	Optional<I> getIdentificador();
}