package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.relatorio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.Autorizacao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Agrupamento;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.MascaraDadosPessoais;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Relatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.SituacaoRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.TipoRelatorio;

/**
 * Relatório guardado, como exibido no sistema. Nos relatórios nominais, o telefone aparece formatado para quem pode
 * acessá-los; o CPF só aparece mascarado (RN019).
 */
public record RelatorioVisualizacao(String identificador, TipoRelatorio tipo, String titulo, LocalDate inicio,
        LocalDate fim, long dias, LocalDate anteriorInicio, LocalDate anteriorFim, Agrupamento agrupamento,
        boolean comparar, String descricaoFiltros, Instant geracao, String usuario, String usuarioNome, Autorizacao perfil,
        int versao, Optional<String> anterior, SituacaoRelatorio situacao, ConteudoRelatorio conteudo,
        List<String> alteracoes, List<ContatoAluno> contatos, RelatorioAutorizacoes autorizacoes) {

    public record ContatoAluno(String aluno, String cpf, String telefone) {
    }

    static RelatorioVisualizacao de(Relatorio relatorio, String descricaoFiltros, List<String> alteracoes,
            RelatorioAutorizacoes autorizacoes) {
        var conteudo = relatorio.getConteudo();
        var contatos = new ArrayList<ContatoAluno>();
        for (var aluno : conteudo.alunosEmRisco()) {
            contatos.add(new ContatoAluno(aluno.aluno(), MascaraDadosPessoais.cpf(aluno.cpf()),
                    MascaraDadosPessoais.telefoneFormatado(aluno.telefone())));
        }
        conteudo.financeiro().ifPresent(financeiro -> financeiro.atrasadas().forEach(item -> contatos.add(new ContatoAluno(
                item.aluno(), MascaraDadosPessoais.cpf(item.cpf()), MascaraDadosPessoais.telefoneFormatado(item.telefone())))));
        var periodo = relatorio.getPeriodo();
        var anteriorPeriodo = periodo.anterior();
        return new RelatorioVisualizacao(relatorio.getIdentificador().orElseThrow().toString(), relatorio.getTipo(),
                relatorio.getTipo().getTitulo(), periodo.inicio(), periodo.fim(), periodo.dias(), anteriorPeriodo.inicio(),
                anteriorPeriodo.fim(), relatorio.getAgrupamento(), relatorio.isComparar(), descricaoFiltros,
                relatorio.getGeracao(), relatorio.getUsuario(), relatorio.getUsuarioNome(), relatorio.getPerfil(),
                relatorio.getVersao(), relatorio.getAnterior().map(Object::toString), relatorio.getSituacao(),
                semDadosPessoais(conteudo), alteracoes, contatos, autorizacoes);
    }

    /** Os dados pessoais saem do conteúdo e vão para a lista de contatos, já formatados ou mascarados. */
    private static ConteudoRelatorio semDadosPessoais(ConteudoRelatorio conteudo) {
        var alunos = conteudo.alunosEmRisco().stream().map(aluno -> new ConteudoRelatorio.AlunoEmRisco(aluno.aluno(),
                aluno.nome(), aluno.plano(), aluno.personal(), aluno.ultimaEntrada(), aluno.diasSemEntrar(),
                aluno.frequenciaAnterior(), aluno.frequenciaAtual(), aluno.motivo(), aluno.queda(), Optional.empty(),
                Optional.empty())).toList();
        var financeiro = conteudo.financeiro().map(valor -> new ConteudoRelatorio.Financeiro(valor.receitaBruta(),
                valor.pagamentos(), valor.estornos(), valor.quantidadeEstornos(), valor.receitaLiquida(),
                valor.totalComVencimento(), valor.valorEmAberto(), valor.taxaInadimplencia(), valor.faixasAtraso(),
                valor.atrasadas().stream().map(item -> new ConteudoRelatorio.MensalidadeAtrasada(item.aluno(), item.nome(),
                        item.plano(), item.vencimento(), item.diasAtraso(), item.valor(), Optional.empty(),
                        Optional.empty())).toList()));
        return new ConteudoRelatorio(conteudo.resumo(), conteudo.indicadores(), conteudo.grupos(), conteudo.movimento(),
                conteudo.planos(), conteudo.ocupacao(), financeiro, conteudo.personais(), alunos, conteudo.metas());
    }
}
