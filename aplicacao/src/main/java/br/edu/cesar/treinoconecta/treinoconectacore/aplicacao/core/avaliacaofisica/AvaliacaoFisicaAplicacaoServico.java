package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.aluno.AlunoOrdenacao;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.avaliacaofisica.spi.IAvaliacaoFisicaAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.meta.MetaAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aluno.AlunoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisica;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.AvaliacaoFisicaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.avaliacaofisica.IndicadorFisico;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.aplicacao.impl.Consulta;

public final class AvaliacaoFisicaAplicacaoServico extends
        AgregadoAplicacaoServicoAbstrato<AvaliacaoFisicaIdentificador, AvaliacaoFisica, AvaliacaoFisicaAutorizacoes, AvaliacaoFisicaProjecao, AvaliacaoFisicaConsulta> {
    private final IAvaliacaoFisicaAplicacaoRepositorio repositorio;
    private final AvaliacaoFisicaServico servico;
    private final AlunoAplicacaoServico alunos;
    private final MetaAplicacaoServico metas;

    public AvaliacaoFisicaAplicacaoServico(IAvaliacaoFisicaAplicacaoRepositorio repositorio, AvaliacaoFisicaServico servico,
            AlunoAplicacaoServico alunos, MetaAplicacaoServico metas) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
        this.alunos = Objects.requireNonNull(alunos);
        this.metas = Objects.requireNonNull(metas);
    }

    public AvaliacaoFisicaFormulario formulario() {
        var consulta = new AlunoConsulta(Optional.of(AlunoOrdenacao.NOME), Optional.empty(), Optional.empty(),
                Optional.of(Consulta.TAMANHO_PAGINA_MAXIMO), Optional.empty());
        var medidas = new ArrayList<IndicadorFisico>();
        for (var indicador : IndicadorFisico.values()) {
            if (indicador.isMedidaCorporal()) {
                medidas.add(indicador);
            }
        }
        return new AvaliacaoFisicaFormulario(alunos.pesquisar(consulta).getProjecoes(), medidas, ParametrosAcademia.PESO_MINIMO,
                ParametrosAcademia.PESO_MAXIMO, ParametrosAcademia.ALTURA_MINIMA, ParametrosAcademia.ALTURA_MAXIMA,
                ParametrosAcademia.MEDIDA_MINIMA, ParametrosAcademia.MEDIDA_MAXIMA);
    }

    public AvaliacaoFisicaDetalhe detalhe(AvaliacaoFisicaIdentificador identificador) {
        var avaliacao = servico.buscar(identificador);
        var referencia = servico.buscarReferencia(avaliacao, Optional.empty()).flatMap(AvaliacaoFisica::getIdentificador);
        return new AvaliacaoFisicaDetalhe(repositorio.projetar(identificador), avaliacao.getMedidas(), referencia,
                avaliacao.getAvaliacaoAnterior(), servico.podeGerenciar());
    }

    public ComparacaoAvaliacoes comparacao(AvaliacaoFisicaIdentificador identificador,
            Optional<AvaliacaoFisicaIdentificador> referenciaInformada) {
        var atual = servico.buscar(identificador);
        var projecao = repositorio.projetar(identificador);
        var disponiveis = listarAnteriores(atual);
        var referencia = servico.buscarReferencia(atual, referenciaInformada);
        if (referencia.isEmpty()) {
            return new ComparacaoAvaliacoes(projecao, Optional.empty(), List.of(), disponiveis);
        }
        var referenciaProjetada = repositorio.projetar(referencia.get().getIdentificador().orElseThrow());
        return new ComparacaoAvaliacoes(projecao, Optional.of(referenciaProjetada), atual.compararCom(referencia.get()),
                disponiveis);
    }

    private List<AvaliacaoFisicaProjecao> listarAnteriores(AvaliacaoFisica atual) {
        var anteriores = new ArrayList<AvaliacaoFisicaProjecao>();
        for (var projecao : repositorio.listarValidas(atual.getAluno())) {
            if (projecao.data().isBefore(atual.getData())) {
                anteriores.add(projecao);
            }
        }
        return anteriores;
    }

    public EvolucaoIndicador evolucao(Optional<AlunoIdentificador> alunoInformado, Optional<IndicadorFisico> indicadorInformado) {
        Objects.requireNonNull(indicadorInformado);
        var aluno = servico.resolverAluno(alunoInformado);
        var historico = servico.consultarHistorico(aluno);
        var indicador = indicadorInformado.orElse(IndicadorFisico.PESO);
        return new EvolucaoIndicador(aluno, indicador, List.of(IndicadorFisico.values()), historico.pontos(indicador),
                historico.tendencia(indicador), historico.interpretacao(indicador));
    }

    public SituacaoAtualAluno situacao(Optional<AlunoIdentificador> alunoInformado) {
        var aluno = servico.resolverAluno(alunoInformado);
        var historico = servico.consultarHistorico(aluno);
        var metasDoAluno = metas.listarDoAluno(aluno);
        var recente = historico.maisRecente();
        if (recente.isEmpty()) {
            return new SituacaoAtualAluno(aluno, Optional.empty(), List.of(), metasDoAluno);
        }
        var indicadores = new ArrayList<SituacaoIndicador>();
        for (var indicador : IndicadorFisico.values()) {
            var valor = recente.get().valorDe(indicador);
            if (valor.isPresent()) {
                indicadores.add(new SituacaoIndicador(indicador, valor.get(), historico.tendencia(indicador),
                        historico.interpretacao(indicador)));
            }
        }
        var projecao = repositorio.projetar(recente.get().getIdentificador().orElseThrow());
        return new SituacaoAtualAluno(aluno, Optional.of(projecao), indicadores, metasDoAluno);
    }
}
