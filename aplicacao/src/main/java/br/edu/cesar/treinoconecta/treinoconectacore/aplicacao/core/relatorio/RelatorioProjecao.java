package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Agrupamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Relatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.SituacaoRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.TipoRelatorio;
import dev.sauloaraujo.d5.framework.aplicacao.IProjecao;

/** Item do histórico de relatórios, sem os valores calculados. */
public record RelatorioProjecao(String identificador, TipoRelatorio tipo, String titulo, LocalDate inicio, LocalDate fim,
        Agrupamento agrupamento, Optional<String> plano, Optional<String> modalidade, Optional<String> personal,
        Optional<String> aula, String descricaoFiltros, Instant geracao, String usuario, String usuarioNome,
        Autorizacao perfil, int versao, SituacaoRelatorio situacao) implements IProjecao<Relatorio> {
    public RelatorioProjecao(Long identificador, TipoRelatorio tipo, LocalDate inicio, LocalDate fim, Agrupamento agrupamento,
            @Nullable Long plano, @Nullable String modalidade, @Nullable Long personal, @Nullable Long aula,
            Instant geracao, String usuario, String usuarioNome, Autorizacao perfil, int versao,
            SituacaoRelatorio situacao) {
        this(identificador.toString(), tipo, tipo.getTitulo(), inicio, fim, agrupamento,
                Optional.ofNullable(plano).map(Object::toString), Optional.ofNullable(modalidade),
                Optional.ofNullable(personal).map(Object::toString), Optional.ofNullable(aula).map(Object::toString), "",
                geracao, usuario, usuarioNome, perfil, versao, situacao);
    }

    public RelatorioProjecao comDescricaoFiltros(String descricao) {
        return new RelatorioProjecao(identificador, tipo, titulo, inicio, fim, agrupamento, plano, modalidade, personal,
                aula, descricao, geracao, usuario, usuarioNome, perfil, versao, situacao);
    }
}
