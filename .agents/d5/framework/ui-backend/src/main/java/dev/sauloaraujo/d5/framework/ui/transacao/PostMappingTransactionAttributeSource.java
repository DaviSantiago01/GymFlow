package dev.sauloaraujo.d5.framework.ui.transacao;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.jspecify.annotations.Nullable;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class PostMappingTransactionAttributeSource implements TransactionAttributeSource {
	private static final TransactionAttribute ESCRITA = new DefaultTransactionAttribute();

	@Override
	public @Nullable TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		var metodoEspecifico = AopUtils.getMostSpecificMethod(method, targetClass);
		var mapeamento = AnnotatedElementUtils.findMergedAnnotation(metodoEspecifico, RequestMapping.class);

		if (mapeamento == null) {
			return null;
		}

		var metodos = mapeamento.method();
		var respondeAPost = metodos.length == 0 || Arrays.asList(metodos).contains(RequestMethod.POST);

		return respondeAPost ? ESCRITA : null;
	}
}