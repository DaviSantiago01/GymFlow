package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.spi.IAlunoRepositorio;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
public final class AlunoRepositorio extends AgregadoRepositorioAbstrato<AlunoIdentificador,Aluno,Long,AlunoJpa> implements IAlunoRepositorio {
    private final IAlunoRepositorioJpa jpa;
    public boolean existePorUsuario(String usuario) { return jpa.existsByUsuario(usuario); }
    public AlunoRepositorio(IAlunoRepositorioJpa jpa) { super(jpa,"Aluno %s inexistente"); this.jpa=java.util.Objects.requireNonNull(jpa); }
    protected Long paraIdentificadorJpa(AlunoIdentificador id) { return Long.valueOf(id.toString()); }
    protected AlunoJpa paraJpa(Aluno a) { return new AlunoJpa(a.getIdentificador().map(this::paraIdentificadorJpa).orElse(null),a.getUsuario(),a.getNome()); }
    protected Aluno paraAgregado(AlunoJpa a) { return new Aluno(Optional.of(new AlunoIdentificador(a.getIdentificador().toString())),a.getUsuario(),a.getNome()); }
    public Aluno buscarPorUsuario(String usuario) { return paraAgregado(jpa.findByUsuario(usuario).orElseThrow(() -> new NaoAutorizadoExcecao("Usuário sem cadastro de aluno"))); }
    public Aluno bloquearPorUsuario(String usuario) { return paraAgregado(jpa.bloquear(usuario).orElseThrow(() -> new NaoAutorizadoExcecao("Usuário sem cadastro de aluno"))); }
}
