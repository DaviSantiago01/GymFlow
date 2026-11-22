package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.TipoRelatorio;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class RelatorioConsulta extends Consulta<RelatorioOrdenacao> {
    private final Optional<TipoRelatorio> tipo;
    private final Optional<String> usuario;
    private final Optional<Integer> dias;
    private final Optional<String> termo;

    public RelatorioConsulta(Optional<RelatorioOrdenacao> ordenacao, Optional<Boolean> descendente,
            Optional<Integer> indicePagina, Optional<Integer> tamanhoPagina, Optional<TipoRelatorio> tipo,
            Optional<String> usuario, Optional<Integer> dias, Optional<String> termo) {
        super(ordenacao, descendente, indicePagina, tamanhoPagina);
        this.tipo = Objects.requireNonNull(tipo);
        this.usuario = Objects.requireNonNull(usuario).filter(valor -> !valor.isBlank());
        this.dias = Objects.requireNonNull(dias);
        dias.ifPresent(valor -> Validate.isTrue(valor > 0 && valor <= 3660, "Quantidade de dias inválida"));
        this.termo = Objects.requireNonNull(termo).map(String::strip).filter(valor -> !valor.isEmpty());
    }

    public Optional<TipoRelatorio> getTipo() { return tipo; }
    public Optional<String> getUsuario() { return usuario; }
    public Optional<Integer> getDias() { return dias; }
    public Optional<String> getTermo() { return termo; }
}
