package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.spi;

import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetiva;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IAulaColetivaRepositorio extends IAgregadoRepositorio<AulaColetivaIdentificador, AulaColetiva> {
    AulaColetiva bloquear(AulaColetivaIdentificador identificador);
    List<AulaColetiva> buscarAtivas();
    List<AulaColetiva> buscarInscricoesDoAluno(AlunoIdentificador aluno);
}
