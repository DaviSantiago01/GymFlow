package dev.sauloaraujo.d5.framework.ui.transacao;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class TransacaoConfiguracao {
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	static DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
		var autoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		autoProxyCreator.setProxyTargetClass(true);
		return autoProxyCreator;
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	static TransactionAttributeSource postMappingTransactionAttributeSource() {
		return new PostMappingTransactionAttributeSource();
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	static TransactionInterceptor postMappingTransactionInterceptor(
			TransactionAttributeSource postMappingTransactionAttributeSource) {
		var interceptor = new TransactionInterceptor();
		interceptor.setTransactionAttributeSource(postMappingTransactionAttributeSource);
		return interceptor;
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	static BeanFactoryTransactionAttributeSourceAdvisor postMappingTransactionAdvisor(
			TransactionAttributeSource postMappingTransactionAttributeSource,
			TransactionInterceptor postMappingTransactionInterceptor) {
		var advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
		advisor.setTransactionAttributeSource(postMappingTransactionAttributeSource);
		advisor.setAdvice(postMappingTransactionInterceptor);
		return advisor;
	}
}
