package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aula;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aula.Aula;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;

@Entity @Table(name="AUL_AULA")
public class AulaJpa implements IAgregadoJpa<Aula> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="AUL_ID") private @Nullable Long identificador;
    @Column(name="AUL_TITULO", nullable=false, length=100) private String titulo;
    @Column(name="AUL_MODALIDADE", nullable=false, length=60) private String modalidade;
    @Column(name="AUL_DESCRICAO", length=500) private @Nullable String descricao;
    @Column(name="AUL_DATA", nullable=false) private LocalDate data;
    @Column(name="AUL_INICIO", nullable=false) private LocalTime inicio;
    @Column(name="AUL_FIM", nullable=false) private LocalTime fim;
    @Column(name="AUL_LIMITE", nullable=false) private int limite;
    @Column(name="AUL_STATUS", nullable=false, length=12) private String status;
    @Column(name="PRF_ID", nullable=false) private Long professor;
    @Column(name="AUL_CRIACAO", nullable=false) private OffsetDateTime criacao;
    protected AulaJpa() { }
    public AulaJpa(@Nullable Long id, String titulo, String modalidade, @Nullable String descricao, LocalDate data,
            LocalTime inicio, LocalTime fim, int limite, String status, Long professor, OffsetDateTime criacao) {
        this.identificador=id; this.titulo=Objects.requireNonNull(titulo); this.modalidade=Objects.requireNonNull(modalidade);
        this.descricao=descricao; this.data=Objects.requireNonNull(data); this.inicio=Objects.requireNonNull(inicio);
        this.fim=Objects.requireNonNull(fim); this.limite=limite; this.status=Objects.requireNonNull(status);
        this.professor=Objects.requireNonNull(professor); this.criacao=Objects.requireNonNull(criacao);
    }
    public Long getIdentificador() { return Objects.requireNonNull(identificador); }
    public String getTitulo(){return titulo;} public String getModalidade(){return modalidade;} public @Nullable String getDescricao(){return descricao;}
    public LocalDate getData(){return data;} public LocalTime getInicio(){return inicio;} public LocalTime getFim(){return fim;}
    public int getLimite(){return limite;} public String getStatus(){return status;} public Long getProfessor(){return professor;}
    public OffsetDateTime getCriacao(){return criacao;}
}
