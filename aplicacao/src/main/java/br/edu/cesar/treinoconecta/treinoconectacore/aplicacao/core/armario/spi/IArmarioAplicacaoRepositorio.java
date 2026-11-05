package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.spi;

import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.ArmarioCodigo;
import dev.sauloaraujo.d5.framework.aplicacao.spi.IAgregadoAplicacaoRepositorio;
public interface IArmarioAplicacaoRepositorio extends IAgregadoAplicacaoRepositorio<ArmarioProjecao,ArmarioConsulta> {
    ArmarioProjecao detalhe(ArmarioCodigo codigo, Instant agora);
    java.util.List<br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.armario.PeriodoOcupado> periodosOcupados(ArmarioCodigo codigo, Instant inicio, Instant termino);
}
