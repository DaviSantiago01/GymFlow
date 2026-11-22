package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.desafio;
import java.math.BigDecimal;import java.time.OffsetDateTime;import java.util.Objects;import org.jspecify.annotations.Nullable;import jakarta.persistence.*;
@Entity @Table(name="DIN_INSCRICAO_DESAFIO",uniqueConstraints=@UniqueConstraint(name="UK_DIN_DESAFIO_ALUNO",columnNames={"DES_ID","ALU_ID"}))
public class DesafioInscricaoJpa {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="DIN_ID")private @Nullable Long identificador;@Column(name="DES_ID",nullable=false)private Long desafio;@Column(name="ALU_ID",nullable=false)private Long aluno;@Column(name="DIN_INDICADOR",nullable=false,length=20)private String indicador;@Column(name="DIN_META",nullable=false,precision=8,scale=2)private BigDecimal meta;@Column(name="DIN_AVALIACAO_INICIAL",nullable=false)private Long avaliacaoInicial;@Column(name="DIN_REGISTRO",nullable=false)private OffsetDateTime registro;
 protected DesafioInscricaoJpa(){}
 public DesafioInscricaoJpa(Long desafio,Long aluno,String indicador,BigDecimal meta,Long avaliacao,OffsetDateTime registro){this.desafio=Objects.requireNonNull(desafio);this.aluno=Objects.requireNonNull(aluno);this.indicador=Objects.requireNonNull(indicador);this.meta=Objects.requireNonNull(meta);this.avaliacaoInicial=Objects.requireNonNull(avaliacao);this.registro=Objects.requireNonNull(registro);}
 public Long getIdentificador(){return Objects.requireNonNull(identificador);}public Long getDesafio(){return desafio;}public Long getAluno(){return aluno;}public String getIndicador(){return indicador;}public BigDecimal getMeta(){return meta;}public Long getAvaliacaoInicial(){return avaliacaoInicial;}public OffsetDateTime getRegistro(){return registro;}
}
