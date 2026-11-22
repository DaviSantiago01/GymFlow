package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.desafio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.profissional.ProfissionalIdentificador;

public record AvaliacaoDesafio(Optional<Long> identificador, DesafioIdentificador desafio, AlunoIdentificador aluno,
        ProfissionalIdentificador profissional, TipoAvaliacaoDesafio tipo, BigDecimal peso, BigDecimal cintura,
        BigDecimal gordura, OffsetDateTime registro) {
    public AvaliacaoDesafio {
        Objects.requireNonNull(identificador); Objects.requireNonNull(desafio); Objects.requireNonNull(aluno); Objects.requireNonNull(profissional);
        Objects.requireNonNull(tipo); Objects.requireNonNull(peso); Objects.requireNonNull(cintura);
        Objects.requireNonNull(gordura); Objects.requireNonNull(registro);
        if (peso.signum()<=0 || cintura.signum()<=0 || gordura.signum()<=0 || gordura.compareTo(BigDecimal.valueOf(100))>=0)
            throw new IllegalArgumentException("Peso e cintura devem ser positivos; gordura corporal deve estar entre 0 e 100.");
    }
    public AvaliacaoDesafio alterar(BigDecimal novoPeso,BigDecimal novaCintura,BigDecimal novaGordura) {
        return new AvaliacaoDesafio(identificador,desafio,aluno,profissional,tipo,novoPeso,novaCintura,novaGordura,registro);
    }
}
