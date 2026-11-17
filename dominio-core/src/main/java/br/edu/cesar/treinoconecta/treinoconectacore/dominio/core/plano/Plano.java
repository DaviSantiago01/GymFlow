package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/** Plano comercial da academia: nome, valor cobrado a cada período e duração do período em meses. */
public final class Plano implements IAgregado<PlanoIdentificador> {
    public static final int TAMANHO_NOME = 60;
    public static final int DURACAO_MINIMA_MESES = 1;
    public static final int DURACAO_MAXIMA_MESES = 12;
    public static final BigDecimal VALOR_MAXIMO = new BigDecimal("99999.99");

    private final Optional<PlanoIdentificador> identificador;
    private final String nome;
    private final BigDecimal valor;
    private final int duracaoMeses;
    private final SituacaoPlano situacao;

    public Plano(Optional<PlanoIdentificador> identificador, String nome, BigDecimal valor, int duracaoMeses,
            SituacaoPlano situacao) {
        this.identificador = Objects.requireNonNull(identificador);
        this.nome = normalizarNome(nome);
        Validate.notNull(valor, PlanoServico.VALOR_INVALIDO);
        Validate.isTrue(valor.signum() > 0, PlanoServico.VALOR_INVALIDO);
        Validate.isTrue(valor.compareTo(VALOR_MAXIMO) <= 0, PlanoServico.VALOR_INVALIDO);
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
        Validate.isTrue(duracaoMeses >= DURACAO_MINIMA_MESES && duracaoMeses <= DURACAO_MAXIMA_MESES,
                PlanoServico.DURACAO_INVALIDA);
        this.duracaoMeses = duracaoMeses;
        this.situacao = Objects.requireNonNull(situacao);
    }

    public static Plano cadastrar(String nome, BigDecimal valor, int duracaoMeses) {
        return new Plano(Optional.empty(), nome, valor, duracaoMeses, SituacaoPlano.ATIVO);
    }

    /** Espaços repetidos não diferenciam dois planos com o mesmo nome. */
    public static String normalizarNome(String nome) {
        Validate.notBlank(nome, PlanoServico.NOME_OBRIGATORIO);
        var normalizado = nome.strip().replaceAll("\\s+", " ");
        Validate.isTrue(normalizado.length() >= 2 && normalizado.length() <= TAMANHO_NOME, PlanoServico.NOME_OBRIGATORIO);
        return normalizado;
    }

    public Plano editar(String novoNome, BigDecimal novoValor, int novaDuracaoMeses) {
        validarAtivo();
        return new Plano(identificador, novoNome, novoValor, novaDuracaoMeses, situacao);
    }

    public Plano inativar() {
        validarAtivo();
        return new Plano(identificador, nome, valor, duracaoMeses, SituacaoPlano.INATIVO);
    }

    public boolean ativo() {
        return situacao == SituacaoPlano.ATIVO;
    }

    private void validarAtivo() {
        Validate.validState(ativo(), PlanoServico.PLANO_INATIVO);
    }

    public Optional<PlanoIdentificador> getIdentificador() { return identificador; }
    public String getNome() { return nome; }
    public BigDecimal getValor() { return valor; }
    public int getDuracaoMeses() { return duracaoMeses; }
    public SituacaoPlano getSituacao() { return situacao; }
}
