package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao;

import java.time.Instant;
import java.util.Optional;

/** Versão de um plano de treino, com os treinos registrados nela e a recomendação que a originou, se houver (RN020). */
public record VersaoPlano(String identificador, int versao, Instant criacao, String situacao, int execucoes,
        Optional<String> recomendacao, Optional<String> recomendacaoTitulo, Optional<String> aprovadaPor) {
}
