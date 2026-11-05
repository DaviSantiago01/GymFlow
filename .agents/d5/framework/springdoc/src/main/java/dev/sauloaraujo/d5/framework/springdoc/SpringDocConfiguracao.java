package dev.sauloaraujo.d5.framework.springdoc;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = "basic"))
@SecurityScheme(name = "basic", type = SecuritySchemeType.HTTP, scheme = "basic")
public class SpringDocConfiguracao {
	@Bean
	OperationCustomizer modelAttributeCustomizador() {
		return new ModelAttributeCustomizador();
	}

	@Bean
	ModelConverter objetoValorStringConversor() {
		return new ObjetoValorStringConversor();
	}
}