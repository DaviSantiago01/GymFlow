package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.armario;

import java.time.LocalDate;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;
public record ReservaRequisicao(@NotNull LocalDate data,
        @NotNull @Min(6) @Max(21) @Digits(integer=2, fraction=0) BigDecimal hora,
        @NotNull @Min(1) @Max(3) @Digits(integer=1, fraction=0) BigDecimal duracaoHoras) { }
