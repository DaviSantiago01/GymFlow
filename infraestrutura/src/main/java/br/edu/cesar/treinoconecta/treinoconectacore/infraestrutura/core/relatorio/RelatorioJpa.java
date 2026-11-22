package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.relatorio;

import java.time.Instant;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Agrupamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Relatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.SituacaoRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.TipoRelatorio;
import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity @Table(name="RLT_RELATORIO")
public class RelatorioJpa implements IAgregadoJpa<Relatorio> {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="RLT_ID")
    private @Nullable Long identificador;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="RLT_TIPO") private TipoRelatorio tipo;
    @NotNull @Column(name="RLT_INICIO") private LocalDate inicio;
    @NotNull @Column(name="RLT_FIM") private LocalDate fim;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="RLT_AGRUPAMENTO") private Agrupamento agrupamento;
    @Column(name="RLT_COMPARAR") private boolean comparar;
    @Column(name="RLT_FILTRO_PLANO") private @Nullable Long filtroPlano;
    @Column(name="RLT_FILTRO_MODALIDADE") private @Nullable String filtroModalidade;
    @Column(name="RLT_FILTRO_PERSONAL") private @Nullable Long filtroPersonal;
    @Column(name="RLT_FILTRO_AULA") private @Nullable Long filtroAula;
    @NotNull @Column(name="RLT_GERACAO") private Instant geracao;
    @NotNull @Column(name="RLT_USUARIO") private String usuario;
    @NotNull @Column(name="RLT_USUARIO_NOME") private String usuarioNome;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="RLT_PERFIL") private Autorizacao perfil;
    @Column(name="RLT_VERSAO") private int versao;
    @Column(name="RLT_ANTERIOR") private @Nullable Long anterior;
    @NotNull @Enumerated(EnumType.STRING) @Column(name="RLT_SITUACAO") private SituacaoRelatorio situacao;
    @NotNull @Lob @Basic(fetch=FetchType.EAGER) @Column(name="RLT_CONTEUDO") private String conteudo;

    protected RelatorioJpa() { }

    public RelatorioJpa(@Nullable Long identificador, TipoRelatorio tipo, LocalDate inicio, LocalDate fim,
            Agrupamento agrupamento, boolean comparar, @Nullable Long filtroPlano, @Nullable String filtroModalidade,
            @Nullable Long filtroPersonal, @Nullable Long filtroAula, Instant geracao, String usuario, String usuarioNome,
            Autorizacao perfil, int versao, @Nullable Long anterior, SituacaoRelatorio situacao, String conteudo) {
        this.identificador = identificador;
        this.tipo = tipo;
        this.inicio = inicio;
        this.fim = fim;
        this.agrupamento = agrupamento;
        this.comparar = comparar;
        this.filtroPlano = filtroPlano;
        this.filtroModalidade = filtroModalidade;
        this.filtroPersonal = filtroPersonal;
        this.filtroAula = filtroAula;
        this.geracao = geracao;
        this.usuario = usuario;
        this.usuarioNome = usuarioNome;
        this.perfil = perfil;
        this.versao = versao;
        this.anterior = anterior;
        this.situacao = situacao;
        this.conteudo = conteudo;
    }

    public Long getIdentificador() { return java.util.Objects.requireNonNull(identificador); }
    public TipoRelatorio getTipo() { return tipo; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public Agrupamento getAgrupamento() { return agrupamento; }
    public boolean isComparar() { return comparar; }
    public @Nullable Long getFiltroPlano() { return filtroPlano; }
    public @Nullable String getFiltroModalidade() { return filtroModalidade; }
    public @Nullable Long getFiltroPersonal() { return filtroPersonal; }
    public @Nullable Long getFiltroAula() { return filtroAula; }
    public Instant getGeracao() { return geracao; }
    public String getUsuario() { return usuario; }
    public String getUsuarioNome() { return usuarioNome; }
    public Autorizacao getPerfil() { return perfil; }
    public int getVersao() { return versao; }
    public @Nullable Long getAnterior() { return anterior; }
    public SituacaoRelatorio getSituacao() { return situacao; }
    public String getConteudo() { return conteudo; }
}
