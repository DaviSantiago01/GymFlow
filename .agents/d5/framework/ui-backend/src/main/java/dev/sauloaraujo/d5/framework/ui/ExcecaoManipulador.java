package dev.sauloaraujo.d5.framework.ui;

import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.sauloaraujo.d5.framework.dominio.impl.ObjetoValorStringAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutenticadoExcecao;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class ExcecaoManipulador {
	private static final Logger LOG = LoggerFactory.getLogger(ExcecaoManipulador.class);
	private static final String SQLSTATE_DADO_INVALIDO = "22";
	private static final String SQLSTATE_VIOLACAO_INTEGRIDADE = "23";
	private static final String VALOR_FORA_DOS_LIMITES = "Um ou mais valores informados excedem o tamanho ou o intervalo permitidos.";
	private static final String RESTRICAO_VIOLADA = "Os dados informados violam uma restrição do banco de dados, como a unicidade de um valor.";

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<Object> tratarAcessoNegado(AccessDeniedException excecao) {
		return corpo(HttpStatus.FORBIDDEN, excecao);
	}

	@ExceptionHandler(NaoAutorizadoExcecao.class)
	ResponseEntity<Object> tratarAcessoNegado(NaoAutorizadoExcecao excecao) {
		return corpo(HttpStatus.FORBIDDEN, excecao);
	}

	@ExceptionHandler(AuthenticationException.class)
	ResponseEntity<Object> tratarNaoAutenticado(AuthenticationException excecao) {
		return corpo(HttpStatus.UNAUTHORIZED, excecao);
	}

	@ExceptionHandler(NaoAutenticadoExcecao.class)
	ResponseEntity<Object> tratarNaoAutenticado(NaoAutenticadoExcecao excecao) {
		return corpo(HttpStatus.UNAUTHORIZED, excecao);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<Object> tratarArgumentoInvalido(IllegalArgumentException excecao) {
		return corpo(HttpStatus.UNPROCESSABLE_CONTENT, excecao);
	}

	@ExceptionHandler(NullPointerException.class)
	ResponseEntity<Object> tratarValorAusente(NullPointerException excecao) {
		return corpo(HttpStatus.UNPROCESSABLE_CONTENT, excecao);
	}

	@ExceptionHandler(IllegalStateException.class)
	ResponseEntity<Object> tratarConflito(IllegalStateException excecao) {
		return corpo(HttpStatus.CONFLICT, excecao);
	}

	@ExceptionHandler(NoSuchElementException.class)
	ResponseEntity<Object> tratarInexistente(NoSuchElementException excecao) {
		return corpo(HttpStatus.NOT_FOUND, excecao);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<Object> tratarCorpoInvalido(HttpMessageNotReadableException excecao) {
		return tratarCausa(excecao.getCause())
				.orElseGet(() -> corpo(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido."));
	}

	@ExceptionHandler(TypeMismatchException.class)
	ResponseEntity<Object> tratarParametroInvalido(TypeMismatchException excecao) {
		return tratarConversao(excecao, excecao.getPropertyName(), excecao.getValue());
	}

	@ExceptionHandler(BindException.class)
	ResponseEntity<Object> tratarParametrosInvalidos(BindException excecao) {
		for (var erro : excecao.getAllErrors()) {
			if (erro instanceof FieldError erroCampo && erroCampo.contains(TypeMismatchException.class)) {
				return tratarConversao(erroCampo.unwrap(TypeMismatchException.class), erroCampo.getField(),
						erroCampo.getRejectedValue());
			}

			if (erro.contains(Throwable.class)) {
				var resposta = tratarCausa(erro.unwrap(Throwable.class));
				if (resposta.isPresent()) {
					return resposta.get();
				}
			}
		}

		return corpo(HttpStatus.BAD_REQUEST, "Parâmetros da requisição inválidos.");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<Object> tratarViolacaoIntegridade(DataIntegrityViolationException excecao) {
		for (Throwable causa = excecao; causa != null; causa = causa.getCause()) {
			if (causa instanceof SQLException sqlException) {
				var estado = sqlException.getSQLState();
				if (estado != null && estado.startsWith(SQLSTATE_DADO_INVALIDO)) {
					return corpo(HttpStatus.UNPROCESSABLE_CONTENT, VALOR_FORA_DOS_LIMITES);
				}
				if (estado != null && estado.startsWith(SQLSTATE_VIOLACAO_INTEGRIDADE)) {
					return corpo(HttpStatus.CONFLICT, RESTRICAO_VIOLADA);
				}
			}
		}

		return tratarExcecaoNaoMapeada(excecao);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<Object> tratarRestricaoViolada(ConstraintViolationException excecao) {
		var mensagem = excecao.getConstraintViolations().stream()
				.map(violacao -> violacao.getPropertyPath() + ": " + violacao.getMessage()).sorted()
				.collect(Collectors.joining("; "));
		return corpo(HttpStatus.UNPROCESSABLE_CONTENT, mensagem);
	}

	@ExceptionHandler(TransactionSystemException.class)
	ResponseEntity<Object> tratarFalhaTransacao(TransactionSystemException excecao) {
		for (Throwable causa = excecao; causa != null; causa = causa.getCause()) {
			if (causa instanceof ConstraintViolationException restricaoViolada) {
				return tratarRestricaoViolada(restricaoViolada);
			}
		}

		return tratarExcecaoNaoMapeada(excecao);
	}

	@ExceptionHandler(BeanInstantiationException.class)
	ResponseEntity<Object> tratarInstanciacaoInvalida(BeanInstantiationException excecao) {
		return tratarCausa(excecao.getCause()).orElseGet(() -> tratarExcecaoNaoMapeada(excecao));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<Object> tratarExcecaoNaoMapeada(Exception excecao) {
		if (excecao instanceof ErrorResponse respostaErro && !respostaErro.getStatusCode().is5xxServerError()) {
			return corpo(respostaErro.getStatusCode(), mensagemPadrao(respostaErro.getStatusCode()));
		}

		LOG.error("Erro não tratado", excecao);
		return corpo(HttpStatus.INTERNAL_SERVER_ERROR, mensagemPadrao(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	private ResponseEntity<Object> tratarConversao(TypeMismatchException excecao, @Nullable String nomeParametro,
			@Nullable Object valor) {
		if (excecao instanceof ConversionNotSupportedException) {
			return tratarExcecaoNaoMapeada(excecao);
		}

		for (Throwable causa = excecao; causa != null; causa = causa.getCause()) {
			if (causa instanceof ConversionFailedException conversao
					&& ObjetoValorStringAbstrato.class.isAssignableFrom(conversao.getTargetType().getType())) {
				var resposta = tratarCausa(conversao.getCause());
				if (resposta.isPresent()) {
					return resposta.get();
				}
			}
		}

		var parametro = nomeParametro != null ? "o parâmetro " + nomeParametro : "um parâmetro";
		var mensagem = "Valor inválido para " + parametro + (valor != null ? ": " + valor : "");
		return corpo(HttpStatus.UNPROCESSABLE_CONTENT, mensagem);
	}

	private Optional<ResponseEntity<Object>> tratarCausa(@Nullable Throwable excecao) {
		for (var causa = excecao; causa != null; causa = causa.getCause()) {
			if (causa instanceof IllegalStateException illegalStateException) {
				return Optional.of(tratarConflito(illegalStateException));
			}

			if (causa instanceof NoSuchElementException noSuchElementException) {
				return Optional.of(tratarInexistente(noSuchElementException));
			}

			if (causa instanceof IllegalArgumentException illegalArgumentException) {
				return Optional.of(tratarArgumentoInvalido(illegalArgumentException));
			}

			if (causa instanceof NullPointerException nullPointerException) {
				return Optional.of(tratarValorAusente(nullPointerException));
			}
		}

		return Optional.empty();
	}

	private ResponseEntity<Object> corpo(HttpStatus status, Exception excecao) {
		return corpo(status, excecao.getMessage());
	}

	private ResponseEntity<Object> corpo(HttpStatusCode status, @Nullable String mensagem) {
		return ResponseEntity.status(status)
				.body(Map.of("mensagem", mensagem != null ? mensagem : mensagemPadrao(status)));
	}

	private static String mensagemPadrao(HttpStatusCode status) {
		return switch (status.value()) {
		case 400 -> "Requisição inválida.";
		case 401 -> "Usuário não autenticado.";
		case 403 -> "Ação não autorizada.";
		case 404 -> "Recurso não encontrado.";
		case 405 -> "Método HTTP não suportado.";
		case 406 -> "Formato de resposta não suportado.";
		case 409 -> "Conflito com o estado atual do recurso.";
		case 415 -> "Formato do corpo da requisição não suportado.";
		case 422 -> "Dados inválidos.";
		default -> status.is4xxClientError() ? "Requisição inválida." : "Erro interno do servidor.";
		};
	}
}
