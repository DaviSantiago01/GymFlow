package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Quem respondeu, quando e, se houver, o motivo da recusa ou o ajuste feito pelo personal (RN019). */
public record RespostaRecomendacao(Instant instante, String respondente, Optional<String> motivo, Optional<String> ajuste)
        implements IObjetoValor {
    public static final int TAMANHO_TEXTO = 250;

    public RespostaRecomendacao {
        Objects.requireNonNull(instante);
        Validate.notBlank(respondente, "Responsável pela resposta obrigatório");
        motivo = Objects.requireNonNull(motivo).map(String::strip).filter(texto -> !texto.isEmpty());
        ajuste = Objects.requireNonNull(ajuste).map(String::strip).filter(texto -> !texto.isEmpty());
        motivo.ifPresent(texto -> Validate.isTrue(texto.length() <= TAMANHO_TEXTO, RecomendacaoServico.TEXTO_LONGO));
        ajuste.ifPresent(texto -> Validate.isTrue(texto.length() <= TAMANHO_TEXTO, RecomendacaoServico.TEXTO_LONGO));
    }
}
