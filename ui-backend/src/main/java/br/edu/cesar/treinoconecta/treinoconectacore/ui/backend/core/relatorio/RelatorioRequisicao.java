package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.relatorio;

import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Agrupamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.FiltrosRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.TipoRelatorio;

public record RelatorioRequisicao(TipoRelatorio tipo, LocalDate inicio, LocalDate fim, Agrupamento agrupamento,
        @Nullable String plano, @Nullable String modalidade, @Nullable String personal, @Nullable String aula,
        @Nullable Boolean comparar) {
    public boolean compararComAnterior() {
        return Boolean.TRUE.equals(comparar);
    }

    public FiltrosRelatorio filtros() {
        return new FiltrosRelatorio(texto(plano).map(PlanoIdentificador::new), texto(modalidade),
                texto(personal).map(PersonalIdentificador::new), texto(aula).map(AulaColetivaIdentificador::new));
    }

    private static Optional<String> texto(@Nullable String valor) {
        return Optional.ofNullable(valor).map(String::strip).filter(conteudo -> !conteudo.isEmpty());
    }
}
