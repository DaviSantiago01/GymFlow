package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.spi.IMetaAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.Meta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.meta.MetaServico;
import dev.sauloaraujo.d5.framework.aplicacao.IConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;
import dev.sauloaraujo.d5.framework.aplicacao.impl.ConsultaResultado;

public final class MetaAplicacaoServico
        extends AgregadoAplicacaoServicoAbstrato<MetaIdentificador, Meta, MetaAutorizacoes, MetaProjecao, MetaConsulta> {
    private final IMetaAplicacaoRepositorio repositorio;
    private final MetaServico servico;
    private final AvaliacaoFisicaServico avaliacoes;
    private final AlunoAplicacaoServico alunos;

    public MetaAplicacaoServico(IMetaAplicacaoRepositorio repositorio, MetaServico servico, AvaliacaoFisicaServico avaliacoes,
            AlunoAplicacaoServico alunos) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
        this.avaliacoes = Objects.requireNonNull(avaliacoes);
        this.alunos = Objects.requireNonNull(alunos);
    }

    @Override
    public IConsultaResultado<MetaProjecao> pesquisar(MetaConsulta consulta) {
        var resultado = super.pesquisar(consulta);
        var projecoes = new ArrayList<MetaProjecao>();
        for (var projecao : resultado.getProjecoes()) {
            projecoes.add(comCumprimento(projecao));
        }
        return new ConsultaResultado<>(projecoes, resultado.getTotal());
    }

    public MetaProjecao projetar(MetaIdentificador identificador) {
        return comCumprimento(repositorio.projetar(identificador));
    }

    public List<MetaProjecao> listarDoAluno(AlunoIdentificador aluno) {
        Objects.requireNonNull(aluno);
        var consulta = new MetaConsulta(Optional.of(MetaOrdenacao.FIM), Optional.empty(), Optional.empty(),
                Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.of(aluno.toString()), Optional.empty());
        return pesquisar(consulta).getProjecoes();
    }

    public MetaFormulario formulario() {
        var consulta = new AlunoConsulta(Optional.of(AlunoOrdenacao.NOME), Optional.empty(), Optional.empty(),
                Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.empty());
        return new MetaFormulario(alunos.pesquisar(consulta).getProjecoes(), List.of(IndicadorFisico.values()));
    }

    private MetaProjecao comCumprimento(MetaProjecao projecao) {
        var historico = avaliacoes.consultarHistorico(projecao.aluno());
        var recente = historico.maisRecenteCom(projecao.indicador());
        if (recente.isEmpty()) {
            return projecao;
        }
        var valor = recente.get().valorDe(projecao.indicador()).orElseThrow();
        var meta = servico.buscar(projecao.identificador());
        return projecao.comCumprimento(valor, meta.percentualCumprimento(valor), meta.statusCumprimento(valor));
    }
}
