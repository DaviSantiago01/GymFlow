package dev.sauloaraujo.d5.framework.springdoc;

import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class SpringDocEnvironmentPostProcessor implements EnvironmentPostProcessor {
	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		environment.getPropertySources().addLast(
				new MapPropertySource("d5-framework-springdoc", Map.of("springdoc.swagger-ui.csrf.enabled", true)));
	}
}
