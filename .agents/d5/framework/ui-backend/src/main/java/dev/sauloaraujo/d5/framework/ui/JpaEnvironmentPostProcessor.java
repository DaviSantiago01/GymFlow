package dev.sauloaraujo.d5.framework.ui;

import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class JpaEnvironmentPostProcessor implements EnvironmentPostProcessor {
	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		environment.getPropertySources()
				.addLast(new MapPropertySource("d5-framework-ui-backend", Map.of("spring.jpa.open-in-view", false)));
	}
}
