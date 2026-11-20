package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.registroacesso;

import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso.RegistroAcesso;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="RAC_REGISTRO_ACESSO")
public class RegistroAcessoJpa implements IAgregadoJpa<RegistroAcesso> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="RAC_ID")
    private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID", updatable=false) private AlunoJpa aluno;
    @NotNull @Column(name="RAC_ENTRADA", updatable=false) private Instant entrada;
    @Column(name="RAC_SAIDA") private @Nullable Instant saida;
    @Column(name="RAC_VERIFICAR") private boolean verificar;

    protected RegistroAcessoJpa() { }

    public RegistroAcessoJpa(@Nullable Long identificador, AlunoJpa aluno, Instant entrada, @Nullable Instant saida,
            boolean verificar) {
        this.identificador = identificador;
        this.aluno = aluno;
        this.entrada = entrada;
        this.saida = saida;
        this.verificar = verificar;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public AlunoJpa getAluno() { return aluno; }
    public Instant getEntrada() { return entrada; }
    public @Nullable Instant getSaida() { return saida; }
    public boolean isVerificar() { return verificar; }
}
