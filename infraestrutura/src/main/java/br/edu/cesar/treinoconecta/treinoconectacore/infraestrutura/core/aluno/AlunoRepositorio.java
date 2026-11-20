package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.academia.AcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia.AcademiaJpa;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;
import jakarta.persistence.EntityManager;

public final class AlunoRepositorio extends AgregadoRepositorioAbstrato<AlunoIdentificador, Aluno, Long, AlunoJpa>
        implements IAlunoRepositorio {
    private final IAlunoRepositorioJpa jpa;
    private final EntityManager em;

    public AlunoRepositorio(IAlunoRepositorioJpa jpa, EntityManager em) {
        super(jpa, AlunoServico.IDENTIFICADOR_INEXISTENTE_MODELO);
        this.jpa = Objects.requireNonNull(jpa);
        this.em = Objects.requireNonNull(em);
    }

    @Override
    public boolean existePorUsuario(String usuario) { return jpa.existsByUsuario(usuario); }

    @Override
    protected Long paraIdentificadorJpa(AlunoIdentificador id) { return Long.valueOf(id.toString()); }

    @Override
    protected AlunoJpa paraJpa(Aluno a) {
        var responsavel = a.getResponsavel();
        var bloqueio = a.getBloqueio();
        return new AlunoJpa(a.getIdentificador().map(this::paraIdentificadorJpa).orElse(null), a.getUsuario(), a.getNome(),
                em.getReference(AcademiaJpa.class, Long.valueOf(a.getAcademia().toString())),
                a.getCpf().map(Cpf::toString).orElse(null), a.getNascimento().orElse(null),
                a.getEmail().map(Email::toString).orElse(null), vazioComoNulo(a.getTelefone()),
                vazioComoNulo(a.getEndereco()), responsavel.map(ResponsavelLegal::nome).orElse(null),
                responsavel.map(item -> item.cpf().toString()).orElse(null),
                responsavel.map(ResponsavelLegal::telefone).map(AlunoRepositorio::vazioComoNulo).orElse(null), a.isAtivo(),
                bloqueio.map(BloqueioAluno::motivo).orElse(null), bloqueio.map(BloqueioAluno::inicio).orElse(null));
    }

    @Override
    protected Aluno paraAgregado(AlunoJpa a) {
        Optional<ResponsavelLegal> responsavel = Optional.empty();
        if (a.getResponsavelNome() != null && a.getResponsavelCpf() != null) {
            responsavel = Optional.of(new ResponsavelLegal(a.getResponsavelNome(), new Cpf(a.getResponsavelCpf()),
                    Objects.requireNonNullElse(a.getResponsavelTelefone(), "")));
        }
        Optional<BloqueioAluno> bloqueio = Optional.empty();
        if (a.getBloqueioMotivo() != null && a.getBloqueioInicio() != null) {
            bloqueio = Optional.of(new BloqueioAluno(a.getBloqueioMotivo(), a.getBloqueioInicio()));
        }
        return new Aluno(Optional.of(new AlunoIdentificador(a.getIdentificador().toString())), a.getUsuario(), a.getNome(),
                new AcademiaIdentificador(a.getAcademia().getIdentificador().toString()),
                Optional.ofNullable(a.getCpf()).map(Cpf::new), Optional.ofNullable(a.getNascimento()),
                Optional.ofNullable(a.getEmail()).map(Email::new), Objects.requireNonNullElse(a.getTelefone(), ""),
                Objects.requireNonNullElse(a.getEndereco(), ""), responsavel, a.isAtivo(), bloqueio);
    }

    /** Grava de imediato para que as restrições de CPF e e-mail únicos sejam verificadas dentro do comando. */
    @Override
    public void alterar(Aluno agregado) {
        Objects.requireNonNull(agregado);
        jpa.saveAndFlush(paraJpa(agregado));
    }

    @Override
    public Aluno buscarPorUsuario(String usuario) {
        return paraAgregado(jpa.findByUsuario(usuario).orElseThrow(() -> new NaoAutorizadoExcecao("Usuário sem cadastro de aluno")));
    }

    @Override
    public Aluno bloquearPorUsuario(String usuario) {
        return paraAgregado(jpa.bloquear(usuario).orElseThrow(() -> new NaoAutorizadoExcecao("Usuário sem cadastro de aluno")));
    }

    @Override
    public Aluno bloquear(AlunoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return paraAgregado(jpa.bloquearPorIdentificador(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(AlunoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador))));
    }

    @Override
    public boolean existePorCpf(Cpf cpf, Optional<AlunoIdentificador> identificadorIgnorado) {
        Objects.requireNonNull(cpf);
        return jpa.existePorCpf(cpf.toString(), identificadorIgnorado.map(this::paraIdentificadorJpa).orElse(null));
    }

    @Override
    public boolean existePorEmail(Email email, Optional<AlunoIdentificador> identificadorIgnorado) {
        Objects.requireNonNull(email);
        return jpa.existePorEmail(email.toString(), identificadorIgnorado.map(this::paraIdentificadorJpa).orElse(null));
    }

    @Override
    public void excluirDefinitivamente(AlunoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        var aluno = jpa.findById(paraIdentificadorJpa(identificador)).orElseThrow(
                () -> new NoSuchElementException(AlunoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador)));
        em.createNativeQuery("delete from AHI_ALUNO_HISTORICO where ALU_ID = ?").setParameter(1, aluno.getIdentificador())
                .executeUpdate();
        em.createNativeQuery("delete from USU_CREDENCIAL where ALU_USUARIO = ?").setParameter(1, aluno.getUsuario())
                .executeUpdate();
        jpa.delete(aluno);
        jpa.flush();
    }

    private static @org.jspecify.annotations.Nullable String vazioComoNulo(String valor) {
        return valor.isBlank() ? null : valor;
    }
}
