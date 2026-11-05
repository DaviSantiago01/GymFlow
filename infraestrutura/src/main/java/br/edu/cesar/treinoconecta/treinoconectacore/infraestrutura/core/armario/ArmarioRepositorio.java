package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.armario;

import java.util.NoSuchElementException;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.spi.IArmarioRepositorio;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;
public final class ArmarioRepositorio extends AgregadoRepositorioAbstrato<ArmarioCodigo,Armario,String,ArmarioJpa> implements IArmarioRepositorio {
    private final IArmarioRepositorioJpa jpa;
    public ArmarioRepositorio(IArmarioRepositorioJpa jpa) { super(jpa,"Armário %s inexistente"); this.jpa=java.util.Objects.requireNonNull(jpa); }
    protected String paraIdentificadorJpa(ArmarioCodigo id) { return id.toString(); }
    protected ArmarioJpa paraJpa(Armario a) { return new ArmarioJpa(a.getIdentificador().orElseThrow().toString(),a.getLocalizacao(),a.getDescricao()); }
    protected Armario paraAgregado(ArmarioJpa a) { return new Armario(new ArmarioCodigo(a.getIdentificador()),a.getLocalizacao(),a.getDescricao()); }
    public Armario bloquear(ArmarioCodigo codigo) { return paraAgregado(jpa.bloquear(codigo.toString()).orElseThrow(() -> new NoSuchElementException("Armário inexistente"))); }
}
