package dev.sauloaraujo.d5.framework.springdoc;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

class ModelAttributeCustomizador implements OperationCustomizer {
	@Override
	public Operation customize(Operation operacao, HandlerMethod handlerMethod) {
		if (operacao.getParameters() == null) {
			return operacao;
		}
		for (var parametroMetodo : handlerMethod.getMethodParameters()) {
			if (parametroMetodo.hasParameterAnnotation(ModelAttribute.class)) {
				expandir(operacao, Objects.requireNonNull(parametroMetodo.getParameterName()),
						parametroMetodo.getParameterType());
			}
		}
		return operacao;
	}

	private void expandir(Operation operacao, String nomeParametro, Class<?> tipoParametro) {
		var parametroOriginal = remover(operacao, nomeParametro);
		if (parametroOriginal.isEmpty()) {
			return;
		}
		var esquema = ModelConverters.getInstance().resolveAsResolvedSchema(new AnnotatedType(tipoParametro)).schema;
		if (esquema == null || esquema.getProperties() == null) {
			operacao.getParameters().add(parametroOriginal.get());
			return;
		}
		var propriedadesObrigatorias = esquema.getRequired();
		esquema.getProperties().forEach((nome, esquemaPropriedade) -> {
			var parametroExpandido = new Parameter().name((String) nome).in(parametroOriginal.get().getIn())
					.schema((Schema<?>) esquemaPropriedade)
					.required(propriedadesObrigatorias != null && propriedadesObrigatorias.contains(nome));
			operacao.getParameters().add(parametroExpandido);
		});
	}

	private Optional<Parameter> remover(Operation operacao, String nomeParametro) {
		var parametros = new ArrayList<>(operacao.getParameters());
		var parametroOriginal = parametros.stream()
				.filter(parametro -> Objects.equals(parametro.getName(), nomeParametro)).findFirst();
		parametroOriginal.ifPresent(parametros::remove);
		operacao.setParameters(parametros);
		return parametroOriginal;
	}
}
