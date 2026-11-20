package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.registroacesso;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/**
 * Passagem do aluno pela academia: a entrada abre o registro e a saída o encerra (F006, RN006 e RN007). O tempo de
 * permanência é calculado a partir dos dois horários (RN008) e o registro é marcado para verificação quando passa do
 * limite configurado (RN009).
 */
public final class RegistroAcesso implements IAgregado<RegistroAcessoIdentificador> {
    private final Optional<RegistroAcessoIdentificador> identificador;
    private final AlunoIdentificador aluno;
    private final Instant entrada;
    private final Optional<Instant> saida;
    private final boolean verificar;

    public RegistroAcesso(Optional<RegistroAcessoIdentificador> identificador, AlunoIdentificador aluno, Instant entrada,
            Optional<Instant> saida, boolean verificar) {
        this.identificador = Objects.requireNonNull(identificador);
        this.aluno = Objects.requireNonNull(aluno);
        this.entrada = Objects.requireNonNull(entrada);
        this.saida = Objects.requireNonNull(saida);
        saida.ifPresent(data -> Validate.isTrue(!data.isBefore(entrada), "A saída não pode anteceder a entrada"));
        this.verificar = verificar;
    }

    public static RegistroAcesso entrar(AlunoIdentificador aluno, Instant agora) {
        return new RegistroAcesso(Optional.empty(), aluno, agora, Optional.empty(), false);
    }

    public RegistroAcesso sair(Instant agora, Duration limite) {
        Objects.requireNonNull(agora);
        Validate.validState(aberto(), RegistroAcessoServico.SEM_ENTRADA_ABERTA);
        Objects.requireNonNull(limite);
        var momento = agora.isBefore(entrada) ? entrada : agora;
        return new RegistroAcesso(identificador, aluno, entrada, Optional.of(momento),
                Duration.between(entrada, momento).compareTo(limite) > 0);
    }

    public boolean aberto() {
        return saida.isEmpty();
    }

    /** Até a saída, a permanência é contada até o instante informado. */
    public Duration permanencia(Instant agora) {
        Objects.requireNonNull(agora);
        var fim = saida.orElse(agora);
        return fim.isBefore(entrada) ? Duration.ZERO : Duration.between(entrada, fim);
    }

    public boolean excedeuLimite(Instant agora, Duration limite) {
        Objects.requireNonNull(limite);
        return verificar || permanencia(agora).compareTo(limite) > 0;
    }

    public Optional<RegistroAcessoIdentificador> getIdentificador() { return identificador; }
    public AlunoIdentificador getAluno() { return aluno; }
    public Instant getEntrada() { return entrada; }
    public Optional<Instant> getSaida() { return saida; }
    public boolean isVerificar() { return verificar; }
}
