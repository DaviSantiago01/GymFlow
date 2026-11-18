package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.problemaequipamento;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento.DadosOperacionais;

public record DadosProblema(String titulo, String relato, GravidadeProblema gravidade, boolean risco,
    Optional<String> evidencia, LocalDate dataRelato)
        implements dev.sauloaraujo.d5.framework.dominio.IObjetoValor {
    public DadosProblema {
        titulo = DadosOperacionais.texto(titulo, 100, "Título");
        relato = DadosOperacionais.texto(relato, 800, "Relato");
        Objects.requireNonNull(gravidade);
        evidencia = DadosOperacionais.textoOpcional(evidencia, 180, "Evidência");
        Objects.requireNonNull(dataRelato);
    }

    public boolean exigeInterdicao() {
        return risco || gravidade == GravidadeProblema.CRITICA;
    }
}
