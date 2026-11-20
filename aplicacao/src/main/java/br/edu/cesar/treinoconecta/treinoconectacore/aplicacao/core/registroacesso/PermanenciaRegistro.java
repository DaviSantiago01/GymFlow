package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.registroacesso;

/**
 * Registro com o tempo de permanência calculado (F006, RN008). Enquanto não há saída, o tempo é contado até agora e
 * {@code excedeuLimite} indica o que será marcado para verificação (RN009).
 */
public record PermanenciaRegistro(RegistroAcessoProjecao registro, long minutos, boolean excedeuLimite) { }
