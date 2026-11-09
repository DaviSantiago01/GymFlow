package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.avaliacaofisica;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.SituacaoAvaliacao;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.aluno.AlunoJpa;
import br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.personal.PersonalJpa;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity @Table(name="AVF_AVALIACAO_FISICA")
public class AvaliacaoFisicaJpa implements IAgregadoJpa<AvaliacaoFisica> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="AVF_ID")
    private @Nullable Long identificador;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ALU_ID") private AlunoJpa aluno;
    @NotNull @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="PER_ID") private PersonalJpa personal;
    @NotNull @Column(name="AVF_DATA") private LocalDate data;
    @NotNull @Column(name="AVF_PESO") private BigDecimal peso;
    @NotNull @Column(name="AVF_ALTURA") private BigDecimal altura;
    @NotNull @Column(name="AVF_IMC") private BigDecimal imc;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="AVF_SITUACAO") private SituacaoAvaliacao situacao;
    @Column(name="AVF_VERSAO") private int versao;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="AVF_AVALIACAO_ANTERIOR") private @Nullable AvaliacaoFisicaJpa avaliacaoAnterior;
    @NotNull @Column(name="AVF_REGISTRO") private Instant registro;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="MDC_MEDIDA_CORPORAL", joinColumns=@JoinColumn(name="AVF_ID"))
    @OrderColumn(name="MDC_INDICE")
    private List<MedidaCorporalJpa> medidas = new ArrayList<>();

    protected AvaliacaoFisicaJpa() { }

    public AvaliacaoFisicaJpa(@Nullable Long identificador, AlunoJpa aluno, PersonalJpa personal, LocalDate data, BigDecimal peso,
            BigDecimal altura, BigDecimal imc, SituacaoAvaliacao situacao, int versao,
            @Nullable AvaliacaoFisicaJpa avaliacaoAnterior, Instant registro, List<MedidaCorporalJpa> medidas) {
        this.identificador = identificador;
        this.aluno = aluno;
        this.personal = personal;
        this.data = data;
        this.peso = peso;
        this.altura = altura;
        this.imc = imc;
        this.situacao = situacao;
        this.versao = versao;
        this.avaliacaoAnterior = avaliacaoAnterior;
        this.registro = registro;
        this.medidas = medidas;
    }

    /** Os dados de uma versão nunca mudam: só a situação é atualizada. */
    public void atualizarSituacao(SituacaoAvaliacao novaSituacao) {
        this.situacao = novaSituacao;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public AlunoJpa getAluno() { return aluno; }
    public PersonalJpa getPersonal() { return personal; }
    public LocalDate getData() { return data; }
    public BigDecimal getPeso() { return peso; }
    public BigDecimal getAltura() { return altura; }
    public BigDecimal getImc() { return imc; }
    public SituacaoAvaliacao getSituacao() { return situacao; }
    public int getVersao() { return versao; }
    public @Nullable AvaliacaoFisicaJpa getAvaliacaoAnterior() { return avaliacaoAnterior; }
    public Instant getRegistro() { return registro; }
    public List<MedidaCorporalJpa> getMedidas() { return medidas; }
}
