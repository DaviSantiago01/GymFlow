package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno;

import java.time.Instant;
import java.time.LocalDate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Aluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.BloqueioAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.DadosAluno;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.Email;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.ResponsavelLegal;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia.AcademiaJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="ALU_ALUNO")
public class AlunoJpa implements IAgregadoJpa<Aluno> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ALU_ID")
    private @Nullable Long identificador;
    @NotNull @Size(max=100) @Column(name="ALU_USUARIO",unique=true) private String usuario;
    @NotNull @Size(max=100) @Column(name="ALU_NOME") private String nome;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="ACD_ID") private AcademiaJpa academia;
    @Size(max=Cpf.TAMANHO) @Column(name="ALU_CPF",unique=true) private @Nullable String cpf;
    @Column(name="ALU_NASCIMENTO") private @Nullable LocalDate nascimento;
    @Size(max=Email.TAMANHO) @Column(name="ALU_EMAIL",unique=true) private @Nullable String email;
    @Size(max=DadosAluno.TAMANHO_TELEFONE) @Column(name="ALU_TELEFONE") private @Nullable String telefone;
    @Size(max=DadosAluno.TAMANHO_ENDERECO) @Column(name="ALU_ENDERECO") private @Nullable String endereco;
    @Size(max=ResponsavelLegal.TAMANHO_NOME) @Column(name="ALU_RESPONSAVEL_NOME") private @Nullable String responsavelNome;
    @Size(max=Cpf.TAMANHO) @Column(name="ALU_RESPONSAVEL_CPF") private @Nullable String responsavelCpf;
    @Size(max=ResponsavelLegal.TAMANHO_TELEFONE) @Column(name="ALU_RESPONSAVEL_TELEFONE") private @Nullable String responsavelTelefone;
    @Column(name="ALU_ATIVO") private boolean ativo = true;
    @Size(max=BloqueioAluno.TAMANHO_MOTIVO) @Column(name="ALU_BLOQUEIO_MOTIVO") private @Nullable String bloqueioMotivo;
    @Column(name="ALU_BLOQUEIO_INICIO") private @Nullable Instant bloqueioInicio;

    protected AlunoJpa() { }

    public AlunoJpa(@Nullable Long identificador, String usuario, String nome, AcademiaJpa academia) {
        this(identificador, usuario, nome, academia, null, null, null, null, null, null, null, null, true, null, null);
    }

    public AlunoJpa(@Nullable Long identificador, String usuario, String nome, AcademiaJpa academia, @Nullable String cpf,
            @Nullable LocalDate nascimento, @Nullable String email, @Nullable String telefone, @Nullable String endereco,
            @Nullable String responsavelNome, @Nullable String responsavelCpf, @Nullable String responsavelTelefone,
            boolean ativo, @Nullable String bloqueioMotivo, @Nullable Instant bloqueioInicio) {
        this.identificador = identificador;
        this.usuario = usuario;
        this.nome = nome;
        this.academia = academia;
        this.cpf = cpf;
        this.nascimento = nascimento;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.responsavelNome = responsavelNome;
        this.responsavelCpf = responsavelCpf;
        this.responsavelTelefone = responsavelTelefone;
        this.ativo = ativo;
        this.bloqueioMotivo = bloqueioMotivo;
        this.bloqueioInicio = bloqueioInicio;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public String getUsuario() { return usuario; }
    public AcademiaJpa getAcademia() { return academia; }
    public String getNome() { return nome; }
    public @Nullable String getCpf() { return cpf; }
    public @Nullable LocalDate getNascimento() { return nascimento; }
    public @Nullable String getEmail() { return email; }
    public @Nullable String getTelefone() { return telefone; }
    public @Nullable String getEndereco() { return endereco; }
    public @Nullable String getResponsavelNome() { return responsavelNome; }
    public @Nullable String getResponsavelCpf() { return responsavelCpf; }
    public @Nullable String getResponsavelTelefone() { return responsavelTelefone; }
    public boolean isAtivo() { return ativo; }
    public @Nullable String getBloqueioMotivo() { return bloqueioMotivo; }
    public @Nullable Instant getBloqueioInicio() { return bloqueioInicio; }
}
