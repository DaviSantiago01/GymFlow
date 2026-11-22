package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio.spi;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IDesafioRepositorio extends IAgregadoRepositorio<DesafioIdentificador,Desafio> {
    Desafio bloquear(DesafioIdentificador identificador);
    boolean existeAvaliacaoInicial(DesafioIdentificador desafio,AlunoIdentificador aluno);
    Optional<AvaliacaoDesafio> buscarAvaliacaoInicial(DesafioIdentificador desafio, AlunoIdentificador aluno);
    boolean existeInscricao(DesafioIdentificador desafio,AlunoIdentificador aluno);
    void inscrever(Desafio desafio,AlunoIdentificador aluno,IndicadorDesafio indicador,BigDecimal meta,
            Long avaliacaoInicial,OffsetDateTime instante);
    AvaliacaoDesafio registrarAvaliacao(DesafioIdentificador desafio,AlunoIdentificador aluno,
            ProfissionalIdentificador profissional,TipoAvaliacaoDesafio tipo,BigDecimal peso,BigDecimal cintura,
            BigDecimal gordura,OffsetDateTime instante);
    Optional<AvaliacaoDesafio> buscarAvaliacao(Long identificador);
    void alterarAvaliacao(AvaliacaoDesafio avaliacao);
    boolean excluirAvaliacao(Long identificador);
    void encerrar(Desafio desafio);
}
