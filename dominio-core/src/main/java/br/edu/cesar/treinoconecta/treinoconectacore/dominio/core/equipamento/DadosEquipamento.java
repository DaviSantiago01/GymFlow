package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.equipamento;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

public record DadosEquipamento(String patrimonio, Optional<String> serie, String nome, TipoEquipamento tipo,
    String marca, String modelo, LocalDate aquisicao, int periodicidade)
        implements dev.sauloaraujo.d5.framework.dominio.IObjetoValor {
    public DadosEquipamento {
        patrimonio = DadosOperacionais.texto(patrimonio.toUpperCase(Locale.ROOT), 24, "Patrimônio");
        serie = DadosOperacionais.textoOpcional(serie.map(s -> s.toUpperCase(Locale.ROOT)), 40, "Série");
        nome = DadosOperacionais.texto(nome, 100, "Nome");
        Objects.requireNonNull(tipo);
        marca = DadosOperacionais.texto(marca, 60, "Marca");
        modelo = DadosOperacionais.texto(modelo, 60, "Modelo");
        Objects.requireNonNull(aquisicao);
        Validate.isTrue(periodicidade > 0, "A periodicidade deve ser maior que zero");
    }
}
