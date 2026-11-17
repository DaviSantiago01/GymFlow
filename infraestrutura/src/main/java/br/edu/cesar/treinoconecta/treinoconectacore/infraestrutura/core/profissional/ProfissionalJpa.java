package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.profissional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cpf;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Cref;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.Profissional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.SituacaoProfissional;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="PRF_PROFISSIONAL")
public class ProfissionalJpa implements IAgregadoJpa<Profissional> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="PRF_ID")
    private @Nullable Long identificador;
    @NotNull @Size(max=Profissional.TAMANHO_NOME) @Column(name="PRF_NOME") private String nome;
    @NotNull @Size(max=Cpf.TAMANHO) @Column(name="PRF_CPF") private String cpf;
    @NotNull @Size(max=Cref.TAMANHO) @Column(name="PRF_CREF") private String cref;
    @NotNull @Size(max=Profissional.TAMANHO_CONTATO) @Column(name="PRF_CONTATO") private String contato;
    @Column(name="PRF_PROFESSOR") private boolean professor;
    @Column(name="PRF_PERSONAL_TRAINER") private boolean personalTrainer;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="PRF_SITUACAO") private SituacaoProfissional situacao;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="PES_PROFISSIONAL_ESPECIALIDADE", joinColumns=@JoinColumn(name="PRF_ID"))
    private Set<EspecialidadeJpa> especialidades = new HashSet<>();
    @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
    @JoinColumn(name="PRF_ID", nullable=false)
    @OrderBy("inicio")
    private List<DisponibilidadeProfissionalJpa> disponibilidades = new ArrayList<>();

    protected ProfissionalJpa() { }

    public ProfissionalJpa(@Nullable Long identificador, String nome, String cpf, String cref, String contato,
            boolean professor, boolean personalTrainer, SituacaoProfissional situacao, Set<EspecialidadeJpa> especialidades,
            List<DisponibilidadeProfissionalJpa> disponibilidades) {
        this.identificador = identificador;
        this.nome = nome;
        this.cpf = cpf;
        this.cref = cref;
        this.contato = contato;
        this.professor = professor;
        this.personalTrainer = personalTrainer;
        this.situacao = situacao;
        this.especialidades = especialidades;
        this.disponibilidades = disponibilidades;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getCref() { return cref; }
    public String getContato() { return contato; }
    public boolean isProfessor() { return professor; }
    public boolean isPersonalTrainer() { return personalTrainer; }
    public SituacaoProfissional getSituacao() { return situacao; }
    public Set<EspecialidadeJpa> getEspecialidades() { return especialidades; }
    public List<DisponibilidadeProfissionalJpa> getDisponibilidades() { return disponibilidades; }
}
