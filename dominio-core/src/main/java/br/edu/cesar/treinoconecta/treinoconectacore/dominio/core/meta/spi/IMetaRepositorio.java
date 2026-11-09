package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.spi;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.Meta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IMetaRepositorio extends IAgregadoRepositorio<MetaIdentificador, Meta> {
    Meta bloquear(MetaIdentificador identificador);
    boolean existeAtivaNoPeriodo(AlunoIdentificador aluno, IndicadorFisico indicador, LocalDate inicio, LocalDate fim,
            Optional<MetaIdentificador> identificadorIgnorado);
    List<Meta> buscarAtivasPorAluno(AlunoIdentificador aluno);
    boolean existePorAvaliacaoReferencia(AvaliacaoFisicaIdentificador avaliacao);
    void transferirReferencia(AvaliacaoFisicaIdentificador antiga, AvaliacaoFisicaIdentificador nova);
}
