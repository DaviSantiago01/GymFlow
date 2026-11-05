package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.armario;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
public record ReservaRequisicao(@NotNull LocalDate data, @NotNull @Min(6) @Max(21) Integer hora,
        @NotNull @Min(1) @Max(3) Integer duracaoHoras) { }
