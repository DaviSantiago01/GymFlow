package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aula;

import java.time.OffsetDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.*;

@Entity @Table(name="AIN_INSCRICAO_AULA", uniqueConstraints=@UniqueConstraint(name="UK_AIN_AULA_ALUNO", columnNames={"AUL_ID","ALU_ID"}))
public class AulaInscricaoJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="AIN_ID") private @Nullable Long identificador;
    @Column(name="AUL_ID", nullable=false) private Long aula;
    @Column(name="ALU_ID", nullable=false) private Long aluno;
    @Column(name="AIN_STATUS", nullable=false, length=24) private String status;
    @Column(name="AIN_REGISTRO", nullable=false) private OffsetDateTime registro;
    @Column(name="AIN_CANCELAMENTO") private @Nullable OffsetDateTime cancelamento;
    protected AulaInscricaoJpa() { }
    public AulaInscricaoJpa(Long aula, Long aluno, String status, OffsetDateTime registro) {
        this.aula=Objects.requireNonNull(aula); this.aluno=Objects.requireNonNull(aluno); this.status=Objects.requireNonNull(status); this.registro=Objects.requireNonNull(registro);
    }
    public Long getIdentificador(){return Objects.requireNonNull(identificador);} public Long getAula(){return aula;} public Long getAluno(){return aluno;}
    public String getStatus(){return status;} public OffsetDateTime getRegistro(){return registro;} public @Nullable OffsetDateTime getCancelamento(){return cancelamento;}
    public void cancelar(String novoStatus, OffsetDateTime instante){status=Objects.requireNonNull(novoStatus);cancelamento=Objects.requireNonNull(instante);}
    public void agendar(OffsetDateTime instante){status="AGENDADO";registro=Objects.requireNonNull(instante);cancelamento=null;}
}
