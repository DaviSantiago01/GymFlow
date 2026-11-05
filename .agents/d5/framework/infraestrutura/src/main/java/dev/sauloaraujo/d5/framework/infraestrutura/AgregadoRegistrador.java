package dev.sauloaraujo.d5.framework.infraestrutura;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import dev.sauloaraujo.d5.framework.aplicacao.IAgregadoAplicacaoServico;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;
import dev.sauloaraujo.d5.framework.dominio.IAgregadoServico;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public class AgregadoRegistrador implements ImportBeanDefinitionRegistrar {
	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		var scanner = criarScanner();
		var beanFactory = (BeanFactory) registry;

		for (var pacoteBase : AutoConfigurationPackages.get(beanFactory)) {
			for (var candidato : scanner.findCandidateComponents(pacoteBase)) {
				registrar(candidato, registry);
			}
		}
	}

	private ClassPathScanningCandidateComponentProvider criarScanner() {
		var scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addExcludeFilter(new AnnotationTypeFilter(Component.class));
		scanner.addIncludeFilter(new AssignableTypeFilter(IAgregadoRepositorio.class));
		scanner.addIncludeFilter(new AssignableTypeFilter(IAgregadoAplicacaoRepositorio.class));
		scanner.addIncludeFilter(new AssignableTypeFilter(IAgregadoServico.class));
		scanner.addIncludeFilter(new AssignableTypeFilter(IAgregadoAplicacaoServico.class));
		return scanner;
	}

	private void registrar(BeanDefinition candidato, BeanDefinitionRegistry registry) {
		var definicao = new GenericBeanDefinition();
		definicao.setBeanClassName(candidato.getBeanClassName());
		definicao.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);

		var nome = BeanDefinitionReaderUtils.generateBeanName(definicao, registry);
		registry.registerBeanDefinition(nome, definicao);
	}
}