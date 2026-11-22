package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.desafio;

import java.time.LocalDate;import java.time.OffsetDateTime;import java.util.Objects;import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio.Desafio;import dev.sauloaraujo.d5.framework.infraestrutura.IAgregadoJpa;import jakarta.persistence.*;

@Entity @Table(name="DES_DESAFIO")
public class DesafioJpa implements IAgregadoJpa<Desafio> {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="DES_ID") private @Nullable Long identificador;
 @Column(name="DES_TITULO",nullable=false,length=100) private String titulo;@Column(name="DES_INDICADOR",nullable=false,length=20) private String indicador;
 @Column(name="DES_INICIO",nullable=false) private LocalDate inicio;@Column(name="DES_FIM",nullable=false) private LocalDate fim;
 @Column(name="DES_STATUS",nullable=false,length=12) private String status;@Column(name="DES_CRIACAO",nullable=false) private OffsetDateTime criacao;
 protected DesafioJpa(){}
 public DesafioJpa(@Nullable Long id,String titulo,String indicador,LocalDate inicio,LocalDate fim,String status,OffsetDateTime criacao){this.identificador=id;this.titulo=Objects.requireNonNull(titulo);this.indicador=Objects.requireNonNull(indicador);this.inicio=Objects.requireNonNull(inicio);this.fim=Objects.requireNonNull(fim);this.status=Objects.requireNonNull(status);this.criacao=Objects.requireNonNull(criacao);}
 public Long getIdentificador(){return Objects.requireNonNull(identificador);}public String getTitulo(){return titulo;}public String getIndicador(){return indicador;}public LocalDate getInicio(){return inicio;}public LocalDate getFim(){return fim;}public String getStatus(){return status;}public OffsetDateTime getCriacao(){return criacao;}
}
