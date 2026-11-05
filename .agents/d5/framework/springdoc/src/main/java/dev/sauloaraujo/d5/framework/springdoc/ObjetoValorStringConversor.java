package dev.sauloaraujo.d5.framework.springdoc;

import java.util.Iterator;

import org.jspecify.annotations.Nullable;

import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

/**
 * Objetos de valor de string são serializados como string simples; portanto, a
 * documentação não deve expor suas propriedades.
 */
class ObjetoValorStringConversor implements ModelConverter {
	@Override
	public @Nullable Schema<?> resolve(AnnotatedType tipo, ModelConverterContext contexto,
			Iterator<ModelConverter> cadeia) {
		var tipoJava = Json.mapper().constructType(tipo.getType());
		if (tipoJava != null && ObjetoValorStringAbstrato.class.isAssignableFrom(tipoJava.getRawClass())) {
			return new StringSchema();
		}
		return cadeia.hasNext() ? cadeia.next().resolve(tipo, contexto, cadeia) : null;
	}
}
