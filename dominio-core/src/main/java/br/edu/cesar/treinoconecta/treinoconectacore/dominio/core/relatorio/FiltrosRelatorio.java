package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IObjetoValor;

/** Filtros opcionais do relatório. A modalidade é a das aulas coletivas cadastradas. */
public record FiltrosRelatorio(Optional<PlanoIdentificador> plano, Optional<String> modalidade,
        Optional<PersonalIdentificador> personal, Optional<AulaColetivaIdentificador> aula) implements IObjetoValor {
    public static final int TAMANHO_MODALIDADE = 40;

    public FiltrosRelatorio {
        Objects.requireNonNull(plano);
        Objects.requireNonNull(personal);
        Objects.requireNonNull(aula);
        modalidade = Objects.requireNonNull(modalidade).map(String::strip).filter(valor -> !valor.isEmpty());
        modalidade.ifPresent(valor -> Validate.isTrue(valor.length() <= TAMANHO_MODALIDADE,
                RelatorioServico.FILTRO_INEXISTENTE_MODELO, TipoFiltro.MODALIDADE.getDescricao()));
    }

    public static FiltrosRelatorio nenhum() {
        return new FiltrosRelatorio(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    /** RN003: cada filtro informado precisa ser compatível com o tipo do relatório. */
    public void validarPara(TipoRelatorio tipo) {
        Objects.requireNonNull(tipo);
        validar(tipo, TipoFiltro.PLANO, plano.isPresent());
        validar(tipo, TipoFiltro.MODALIDADE, modalidade.isPresent());
        validar(tipo, TipoFiltro.PERSONAL, personal.isPresent());
        validar(tipo, TipoFiltro.AULA_COLETIVA, aula.isPresent());
    }

    private static void validar(TipoRelatorio tipo, TipoFiltro filtro, boolean informado) {
        Validate.isTrue(!informado || tipo.aceita(filtro), RelatorioServico.FILTRO_INCOMPATIVEL_MODELO,
                filtro.getDescricao(), tipo.getTitulo());
    }

    public FiltrosRelatorio comPersonal(PersonalIdentificador responsavel) {
        return new FiltrosRelatorio(plano, modalidade, Optional.of(responsavel), aula);
    }

    public boolean vazio() {
        return plano.isEmpty() && modalidade.isEmpty() && personal.isEmpty() && aula.isEmpty();
    }
}
