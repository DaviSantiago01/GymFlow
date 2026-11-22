package br.edu.cesar.treinoconecta.treinoconectacore.infraestrutura.core.recomendacao;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.RecomendacaoProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.VersaoPlano;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.recomendacao.spi.IRecomendacaoAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.planodetreino.PlanoDeTreinoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.recomendacao.RecomendacaoServico;
import dev.sauloaraujo.d5.framework.dominio.spi.ISegurancaServico;
import dev.sauloaraujo.d5.framework.infraestrutura.AgregadoAplicacaoRepositorioAbstrato;

public final class RecomendacaoAplicacaoRepositorio
        extends AgregadoAplicacaoRepositorioAbstrato<RecomendacaoProjecao, RecomendacaoOrdenacao, RecomendacaoConsulta>
        implements IRecomendacaoAplicacaoRepositorio {
    private final IRecomendacaoRepositorioJpa repositorioJpa;
    private final ISegurancaServico seguranca;
    private final Clock relogio;
    private final JdbcTemplate banco;

    public RecomendacaoAplicacaoRepositorio(IRecomendacaoRepositorioJpa repositorioJpa, ISegurancaServico seguranca,
            Clock relogio, JdbcTemplate banco) {
        this.banco = Objects.requireNonNull(banco);
        this.repositorioJpa = Objects.requireNonNull(repositorioJpa);
        this.seguranca = Objects.requireNonNull(seguranca);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    protected Page<RecomendacaoProjecao> pesquisar(RecomendacaoConsulta consulta, Pageable pageable) {
        var desde = consulta.getDias().map(dias -> relogio.instant().minus(Duration.ofDays(dias))).orElse(null);
        var aluno = consulta.getAluno().map(Long::valueOf).orElse(null);
        boolean sugestoes = consulta.getSugestoesDeTreino().orElse(false) && usuarioPersonal() != null;
        return repositorioJpa.pesquisar(aluno, consulta.getSituacao().orElse(null), consulta.getCategoria().orElse(null),
                desde, usuarioAluno(), usuarioPersonal(), sugestoes, pageable);
    }

    private @Nullable String usuarioAluno() {
        return seguranca.possuiAutorizacao(Autorizacao.USUARIO.name()) ? seguranca.getUsuario() : null;
    }

    private @Nullable String usuarioPersonal() {
        return seguranca.possuiAutorizacao(Autorizacao.PERSONAL.name()) ? seguranca.getUsuario() : null;
    }

    @Override
    protected String paraPropriedadeOrdenacao(RecomendacaoOrdenacao ordenacao) {
        return switch (ordenacao) {
        case GERACAO -> "geracao";
        case VALIDADE -> "validade";
        };
    }

    @Override
    public List<VersaoPlano> versoesDoPlano(PlanoDeTreinoIdentificador plano) {
        Objects.requireNonNull(plano);
        var versoes = new ArrayList<VersaoPlano>();
        Long atual = Long.valueOf(plano.toString());
        while (atual != null && versoes.size() < 100) {
            var codigo = atual;
            var linhas = banco.query("""
                    select p.PLT_ID, p.PLT_VERSAO, p.PLT_CRIACAO, p.PLT_SITUACAO, p.PLT_PLANO_ANTERIOR,
                           (select count(distinct cast(e.EXC_DATA as date)) from EXC_EXECUCAO_EXERCICIO e where e.PLT_ID = p.PLT_ID),
                           r.REC_ID, r.REC_TITULO, r.REC_RESPONDENTE
                      from PLT_PLANO_TREINO p
                      left join REC_RECOMENDACAO r on r.PLT_GERADO = p.PLT_ID
                     where p.PLT_ID = ?
                    """, (linha, indice) -> new LinhaVersao(
                    new VersaoPlano(linha.getString(1), linha.getInt(2), linha.getObject(3, OffsetDateTime.class).toInstant(),
                            linha.getString(4), linha.getInt(6), Optional.ofNullable(linha.getString(7)),
                            Optional.ofNullable(linha.getString(8)), Optional.ofNullable(linha.getString(9))),
                    Optional.ofNullable(linha.getObject(5, Long.class))), codigo);
            if (linhas.isEmpty()) {
                break;
            }
            versoes.add(linhas.get(0).versao());
            atual = linhas.get(0).anterior().orElse(null);
        }
        return versoes;
    }

    private record LinhaVersao(VersaoPlano versao, Optional<Long> anterior) {
    }

    @Override
    public RecomendacaoProjecao projetar(RecomendacaoIdentificador identificador) {
        Objects.requireNonNull(identificador);
        return repositorioJpa.projetar(Long.valueOf(identificador.toString())).orElseThrow(() -> new NoSuchElementException(
                RecomendacaoServico.IDENTIFICADOR_INEXISTENTE_MODELO.formatted(identificador)));
    }
}
