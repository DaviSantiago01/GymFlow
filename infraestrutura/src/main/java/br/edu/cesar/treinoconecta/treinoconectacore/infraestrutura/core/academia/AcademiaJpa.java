package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.academia;

import jakarta.persistence.*;
@Entity @Table(name="ACD_ACADEMIA")
public class AcademiaJpa {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ACD_ID") private Long identificador;
    @Column(name="ACD_NOME",nullable=false,length=100) private String nome;
    protected AcademiaJpa(){}
    public Long getIdentificador(){return identificador;} public String getNome(){return nome;}
}
