package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.metaacademia;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademiaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.spi.IMetaAcademiaRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.IndicadorAcademia;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class MetaAcademiaRepositorio
        extends AgregadoRepositorioAbstrato<MetaAcademiaIdentificador, MetaAcademia, Long, MetaAcademiaJpa>
        implements IMetaAcademiaRepositorio {
    private final IMetaAcademiaRepositorioJpa repositorioJpa;

    public MetaAcademiaRepositorio(IMetaAcademiaRepositorioJpa repositorioJpa) {
        super(repositorioJpa, MetaAcademiaServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    @Override
    protected Long paraIdentificadorJpa(MetaAcademiaIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected MetaAcademiaJpa paraJpa(MetaAcademia agregado) {
        return new MetaAcademiaJpa(agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null),
                agregado.getIndicador(), agregado.getValorDesejado(), agregado.getInicio(), agregado.getFim(),
                agregado.getSituacao(), agregado.getCriacao(), agregado.getResponsavel());
    }

    @Override
    protected MetaAcademia paraAgregado(MetaAcademiaJpa entidadeJpa) {
        return new MetaAcademia(Optional.of(new MetaAcademiaIdentificador(entidadeJpa.getIdentificador().toString())),
                entidadeJpa.getIndicador(), entidadeJpa.getValorDesejado(), entidadeJpa.getInicio(), entidadeJpa.getFim(),
                entidadeJpa.getSituacao(), entidadeJpa.getCriacao(), entidadeJpa.getResponsavel());
    }

    @Override
    public MetaAcademia bloquear(MetaAcademiaIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return paraAgregado(repositorioJpa.bloquear(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(MetaAcademiaServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador))));
    }

    @Override
    public boolean existeAtivaSobreposta(IndicadorAcademia indicador, LocalDate inicio, LocalDate fim,
            Optional<MetaAcademiaIdentificador> ignorada) {
        Objects.requireNonNull(ignorada);
        return repositorioJpa.existeAtivaSobreposta(indicador, inicio, fim,
                ignorada.map(this::paraIdentificadorJpa).orElse(null));
    }

    @Override
    public List<MetaAcademia> buscarVigentes(LocalDate inicio, LocalDate fim) {
        var resultado = new ArrayList<MetaAcademia>();
        for (var entidade : repositorioJpa.buscarVigentes(inicio, fim)) {
            resultado.add(paraAgregado(entidade));
        }
        return resultado;
    }
}
