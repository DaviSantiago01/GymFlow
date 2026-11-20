package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.registroacesso;

import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.academia.AcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcesso;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcessoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcessoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.spi.IRegistroAcessoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia.AcademiaJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

public final class RegistroAcessoRepositorio
        extends AgregadoRepositorioAbstrato<RegistroAcessoIdentificador, RegistroAcesso, Long, RegistroAcessoJpa>
        implements IRegistroAcessoRepositorio {
    private final IRegistroAcessoRepositorioJpa jpa;
    private final EntityManager em;

    public RegistroAcessoRepositorio(IRegistroAcessoRepositorioJpa jpa, EntityManager em) {
        super(jpa, RegistroAcessoServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.jpa = Objects.requireNonNull(jpa);
        this.em = Objects.requireNonNull(em);
    }

    @Override
    protected Long paraIdentificadorJpa(RegistroAcessoIdentificador identificador) {
        return Long.valueOf(identificador.toString());
    }

    @Override
    protected RegistroAcessoJpa paraJpa(RegistroAcesso agregado) {
        return new RegistroAcessoJpa(agregado.getIdentificador().map(this::paraIdentificadorJpa).orElse(null),
                em.getReference(AlunoJpa.class, Long.valueOf(agregado.getAluno().toString())), agregado.getEntrada(),
                agregado.getSaida().orElse(null), agregado.isVerificar());
    }

    @Override
    protected RegistroAcesso paraAgregado(RegistroAcessoJpa jpaRegistro) {
        return new RegistroAcesso(Optional.of(new RegistroAcessoIdentificador(jpaRegistro.getIdentificador().toString())),
                new AlunoIdentificador(jpaRegistro.getAluno().getIdentificador().toString()), jpaRegistro.getEntrada(),
                Optional.ofNullable(jpaRegistro.getSaida()), jpaRegistro.isVerificar());
    }

    /** Grava de imediato para que a restrição de uma única entrada aberta por aluno seja verificada no comando. */
    @Override
    public RegistroAcesso incluir(RegistroAcesso agregado) {
        Objects.requireNonNull(agregado);
        return paraAgregado(jpa.saveAndFlush(paraJpa(agregado)));
    }

    @Override
    public void alterar(RegistroAcesso agregado) {
        Objects.requireNonNull(agregado);
        jpa.saveAndFlush(paraJpa(agregado));
    }

    @Override
    public Optional<RegistroAcesso> bloquearAbertoDoAluno(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        return jpa.bloquearAbertoDoAluno(Long.valueOf(aluno.toString())).map(this::paraAgregado);
    }

    @Override
    public long contarPresentesComBloqueio(AcademiaIdentificador academia) {
        Objects.requireNonNull(academia);
        Long identificador = Long.valueOf(academia.toString());
        em.find(AcademiaJpa.class, identificador, LockModeType.PESSIMISTIC_WRITE);
        return jpa.contarPresentes(identificador);
    }
}
