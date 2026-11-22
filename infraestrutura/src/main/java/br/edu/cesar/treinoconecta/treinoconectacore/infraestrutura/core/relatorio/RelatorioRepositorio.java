package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.relatorio;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.FiltrosRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.PeriodoAnalise;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Relatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.spi.IRelatorioRepositorio;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;
import tools.jackson.databind.json.JsonMapper;

/** Grava os valores calculados como JSON, para que o relatório possa ser consultado sem ser gerado de novo (RN017). */
public final class RelatorioRepositorio extends AgregadoRepositorioAbstrato<RelatorioIdentificador, Relatorio, Long, RelatorioJpa>
        implements IRelatorioRepositorio {
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final IRelatorioRepositorioJpa repositorioJpa;

    public RelatorioRepositorio(IRelatorioRepositorioJpa repositorioJpa) {
        super(repositorioJpa, RelatorioServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    @Override
    protected Long paraIdentificadorJpa(RelatorioIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected RelatorioJpa paraJpa(Relatorio agregado) {
        var filtros = agregado.getFiltros();
        return new RelatorioJpa(agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null),
                agregado.getTipo(), agregado.getPeriodo().inicio(), agregado.getPeriodo().fim(), agregado.getAgrupamento(),
                agregado.isComparar(), filtros.plano().map(id -> Long.valueOf(id.toString())).orElse(null),
                filtros.modalidade().orElse(null), filtros.personal().map(id -> Long.valueOf(id.toString())).orElse(null),
                filtros.aula().map(id -> Long.valueOf(id.toString())).orElse(null), agregado.getGeracao(),
                agregado.getUsuario(), agregado.getUsuarioNome(), agregado.getPerfil(), agregado.getVersao(),
                agregado.getAnterior().map(this::paraIdentificadorJpa).orElse(null), agregado.getSituacao(),
                JSON.writeValueAsString(agregado.getConteudo()));
    }

    @Override
    protected Relatorio paraAgregado(RelatorioJpa entidade) {
        var filtros = new FiltrosRelatorio(
                Optional.ofNullable(entidade.getFiltroPlano()).map(id -> new PlanoIdentificador(id.toString())),
                Optional.ofNullable(entidade.getFiltroModalidade()),
                Optional.ofNullable(entidade.getFiltroPersonal()).map(id -> new PersonalIdentificador(id.toString())),
                Optional.ofNullable(entidade.getFiltroAula()).map(id -> new AulaColetivaIdentificador(id.toString())));
        return new Relatorio(Optional.of(new RelatorioIdentificador(entidade.getIdentificador().toString())),
                entidade.getTipo(), new PeriodoAnalise(entidade.getInicio(), entidade.getFim()), entidade.getAgrupamento(),
                filtros, entidade.isComparar(), entidade.getGeracao(), entidade.getUsuario(), entidade.getUsuarioNome(),
                entidade.getPerfil(), entidade.getVersao(),
                Optional.ofNullable(entidade.getAnterior()).map(id -> new RelatorioIdentificador(id.toString())),
                entidade.getSituacao(), JSON.readValue(entidade.getConteudo(), ConteudoRelatorio.class));
    }

    @Override
    public Relatorio bloquear(RelatorioIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return paraAgregado(repositorioJpa.bloquear(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(RelatorioServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador))));
    }
}
