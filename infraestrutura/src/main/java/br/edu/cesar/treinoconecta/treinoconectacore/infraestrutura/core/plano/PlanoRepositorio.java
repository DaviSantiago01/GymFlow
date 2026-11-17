package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.plano;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.Plano;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.spi.IPlanoRepositorio;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class PlanoRepositorio extends AgregadoRepositorioAbstrato<PlanoIdentificador, Plano, Long, PlanoJpa>
        implements IPlanoRepositorio {
    private final IPlanoRepositorioJpa repositorioJpa;

    public PlanoRepositorio(IPlanoRepositorioJpa repositorioJpa) {
        super(repositorioJpa, PlanoServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    @Override
    protected Long paraIdentificadorJpa(PlanoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected PlanoJpa paraJpa(Plano agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        return new PlanoJpa(identificador, agregado.getNome(), agregado.getValor(), agregado.getDuracaoMeses(),
                agregado.getSituacao());
    }

    @Override
    protected Plano paraAgregado(PlanoJpa entidadeJpa) {
        return new Plano(Optional.of(new PlanoIdentificador(entidadeJpa.getIdentificador().toString())), entidadeJpa.getNome(),
                entidadeJpa.getValor(), entidadeJpa.getDuracaoMeses(), entidadeJpa.getSituacao());
    }

    /** A gravação imediata mantém a restrição de unicidade do banco coerente com a ordem dos comandos. */
    @Override
    public void alterar(Plano agregado) {
        Objects.requireNonNull(agregado);
        repositorioJpa.saveAndFlush(paraJpa(agregado));
    }

    @Override
    public Plano bloquear(PlanoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return paraAgregado(repositorioJpa.bloquear(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(PlanoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador))));
    }

    @Override
    public boolean existeAtivoComNomeEDuracao(String nome, int duracaoMeses, Optional<PlanoIdentificador> identificadorIgnorado) {
        Objects.requireNonNull(nome);
        var ignorado = identificadorIgnorado.map(this::paraIdentificadorJpa).orElse(null);
        return repositorioJpa.existeAtivoComNomeEDuracao(nome, duracaoMeses, ignorado);
    }
}
