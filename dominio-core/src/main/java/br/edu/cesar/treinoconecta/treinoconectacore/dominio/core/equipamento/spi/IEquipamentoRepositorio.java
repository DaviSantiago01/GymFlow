package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.spi;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.Equipamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.EquipamentoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.spi.IAgregadoRepositorio;

public interface IEquipamentoRepositorio extends IAgregadoRepositorio<EquipamentoIdentificador, Equipamento> {
    Equipamento bloquear(EquipamentoIdentificador identificador);
    boolean existePatrimonio(String patrimonio, Optional<EquipamentoIdentificador> ignorado);
    boolean existeSerie(String serie, Optional<EquipamentoIdentificador> ignorado);
}
