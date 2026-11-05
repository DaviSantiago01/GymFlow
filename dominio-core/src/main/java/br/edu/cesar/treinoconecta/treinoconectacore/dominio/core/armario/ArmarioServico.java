package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.spi.IArmarioRepositorio;
import dev.sauloaraujo.d5.framework.dominio.impl.*;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
public final class ArmarioServico extends AgregadoServicoAbstrato<ArmarioCodigo, Armario, AgregadoAutorizacoes<Armario>> {
    public ArmarioServico(ISegurancaServico seguranca, IArmarioRepositorio repositorio) {
        super(seguranca, repositorio, "Registro %s existente", "Registro %s inexistente");
    }
    @Override public AgregadoAutorizacoes<Armario> criarAutorizacoes(Optional<ArmarioCodigo> identificador) {
        Objects.requireNonNull(identificador);
        return new AgregadoAutorizacoes<>(false, getSegurancaServico().possuiAutorizacao("USUARIO"), false, false);
    }
}
