package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.time.Instant;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.TipoAlteracaoAluno;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/** Linha do histórico de alterações do aluno (F005, RN011): só é incluída, nunca alterada. */
@Entity @Table(name="AHI_ALUNO_HISTORICO")
public class HistoricoAlunoJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="AHI_ID")
    private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID", updatable=false) private AlunoJpa aluno;
    @NotNull @Column(name="AHI_INSTANTE", updatable=false) private Instant instante;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="AHI_TIPO", updatable=false) private TipoAlteracaoAluno tipo;
    @NotNull @Size(max=250) @Column(name="AHI_DESCRICAO", updatable=false) private String descricao;
    @NotNull @Size(max=100) @Column(name="AHI_RESPONSAVEL", updatable=false) private String responsavel;

    protected HistoricoAlunoJpa() { }

    public HistoricoAlunoJpa(AlunoJpa aluno, Instant instante, TipoAlteracaoAluno tipo, String descricao, String responsavel) {
        this.aluno = aluno;
        this.instante = instante;
        this.tipo = tipo;
        this.descricao = descricao;
        this.responsavel = responsavel;
    }
}
