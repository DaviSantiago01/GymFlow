package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.treino;

import java.time.Clock;
import java.util.*;
import org.springframework.data.domain.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.*;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.treino.spi.ITreinoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.treino.TreinoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;
public final class TreinoAplicacaoRepositorio extends AgregadoAplicacaoRepositorioAbstrato<TreinoProjecao,TreinoOrdenacao,TreinoConsulta> implements ITreinoAplicacaoRepositorio {
    private final ITreinoRepositorioJpa jpa;private final ISegurancaServico seguranca;private final Clock relogio;
    public TreinoAplicacaoRepositorio(ITreinoRepositorioJpa jpa,ISegurancaServico seguranca,Clock relogio){this.jpa=Objects.requireNonNull(jpa);this.seguranca=Objects.requireNonNull(seguranca);this.relogio=Objects.requireNonNull(relogio);}
    protected Page<TreinoProjecao> pesquisar(TreinoConsulta consulta,Pageable pagina){return jpa.historico(seguranca.getUsuario(),relogio.instant(),PageRequest.of(pagina.getPageNumber(),pagina.getPageSize()));}
    protected String paraPropriedadeOrdenacao(TreinoOrdenacao ordenacao){return "identificador";}
    public TreinoProjecao detalhe(TreinoIdentificador id,String usuario){return jpa.detalhe(Long.valueOf(id.toString()),usuario,relogio.instant()).orElseThrow(()->new NoSuchElementException("Treino concluído não encontrado para este aluno"));}
}
