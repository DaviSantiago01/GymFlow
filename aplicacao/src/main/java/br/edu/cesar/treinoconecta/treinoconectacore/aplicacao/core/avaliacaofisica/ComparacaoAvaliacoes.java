package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica;

import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.ComparacaoIndicador;

public record ComparacaoAvaliacoes(AvaliacaoFisicaProjecao atual, Optional<AvaliacaoFisicaProjecao> referencia,
        List<ComparacaoIndicador> indicadores, List<AvaliacaoFisicaProjecao> disponiveis) { }
