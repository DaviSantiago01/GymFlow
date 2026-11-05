package dev.sauloaraujo.d5.framework.ui;

import java.lang.reflect.InvocationTargetException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;

public class ObjetoValorStringConversorFactory implements ConverterFactory<String, ObjetoValorStringAbstrato> {
	@Override
	public <T extends ObjetoValorStringAbstrato> Converter<String, T> getConverter(Class<T> tipoAlvo) {
		return source -> instanciar(tipoAlvo, source);
	}

	private <T extends ObjetoValorStringAbstrato> T instanciar(Class<T> tipoAlvo, String source) {
		try {
			return tipoAlvo.getConstructor(String.class).newInstance(source);
		} catch (InvocationTargetException excecao) {
			if (excecao.getCause() instanceof RuntimeException causa) {
				throw causa;
			}
			throw new IllegalArgumentException("Não foi possível converter para " + tipoAlvo.getSimpleName(), excecao);
		} catch (ReflectiveOperationException excecao) {
			throw new IllegalArgumentException("Não foi possível converter para " + tipoAlvo.getSimpleName(), excecao);
		}
	}
}
