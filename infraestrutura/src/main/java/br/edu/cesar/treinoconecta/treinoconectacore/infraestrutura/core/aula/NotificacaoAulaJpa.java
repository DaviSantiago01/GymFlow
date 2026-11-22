package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aula;

import java.time.OffsetDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.*;

@Entity @Table(name="NOT_NOTIFICACAO_AULA")
public class NotificacaoAulaJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="NOT_ID") private @Nullable Long identificador;
    @Column(name="AUL_ID", nullable=false) private Long aula;
    @Column(name="ALU_ID", nullable=false) private Long aluno;
    @Column(name="NOT_MENSAGEM", nullable=false, length=300) private String mensagem;
    @Column(name="NOT_REGISTRO", nullable=false) private OffsetDateTime registro;
    @Column(name="NOT_LIDA", nullable=false) private boolean lida;
    protected NotificacaoAulaJpa() { }
    public NotificacaoAulaJpa(Long aula, Long aluno, String mensagem, OffsetDateTime registro) { this.aula=Objects.requireNonNull(aula);this.aluno=Objects.requireNonNull(aluno);this.mensagem=Objects.requireNonNull(mensagem);this.registro=Objects.requireNonNull(registro); }
}
