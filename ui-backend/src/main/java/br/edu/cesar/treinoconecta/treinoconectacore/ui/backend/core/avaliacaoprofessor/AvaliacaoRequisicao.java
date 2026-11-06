package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.avaliacaoprofessor;

import java.math.BigDecimal;
import java.util.Optional;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaoprofessor.NotasAvaliacao;
public record AvaliacaoRequisicao(
    @NotNull(message="Informe a nota de auxílio") @DecimalMin("0") @DecimalMax("5") @Digits(integer=1,fraction=0,message="Use uma nota inteira de 0 a 5") BigDecimal auxilio,
    @NotNull(message="Informe a nota de pontualidade") @DecimalMin("0") @DecimalMax("5") @Digits(integer=1,fraction=0,message="Use uma nota inteira de 0 a 5") BigDecimal pontualidade,
    @NotNull(message="Informe a nota de postura") @DecimalMin("0") @DecimalMax("5") @Digits(integer=1,fraction=0,message="Use uma nota inteira de 0 a 5") BigDecimal postura,
    @NotNull(message="Informe a nota de didática") @DecimalMin("0") @DecimalMax("5") @Digits(integer=1,fraction=0,message="Use uma nota inteira de 0 a 5") BigDecimal didatica,
    @NotNull(message="Informe a nota de adequação") @DecimalMin("0") @DecimalMax("5") @Digits(integer=1,fraction=0,message="Use uma nota inteira de 0 a 5") BigDecimal adequacao,
    @Size(max=500,message="O comentário deve ter no máximo 500 caracteres") @Nullable String comentario) {
    public NotasAvaliacao notas() { return new NotasAvaliacao(auxilio.intValueExact(),pontualidade.intValueExact(),postura.intValueExact(),didatica.intValueExact(),adequacao.intValueExact()); }
    public Optional<String> comentarioOpcional() { return Optional.ofNullable(comentario); }
}
