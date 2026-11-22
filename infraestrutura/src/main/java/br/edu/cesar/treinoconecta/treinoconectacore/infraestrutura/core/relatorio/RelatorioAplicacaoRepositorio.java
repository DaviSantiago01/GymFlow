package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.relatorio;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.OpcaoFiltro;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.RelatorioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.RelatorioOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.RelatorioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.spi.IRelatorioAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.FiltrosRelatorio;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class RelatorioAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<RelatorioProjecao, RelatorioOrdenacao, RelatorioConsulta>
        implements IRelatorioAplicacaoRepositorio {
    private final IRelatorioRepositorioJpa repositorioJpa;
    private final JdbcTemplate banco;
    private final ISegurancaServico seguranca;
    private final Clock relogio;

    public RelatorioAplicacaoRepositorio(IRelatorioRepositorioJpa repositorioJpa, JdbcTemplate banco,
            ISegurancaServico seguranca, Clock relogio) {
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.banco = Objects.requireNonNull(banco);
        this.seguranca = Objects.requireNonNull(seguranca);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    protected Page<RelatorioProjecao> pesquisar(RelatorioConsulta consulta, Pageable pageable) {
        var desde = consulta.getDias().map(dias -> relogio.instant().minus(Duration.ofDays(dias))).orElse(null);
        return repositorioJpa.pesquisar(consulta.getTipo().orElse(null), consulta.getUsuario().orElse(null),
                usuarioRestrito(), desde, paraLike(consulta.getTermo()), pageable);
    }

    /** O gestor vê todo o histórico; o personal, só os relatórios que ele gerou. */
    private @Nullable String usuarioRestrito() {
        return seguranca.possuiAutorizacao(Autorizacao.ADMINISTRADOR.name()) ? null : seguranca.getUsuario();
    }

    @Override
    protected String paraPropriedadeOrdenacao(RelatorioOrdenacao ordenacao) {
        return switch (ordenacao) {
        case GERACAO -> "geracao";
        case TIPO -> "tipo";
        case INICIO -> "inicio";
        };
    }

    @Override
    public String descreverFiltros(FiltrosRelatorio filtros) {
        Objects.requireNonNull(filtros);
        var partes = new ArrayList<String>();
        filtros.plano().ifPresent(plano -> partes.add("plano contratado: " + nome(
                "select PLA_NOME from PLA_PLANO where PLA_ID = ?", plano.toString()).toLowerCase()));
        filtros.modalidade().ifPresent(modalidade -> partes.add("modalidade: " + modalidade));
        filtros.personal().ifPresent(personal -> partes.add("personal: " + nome(
                "select PER_NOME from PER_PERSONAL where PER_ID = ?", personal.toString())));
        filtros.aula().ifPresent(aula -> partes.add("aula coletiva: " + aulas().stream()
                .filter(opcao -> opcao.identificador().equals(aula.toString())).map(OpcaoFiltro::nome).findFirst()
                .orElse(aula.toString())));
        return partes.isEmpty() ? "Nenhum" : String.join("; ", partes);
    }

    private String nome(String sql, String identificador) {
        var nomes = banco.queryForList(sql, String.class, Long.valueOf(identificador));
        return nomes.isEmpty() ? identificador : Objects.requireNonNull(nomes.get(0));
    }

    @Override
    public List<OpcaoFiltro> planos() {
        return banco.query("""
                select PLA_ID, PLA_NOME || ' (' || PLA_DURACAO_MESES || CASE WHEN PLA_DURACAO_MESES = 1 THEN ' mês)' ELSE ' meses)' END
                  from PLA_PLANO order by PLA_DURACAO_MESES, PLA_NOME
                """, (linha, indice) -> new OpcaoFiltro(linha.getString(1), linha.getString(2)));
    }

    @Override
    public List<OpcaoFiltro> personais() {
        var restrito = seguranca.possuiAutorizacao(Autorizacao.PERSONAL.name())
                && !seguranca.possuiAutorizacao(Autorizacao.ADMINISTRADOR.name());
        if (restrito) {
            return banco.query("select PER_ID, PER_NOME from PER_PERSONAL where PER_USUARIO = ?",
                    (linha, indice) -> new OpcaoFiltro(linha.getString(1), linha.getString(2)), seguranca.getUsuario());
        }
        return banco.query("select PER_ID, PER_NOME from PER_PERSONAL order by PER_NOME",
                (linha, indice) -> new OpcaoFiltro(linha.getString(1), linha.getString(2)));
    }

    @Override
    public List<OpcaoFiltro> aulas() {
        return banco.query("""
                select AUC_ID, AUC_NOME, AUC_DIA, AUC_INICIO from AUC_AULA_COLETIVA
                 where AUC_ATIVA order by AUC_NOME, AUC_DIA, AUC_INICIO
                """, (linha, indice) -> new OpcaoFiltro(linha.getString(1), linha.getString(2) + " · "
                        + dia(linha.getString(3)) + ", " + linha.getString(4).substring(0, 5)));
    }

    private static String dia(String dia) {
        return switch (dia) {
        case "SEGUNDA" -> "seg";
        case "TERCA" -> "ter";
        case "QUARTA" -> "qua";
        case "QUINTA" -> "qui";
        case "SEXTA" -> "sex";
        case "SABADO" -> "sáb";
        default -> "dom";
        };
    }

    @Override
    public List<String> modalidades() {
        return banco.queryForList("select distinct AUC_MODALIDADE from AUC_AULA_COLETIVA order by AUC_MODALIDADE",
                String.class).stream().map(Objects::requireNonNull).toList();
    }
}
