package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.metaacademia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.MetaAcademiaAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.MetaAcademiaConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.metaacademia.MetaAcademiaProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademiaAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademiaIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.metaacademia.MetaAcademiaServico;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.CumprimentoMeta;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.IndicadorAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioServico;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/meta-academia")
public class MetaAcademiaControlador extends AgregadoControladorAbstrato<MetaAcademiaIdentificador, MetaAcademia,
        MetaAcademiaAutorizacoes, MetaAcademiaProjecao, MetaAcademiaConsulta> {
    private final MetaAcademiaServico servico;
    private final RelatorioServico relatorios;

    public MetaAcademiaControlador(MetaAcademiaServico servico, MetaAcademiaAplicacaoServico consultas,
            RelatorioServico relatorios) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.relatorios = Objects.requireNonNull(relatorios);
    }

    public record MetaRequisicao(IndicadorAcademia indicador, BigDecimal valorDesejado, LocalDate inicio, LocalDate fim) {
    }

    public record AjusteRequisicao(BigDecimal valorDesejado, LocalDate inicio, LocalDate fim) {
    }

    public record IndicadorOpcao(IndicadorAcademia indicador, String descricao, String sentido, String unidade) {
    }

    /** Cumprimento das metas do mês, calculado com os indicadores do período já transcorrido (RN016). */
    public record Acompanhamento(String mes, LocalDate inicio, LocalDate fim, LocalDate apuradoAte, boolean encerrado,
            List<CumprimentoMeta> metas) {
    }

    @Override
    public List<IndicadorOpcao> inclusao() {
        return Arrays.stream(IndicadorAcademia.values()).map(indicador -> new IndicadorOpcao(indicador,
                indicador.getDescricao(), indicador.getSentido().name(), indicador.getUnidade().name())).toList();
    }

    @GetMapping("acompanhamento")
    public Acompanhamento acompanhamento(@RequestParam(required = false) String mes) {
        var hoje = LocalDate.now(ParametrosAcademia.FUSO);
        var referencia = mes == null || mes.isBlank() ? YearMonth.from(hoje) : YearMonth.parse(mes);
        var painel = relatorios.painel(referencia);
        return new Acompanhamento(referencia.toString(), referencia.atDay(1), referencia.atEndOfMonth(),
                painel.periodo().fim(), referencia.atEndOfMonth().isBefore(hoje), painel.metas());
    }

    @PostMapping("cadastrar")
    public MetaAcademia cadastrar(@RequestBody MetaRequisicao requisicao) {
        return servico.cadastrar(requisicao.indicador(), requisicao.valorDesejado(), requisicao.inicio(), requisicao.fim());
    }

    @PostMapping("{identificador}/encerrar")
    public MetaAcademia encerrar(@PathVariable MetaAcademiaIdentificador identificador) {
        return servico.encerrar(identificador);
    }

    @PostMapping("{identificador}/substituir")
    public MetaAcademia substituir(@PathVariable MetaAcademiaIdentificador identificador,
            @RequestBody AjusteRequisicao requisicao) {
        return servico.substituir(identificador, requisicao.valorDesejado(), requisicao.inicio(), requisicao.fim());
    }
}
