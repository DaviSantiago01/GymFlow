package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import dev.sauloaraujo.d5.framework.dominio.IAgregado;

/**
 * Relatório gerado e guardado no histórico (RN017). Os valores calculados não mudam depois da geração (RN018): se os
 * registros do período forem alterados, gera-se uma nova versão e a anterior fica marcada como substituída.
 */
public final class Relatorio implements IAgregado<RelatorioIdentificador> {
    public static final int TAMANHO_USUARIO = 100;

    private final Optional<RelatorioIdentificador> identificador;
    private final TipoRelatorio tipo;
    private final PeriodoAnalise periodo;
    private final Agrupamento agrupamento;
    private final FiltrosRelatorio filtros;
    private final boolean comparar;
    private final Instant geracao;
    private final String usuario;
    private final String usuarioNome;
    private final Autorizacao perfil;
    private final int versao;
    private final Optional<RelatorioIdentificador> anterior;
    private final SituacaoRelatorio situacao;
    private final ConteudoRelatorio conteudo;

    public Relatorio(Optional<RelatorioIdentificador> identificador, TipoRelatorio tipo, PeriodoAnalise periodo,
            Agrupamento agrupamento, FiltrosRelatorio filtros, boolean comparar, Instant geracao, String usuario,
            String usuarioNome, Autorizacao perfil, int versao, Optional<RelatorioIdentificador> anterior,
            SituacaoRelatorio situacao, ConteudoRelatorio conteudo) {
        this.identificador = Objects.requireNonNull(identificador);
        this.tipo = Validate.notNull(tipo, RelatorioServico.TIPO_OBRIGATORIO);
        this.periodo = Objects.requireNonNull(periodo);
        this.agrupamento = Validate.notNull(agrupamento, RelatorioServico.AGRUPAMENTO_OBRIGATORIO);
        this.filtros = Objects.requireNonNull(filtros);
        this.comparar = comparar;
        this.geracao = Objects.requireNonNull(geracao);
        this.usuario = Validate.notBlank(usuario, "Usuário obrigatório");
        this.usuarioNome = Validate.notBlank(usuarioNome, "Nome do usuário obrigatório");
        Validate.isTrue(usuario.length() <= TAMANHO_USUARIO && usuarioNome.length() <= TAMANHO_USUARIO,
                "Nome ou usuário muito longo");
        this.perfil = Objects.requireNonNull(perfil);
        Validate.isTrue(versao >= 1, "A versão do relatório deve ser maior que zero");
        this.versao = versao;
        this.anterior = Objects.requireNonNull(anterior);
        Validate.isTrue(anterior.isPresent() == (versao > 1), "Versão anterior do relatório inconsistente");
        this.situacao = Objects.requireNonNull(situacao);
        this.conteudo = Objects.requireNonNull(conteudo);
        filtros.validarPara(tipo);
    }

    public static Relatorio gerar(TipoRelatorio tipo, PeriodoAnalise periodo, Agrupamento agrupamento,
            FiltrosRelatorio filtros, boolean comparar, Instant agora, String usuario, String usuarioNome,
            Autorizacao perfil, ConteudoRelatorio conteudo) {
        return new Relatorio(Optional.empty(), tipo, periodo, agrupamento, filtros, comparar, agora, usuario, usuarioNome,
                perfil, 1, Optional.empty(), SituacaoRelatorio.ATUAL, conteudo);
    }

    /** Nova versão com os mesmos parâmetros e os valores recalculados. Este relatório permanece como estava. */
    public Relatorio novaVersao(ConteudoRelatorio novoConteudo, Instant agora, String novoUsuario, String novoNome,
            Autorizacao novoPerfil) {
        Validate.validState(situacao == SituacaoRelatorio.ATUAL, RelatorioServico.VERSAO_JA_SUBSTITUIDA);
        return new Relatorio(Optional.empty(), tipo, periodo, agrupamento, filtros, comparar, agora, novoUsuario,
                novoNome, novoPerfil, versao + 1, identificador, SituacaoRelatorio.ATUAL, novoConteudo);
    }

    public Relatorio substituir() {
        Validate.validState(situacao == SituacaoRelatorio.ATUAL, RelatorioServico.VERSAO_JA_SUBSTITUIDA);
        return new Relatorio(identificador, tipo, periodo, agrupamento, filtros, comparar, geracao, usuario, usuarioNome,
                perfil, versao, anterior, SituacaoRelatorio.SUBSTITUIDO, conteudo);
    }

    public Optional<RelatorioIdentificador> getIdentificador() { return identificador; }
    public TipoRelatorio getTipo() { return tipo; }
    public PeriodoAnalise getPeriodo() { return periodo; }
    public Agrupamento getAgrupamento() { return agrupamento; }
    public FiltrosRelatorio getFiltros() { return filtros; }
    public boolean isComparar() { return comparar; }
    public Instant getGeracao() { return geracao; }
    public String getUsuario() { return usuario; }
    public String getUsuarioNome() { return usuarioNome; }
    public Autorizacao getPerfil() { return perfil; }
    public int getVersao() { return versao; }
    public Optional<RelatorioIdentificador> getAnterior() { return anterior; }
    public SituacaoRelatorio getSituacao() { return situacao; }
    public ConteudoRelatorio getConteudo() { return conteudo; }
}
