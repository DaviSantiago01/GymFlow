package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.treino;

import jakarta.persistence.*;
@Entity @Table(name="VAT_TREINO_AVALIADO")
public class TreinoAvaliadoJpa {
    @Id @Column(name="TRN_ID") private Long identificador;
    protected TreinoAvaliadoJpa(){}
}
