package br.edu.cesar.treinoconecta.treinoconectacore.ui.backend.core.relatorio;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.FormularioRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.RelatorioAplicacaoServico;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.RelatorioConsulta;
import br.edu.cesar.treinoconecta.treinoconectacore.aplicacao.core.relatorio.RelatorioProjecao;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.ParametrosAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ConteudoRelatorio.Resumo;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.ExportacaoRelatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.PainelAcademia;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.Relatorio;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioAutorizacoes;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioIdentificador;
import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio.RelatorioServico;
import dev.sauloaraujo.d5.framework.dominio.spi.NaoAutorizadoExcecao;
import dev.sauloaraujo.d5.framework.ui.AgregadoControladorAbstrato;
import dev.sauloaraujo.d5.framework.ui.Backend;

@RestController
@RequestMapping(Backend.PREFIXO + "core/relatorio")
public class RelatorioControlador extends AgregadoControladorAbstrato<RelatorioIdentificador, Relatorio,
        RelatorioAutorizacoes, RelatorioProjecao, RelatorioConsulta> {
    private static final DateTimeFormatter ARQUIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final RelatorioServico servico;
    private final RelatorioAplicacaoServico consultas;

    public RelatorioControlador(RelatorioServico servico, RelatorioAplicacaoServico consultas) {
        super(servico, consultas);
        this.servico = Objects.requireNonNull(servico);
        this.consultas = Objects.requireNonNull(consultas);
    }

    @Override
    public FormularioRelatorio inclusao() {
        if (!servico.criarAutorizacoes(Optional.empty()).getGerar()) {
            throw new NaoAutorizadoExcecao("Ação não autorizada");
        }
        return consultas.formulario();
    }

    @PostMapping("verificar")
    public Resumo verificar(@RequestBody RelatorioRequisicao requisicao) {
        return servico.verificar(requisicao.tipo(), requisicao.inicio(), requisicao.fim(), requisicao.agrupamento(),
                requisicao.filtros());
    }

    @PostMapping("gerar")
    public RelatorioVisualizacao gerar(@RequestBody RelatorioRequisicao requisicao) {
        var relatorio = servico.gerar(requisicao.tipo(), requisicao.inicio(), requisicao.fim(), requisicao.agrupamento(),
                requisicao.filtros(), requisicao.compararComAnterior());
        return visualizar(relatorio);
    }

    @GetMapping("{identificador}/visualizacao")
    public RelatorioVisualizacao visualizacao(@PathVariable RelatorioIdentificador identificador) {
        return visualizar(servico.buscar(identificador));
    }

    @PostMapping("{identificador}/nova-versao")
    public RelatorioVisualizacao novaVersao(@PathVariable RelatorioIdentificador identificador) {
        return visualizar(servico.gerarNovaVersao(identificador));
    }

    @GetMapping("painel")
    public PainelAcademia painel(@RequestParam(required = false) String mes) {
        var referencia = mes == null || mes.isBlank() ? YearMonth.now(ParametrosAcademia.FUSO) : YearMonth.parse(mes);
        return servico.painel(referencia);
    }

    /** Prévia do arquivo: os mesmos valores, com CPF e telefone já mascarados (RN019 e RN020). */
    @GetMapping("{identificador}/exportacao")
    public ExportacaoRelatorio exportacao(@PathVariable RelatorioIdentificador identificador) {
        var relatorio = buscarParaExportar(identificador);
        return ExportacaoRelatorio.de(relatorio, consultas.descreverFiltros(relatorio));
    }

    @GetMapping("{identificador}/exportar")
    public ResponseEntity<byte[]> exportar(@PathVariable RelatorioIdentificador identificador,
            @RequestParam(defaultValue = "PDF") String formato) {
        var relatorio = buscarParaExportar(identificador);
        var exportacao = ExportacaoRelatorio.de(relatorio, consultas.descreverFiltros(relatorio));
        boolean planilha = "PLANILHA".equalsIgnoreCase(formato);
        var conteudo = planilha ? ExportadorPlanilha.gerar(exportacao) : ExportadorPdf.gerar(exportacao);
        var nome = "relatorio-" + relatorio.getTipo().name().toLowerCase().replace('_', '-') + "-"
                + relatorio.getGeracao().atZone(ParametrosAcademia.FUSO).format(ARQUIVO) + (planilha ? ".xlsx" : ".pdf");
        var tipo = planilha
                ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.APPLICATION_PDF;
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nome, StandardCharsets.UTF_8).build().toString())
                .body(conteudo);
    }

    private Relatorio buscarParaExportar(RelatorioIdentificador identificador) {
        Objects.requireNonNull(identificador);
        if (!servico.criarAutorizacoes(Optional.of(identificador)).getExportar()) {
            throw new NaoAutorizadoExcecao("Ação não autorizada");
        }
        return servico.buscar(identificador);
    }

    private RelatorioVisualizacao visualizar(Relatorio relatorio) {
        var identificador = relatorio.getIdentificador().orElseThrow();
        return RelatorioVisualizacao.de(relatorio, consultas.descreverFiltros(relatorio),
                servico.alteracoesAposGeracao(identificador), servico.criarAutorizacoes(Optional.of(identificador)));
    }
}
