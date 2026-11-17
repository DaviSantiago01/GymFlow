package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.spi;

import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.AdesaoPlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IAdesaoPlanoRepositorio extends IAgregadoRepositorio<AdesaoPlanoIdentificador, AdesaoPlano> {
    AdesaoPlano bloquear(AdesaoPlanoIdentificador identificador);
    Optional<AdesaoPlano> buscarAtivaDoAluno(AlunoIdentificador aluno);
    List<AdesaoPlano> buscarAtivas();
}
