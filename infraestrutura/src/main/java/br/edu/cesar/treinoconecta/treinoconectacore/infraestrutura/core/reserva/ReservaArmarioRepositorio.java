package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.reserva;

import java.time.Instant;
import java.util.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.armario.ArmarioCodigo;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.*;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.reserva.spi.IReservaArmarioRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.IAlunoRepositorioJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.armario.IArmarioRepositorioJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoRepositorioAbstrato;
public final class ReservaArmarioRepositorio extends AgregadoRepositorioAbstrato<ReservaArmarioIdentificador,ReservaArmario,Long,ReservaArmarioJpa> implements IReservaArmarioRepositorio {
    private final IReservaArmarioRepositorioJpa jpa;
    private final IAlunoRepositorioJpa alunos;
    private final IArmarioRepositorioJpa armarios;
    public ReservaArmarioRepositorio(IReservaArmarioRepositorioJpa jpa, IAlunoRepositorioJpa alunos, IArmarioRepositorioJpa armarios) {
        super(jpa,"Reserva %s inexistente"); this.jpa=Objects.requireNonNull(jpa);
        this.alunos=Objects.requireNonNull(alunos); this.armarios=Objects.requireNonNull(armarios);
    }
    protected Long paraIdentificadorJpa(ReservaArmarioIdentificador id) { return Long.valueOf(id.toString()); }
    protected ReservaArmarioJpa paraJpa(ReservaArmario r) {
        return new ReservaArmarioJpa(r.getIdentificador().map(this::paraIdentificadorJpa).orElse(null),
                alunos.getReferenceById(Long.valueOf(r.getAluno().toString())),armarios.getReferenceById(r.getArmario().toString()),
                r.getConfirmacao(),r.getInicio(),r.getTermino(),r.getEncerramento().orElse(null),r.getSituacao());
    }
    protected ReservaArmario paraAgregado(ReservaArmarioJpa r) {
        return new ReservaArmario(Optional.of(new ReservaArmarioIdentificador(r.getIdentificador().toString())),
                new AlunoIdentificador(r.getAluno().getIdentificador().toString()),new ArmarioCodigo(r.getArmario().getIdentificador()),
                r.getConfirmacao(),r.getInicio(),r.getTermino(),Optional.ofNullable(r.getEncerramento()),r.getSituacao());
    }
    public boolean existeAtivaPorAluno(AlunoIdentificador aluno, Instant agora) { return jpa.existePorAluno(Long.valueOf(aluno.toString()),agora); }
    public boolean existeSobreposicao(ArmarioCodigo armario, Instant inicio, Instant termino) { return jpa.existeSobreposicao(armario.toString(),inicio,termino); }
    public ReservaArmario bloquear(ReservaArmarioIdentificador id) { return paraAgregado(jpa.bloquear(paraIdentificadorJpa(id)).orElseThrow(() -> new NoSuchElementException("Reserva inexistente"))); }
    public void vencerRelacionadas(AlunoIdentificador aluno, ArmarioCodigo armario, Instant agora) { jpa.vencerRelacionadas(Long.valueOf(aluno.toString()),armario.toString(),agora); }
    public int vencerTodas(Instant agora) { return jpa.vencerTodas(agora); }
}
