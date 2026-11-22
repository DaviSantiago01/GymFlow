package br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.spi.IRelatorioAplicacaoRepositorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.aulacoletiva.AulaColetivaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.personal.PersonalIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.FiltrosRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ParametrosRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Relatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.TipoRelatorio;
import dev.sauloaraujo.d5.framework.aplicacao.IConsultaResultado;
import dev.sauloaraujo.d5.framework.aplicacao.impl.AgregadoAplicacaoServicoAbstrato;
import dev.sauloaraujo.d5.framework.aplicacao.impl.ConsultaResultado;

public final class RelatorioAplicacaoServico extends AgregadoAplicacaoServicoAbstrato<RelatorioIdentificador, Relatorio,
        RelatorioAutorizacoes, RelatorioProjecao, RelatorioConsulta> {
    private final IRelatorioAplicacaoRepositorio repositorio;
    private final RelatorioServico servico;
    private final Clock relogio;

    public RelatorioAplicacaoServico(IRelatorioAplicacaoRepositorio repositorio, RelatorioServico servico, Clock relogio) {
        super(repositorio, servico);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.servico = Objects.requireNonNull(servico);
        this.relogio = Objects.requireNonNull(relogio);
    }

    @Override
    public IConsultaResultado<RelatorioProjecao> pesquisar(RelatorioConsulta consulta) {
        var resultado = super.pesquisar(consulta);
        var projecoes = new ArrayList<RelatorioProjecao>();
        for (var projecao : resultado.getProjecoes()) {
            projecoes.add(projecao.comDescricaoFiltros(descreverFiltros(projecao)));
        }
        return new ConsultaResultado<>(projecoes, resultado.getTotal());
    }

    private String descreverFiltros(RelatorioProjecao projecao) {
        var filtros = new FiltrosRelatorio(
                projecao.plano().map(PlanoIdentificador::new),
                projecao.modalidade(),
                projecao.personal().map(PersonalIdentificador::new),
                projecao.aula().map(AulaColetivaIdentificador::new));
        return repositorio.descreverFiltros(filtros);
    }

    public String descreverFiltros(Relatorio relatorio) {
        Objects.requireNonNull(relatorio);
        return repositorio.descreverFiltros(relatorio.getFiltros());
    }

    public FormularioRelatorio formulario() {
        var autorizacoes = servico.criarAutorizacoes(Optional.empty());
        boolean personal = autorizacoes.getGerar() && !autorizacoes.getPainel();
        return new FormularioRelatorio(autorizacoes.getTipos(), repositorio.planos(), repositorio.personais(),
                repositorio.aulas(), repositorio.modalidades(), relogio.instant().atZone(ParametrosAcademia.FUSO).toLocalDate(),
                ParametrosRelatorio.PERIODO_MAXIMO_DIAS, ParametrosRelatorio.AGRUPAMENTO_DIARIO_MAXIMO_DIAS, personal);
    }

    public boolean permitido(TipoRelatorio tipo) {
        return servico.criarAutorizacoes(Optional.empty()).getTipos().contains(tipo);
    }
}
