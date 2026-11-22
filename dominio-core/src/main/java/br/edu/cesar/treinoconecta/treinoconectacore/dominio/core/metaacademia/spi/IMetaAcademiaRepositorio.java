package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.spi;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.IndicadorAcademia;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IMetaAcademiaRepositorio extends IAgregadoRepositorio<MetaAcademiaIdentificador, MetaAcademia> {
    MetaAcademia bloquear(MetaAcademiaIdentificador identificador);

    boolean existeAtivaSobreposta(IndicadorAcademia indicador, LocalDate inicio, LocalDate fim,
            Optional<MetaAcademiaIdentificador> ignorada);

    /** Metas ativas ou já encerradas cujo período se sobrepõe ao intervalo informado. Substituídas ficam de fora. */
    List<MetaAcademia> buscarVigentes(LocalDate inicio, LocalDate fim);
}
