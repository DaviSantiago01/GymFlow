package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.reserva;

import java.time.Instant;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.*;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;
public record ReservaArmarioProjecao(String identificador, String alunoNome, String armario,
        String localizacao, Instant confirmacao, Instant inicio, Instant termino, Optional<Instant> encerramento,
        SituacaoReserva situacao) implements IProjecao<ReservaArmario> { }
