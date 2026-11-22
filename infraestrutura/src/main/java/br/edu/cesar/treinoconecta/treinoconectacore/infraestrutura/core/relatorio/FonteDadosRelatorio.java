package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.relatorio;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.adesaoplano.SituacaoAdesao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.DiaSemana;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.AlunoDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.AulaDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.EntradaDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.ExecucaoDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.MatriculaDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.MensalidadeDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.MetaAtingidaDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.PagamentoDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.PersonalDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.PlanoDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.PlanoTreinoDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.TrancamentoDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.DadosAcademia.UsoEstacionamentoDado;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.PeriodoAnalise;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.spi.IFonteDadosRelatorio;

/** Lê os registros da operação diretamente das tabelas, sem montar os agregados de cada módulo. */
public final class FonteDadosRelatorio implements IFonteDadosRelatorio {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MARGEM_MENSALIDADES_DIAS = 62;

    private final JdbcTemplate banco;

    public FonteDadosRelatorio(JdbcTemplate banco) {
        this.banco = Objects.requireNonNull(banco);
    }

    @Override
    public DadosAcademia carregar(LocalDate desde, LocalDate ate) {
        Objects.requireNonNull(desde);
        Objects.requireNonNull(ate);
        var inicio = Timestamp.valueOf(desde.atStartOfDay());
        var fim = Timestamp.valueOf(ate.plusDays(1).atStartOfDay());
        var matriculas = banco.query("""
                select ADP_ID, ALU_ID, PLA_ID, ADP_PERIODICIDADE_MESES, ADP_INICIO, ADP_ENCERRAMENTO, ADP_SITUACAO
                  from ADP_ADESAO_PLANO where ADP_INICIO <= ?
                """, (linha, indice) -> new MatriculaDado(linha.getString(1), linha.getString(2), linha.getString(3),
                linha.getInt(4), linha.getDate(5).toLocalDate(),
                Optional.ofNullable(linha.getDate(6)).map(Date::toLocalDate), SituacaoAdesao.valueOf(linha.getString(7))),
                Date.valueOf(ate));
        var trancamentos = banco.query("""
                select t.ADP_ID, a.ALU_ID, t.TRC_INICIO, t.TRC_FIM
                  from TRC_TRANCAMENTO t join ADP_ADESAO_PLANO a on a.ADP_ID = t.ADP_ID
                """, (linha, indice) -> new TrancamentoDado(linha.getString(1), linha.getString(2),
                linha.getDate(3).toLocalDate(), linha.getDate(4).toLocalDate()));
        var entradas = banco.query("""
                select ALU_ID, RAC_ENTRADA from RAC_REGISTRO_ACESSO
                 where RAC_ENTRADA >= ? and RAC_ENTRADA < ?
                """, (linha, indice) -> {
            var entrada = instante(linha.getObject(2, OffsetDateTime.class));
            return new EntradaDado(linha.getString(1), entrada.atZone(ParametrosAcademia.FUSO).toLocalDateTime(), entrada);
        }, inicioLocal(desde), inicioLocal(ate.plusDays(1)));
        var aulas = banco.query("""
                select a.AUC_ID, a.AUC_NOME, a.AUC_MODALIDADE, a.AUC_DIA, a.AUC_INICIO, a.AUC_CAPACIDADE,
                       (select count(*) from IAC_INSCRICAO_AULA i where i.AUC_ID = a.AUC_ID)
                  from AUC_AULA_COLETIVA a where a.AUC_ATIVA
                """, (linha, indice) -> new AulaDado(linha.getString(1), linha.getString(2), linha.getString(3),
                DiaSemana.valueOf(linha.getString(4)), LocalTime.parse(linha.getString(5)), linha.getInt(6), linha.getInt(7)));
        var estacionamento = banco.query("""
                select EST_ENTRADA, EST_SAIDA from EST_USO_ESTACIONAMENTO where EST_ENTRADA >= ? and EST_ENTRADA < ?
                """, (linha, indice) -> new UsoEstacionamentoDado(linha.getTimestamp(1).toLocalDateTime(),
                linha.getTimestamp(2).toLocalDateTime()), inicio, fim);
        var planosTreino = banco.query("""
                select ALU_ID, PER_ID, PLT_FREQUENCIA_SEMANAL, PLT_CRIACAO, PLT_ENCERRAMENTO
                  from PLT_PLANO_TREINO where PLT_SITUACAO <> 'RASCUNHO'
                """, (linha, indice) -> new PlanoTreinoDado(linha.getString(1), linha.getString(2), linha.getInt(3),
                dataLocal(instante(linha.getObject(4, OffsetDateTime.class))),
                Optional.ofNullable(linha.getObject(5, OffsetDateTime.class)).map(valor -> dataLocal(instante(valor)))));
        var execucoes = banco.query("""
                select ALU_ID, EXC_DATA from EXC_EXECUCAO_EXERCICIO where EXC_DATA >= ?
                """, (linha, indice) -> new ExecucaoDado(linha.getString(1),
                dataLocal(instante(linha.getObject(2, OffsetDateTime.class)))),
                OffsetDateTime.ofInstant(desde.atStartOfDay(ParametrosAcademia.FUSO).toInstant(), ParametrosAcademia.FUSO));
        var metas = banco.query("select ALU_ID, MET_DATA_ATINGIDA from MET_META where MET_DATA_ATINGIDA is not null",
                (linha, indice) -> new MetaAtingidaDado(linha.getString(1), linha.getDate(2).toLocalDate()));
        var alunos = banco.query("""
                select ALU_ID, ALU_NOME, ALU_CPF, ALU_TELEFONE from ALU_ALUNO
                """, (linha, indice) -> new AlunoDado(linha.getString(1), linha.getString(2), digitos(linha.getString(3)),
                digitos(linha.getString(4))));
        var personais = banco.query("select PER_ID, PER_NOME from PER_PERSONAL",
                (linha, indice) -> new PersonalDado(linha.getString(1), linha.getString(2)));
        var planos = banco.query("select PLA_ID, PLA_NOME from PLA_PLANO",
                (linha, indice) -> new PlanoDado(linha.getString(1), linha.getString(2)));
        return new DadosAcademia(matriculas, trancamentos, entradas, mensalidades(desde, ate), aulas, estacionamento,
                planosTreino, execucoes, metas, alunos, personais, planos);
    }

    /** Mensalidades com vencimento próximo da janela analisada, já com os pagamentos de cada uma. */
    private List<MensalidadeDado> mensalidades(LocalDate desde, LocalDate ate) {
        var pagamentos = new HashMap<String, List<PagamentoDado>>();
        banco.query("""
                select p.MEN_ID, p.PAG_VALOR, p.PAG_REGISTRO, p.PAG_ESTORNO
                  from PAG_PAGAMENTO p join MEN_MENSALIDADE m on m.MEN_ID = p.MEN_ID
                 where m.MEN_VENCIMENTO between ? and ?
                """, linha -> {
            var registro = instante(linha.getObject(3, OffsetDateTime.class));
            var estorno = Optional.ofNullable(linha.getObject(4, OffsetDateTime.class)).map(FonteDadosRelatorio::instante);
            pagamentos.computeIfAbsent(linha.getString(1), chave -> new ArrayList<>()).add(new PagamentoDado(
                    linha.getBigDecimal(2), dataLocal(registro), estorno.map(FonteDadosRelatorio::dataLocal), registro,
                    estorno));
        }, Date.valueOf(desde.minusDays(MARGEM_MENSALIDADES_DIAS)), Date.valueOf(ate.plusDays(MARGEM_MENSALIDADES_DIAS)));
        return banco.query("""
                select m.MEN_ID, m.ALU_ID, a.PLA_ID, m.MEN_VENCIMENTO, m.MEN_VALOR
                  from MEN_MENSALIDADE m join ADP_ADESAO_PLANO a on a.ADP_ID = m.ADP_ID
                 where m.MEN_VENCIMENTO between ? and ?
                """, (linha, indice) -> new MensalidadeDado(linha.getString(1), linha.getString(2), linha.getString(3),
                linha.getDate(4).toLocalDate(), linha.getBigDecimal(5),
                pagamentos.getOrDefault(linha.getString(1), List.of())),
                Date.valueOf(desde.minusDays(MARGEM_MENSALIDADES_DIAS)), Date.valueOf(ate.plusDays(MARGEM_MENSALIDADES_DIAS)));
    }

    @Override
    public Set<String> modalidades() {
        return new LinkedHashSet<>(banco.queryForList("select distinct AUC_MODALIDADE from AUC_AULA_COLETIVA", String.class));
    }

    @Override
    public String nomeDoUsuario(String usuario) {
        Objects.requireNonNull(usuario);
        for (var consulta : List.of("select ADM_NOME from ADM_ADMINISTRADOR where ADM_USUARIO = ?",
                "select PER_NOME from PER_PERSONAL where PER_USUARIO = ?",
                "select ALU_NOME from ALU_ALUNO where ALU_USUARIO = ?")) {
            var nomes = banco.queryForList(consulta, String.class, usuario);
            if (!nomes.isEmpty()) {
                return Objects.requireNonNull(nomes.get(0));
            }
        }
        return usuario;
    }

    /** RN018: estornos e pagamentos do período registrados depois da geração do relatório. */
    @Override
    public List<String> alteracoesApos(PeriodoAnalise periodo, Instant geracao) {
        var limite = OffsetDateTime.ofInstant(geracao, ParametrosAcademia.FUSO);
        var inicio = Date.valueOf(periodo.inicio());
        var fim = Date.valueOf(periodo.fim());
        var moeda = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        var alteracoes = new ArrayList<String>();
        banco.query("""
                select p.PAG_ESTORNO, p.PAG_VALOR, p.PAG_REGISTRO
                  from PAG_PAGAMENTO p join MEN_MENSALIDADE m on m.MEN_ID = p.MEN_ID
                 where p.PAG_ESTORNO > ?
                   and (m.MEN_VENCIMENTO between ? and ? or cast(p.PAG_REGISTRO as date) between ? and ?)
                 order by p.PAG_ESTORNO
                """, linha -> {
            alteracoes.add("Em " + formatar(linha.getObject(1, OffsetDateTime.class)) + " foi registrado um estorno de "
                    + moeda.format(linha.getBigDecimal(2)).replace(' ', ' ') + " referente a um pagamento de "
                    + formatar(linha.getObject(3, OffsetDateTime.class)) + ".");
        }, limite, inicio, fim, inicio, fim);
        banco.query("""
                select p.PAG_REGISTRO, m.MEN_VENCIMENTO
                  from PAG_PAGAMENTO p join MEN_MENSALIDADE m on m.MEN_ID = p.MEN_ID
                 where p.PAG_REGISTRO > ? and m.MEN_VENCIMENTO between ? and ?
                 order by p.PAG_REGISTRO
                """, linha -> {
            alteracoes.add("Em " + formatar(linha.getObject(1, OffsetDateTime.class))
                    + " foi registrado o pagamento de uma mensalidade com vencimento em "
                    + linha.getDate(2).toLocalDate().format(DATA) + ".");
        }, limite, inicio, fim);
        return alteracoes;
    }

    /** Início do dia no fuso da academia, para comparar com colunas gravadas com fuso horário. */
    private static OffsetDateTime inicioLocal(LocalDate data) {
        return data.atStartOfDay(ParametrosAcademia.FUSO).toOffsetDateTime();
    }

    /** CPF e telefone são guardados no cadastro do aluno com ou sem pontuação; os relatórios usam só os dígitos. */
    private static Optional<String> digitos(@Nullable String valor) {
        return Optional.ofNullable(valor).map(texto -> texto.replaceAll("\\D", "")).filter(texto -> !texto.isEmpty());
    }

    private static String formatar(OffsetDateTime valor) {
        return dataLocal(instante(valor)).format(DATA);
    }

    private static Instant instante(OffsetDateTime valor) {
        return valor.toInstant();
    }

    private static LocalDate dataLocal(Instant instante) {
        return instante.atZone(ParametrosAcademia.FUSO).toLocalDate();
    }
}
