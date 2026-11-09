package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.Personal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.spi.IPersonalRepositorio;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;

public final class PersonalRepositorio extends AgregadoRepositorioAbstrato<PersonalIdentificador, Personal, Long, PersonalJpa>
        implements IPersonalRepositorio {
    private final IPersonalRepositorioJpa repositorioJpa;

    public PersonalRepositorio(IPersonalRepositorioJpa repositorioJpa) {
        super(repositorioJpa, PersonalServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
    }

    @Override
    protected Long paraIdentificadorJpa(PersonalIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected PersonalJpa paraJpa(Personal agregado) {
        var identificador = agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null);
        return new PersonalJpa(identificador, agregado.getUsuario(), agregado.getNome());
    }

    @Override
    protected Personal paraAgregado(PersonalJpa entidadeJpa) {
        var identificador = new PersonalIdentificador(entidadeJpa.getIdentificador().toString());
        return new Personal(Optional.of(identificador), entidadeJpa.getUsuario(), entidadeJpa.getNome());
    }

    @Override
    public Personal buscarPorUsuario(String usuario) {
        Objects.requireNonNull(usuario);
        var personal = repositorioJpa.findByUsuario(usuario)
                .orElseThrow(() -> new NaoAutorizadoExcecao(PersonalServico.USUARIO_SEM_CADASTRO));
        return paraAgregado(personal);
    }
}
