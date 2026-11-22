package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.relatorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import br.edu.cesar.treinoconecta.treinoconectacore.dominio.core.plano.PlanoIdentificador;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public final class RelatorioStepDefinitions {
    private LocalDate hoje = LocalDate.now();
    private PeriodoAnalise periodo;
    private int grupos;
    private Variacao variacao;
    private boolean atingida;
    private String cpf;
    private String telefone;
    private RuntimeException falha;

    @Dado("que a data atual dos relatórios é {string}")
    public void dataAtual(String data) {
        hoje = LocalDate.parse(data);
    }

    @Quando("eu informar o período de {string} a {string} no relatório")
    public void informarPeriodo(String inicio, String fim) {
        executar(() -> periodo = PeriodoAnalise.validar(LocalDate.parse(inicio), LocalDate.parse(fim), hoje));
    }

    @Quando("eu agrupar por {string} o período de {string} a {string}")
    public void agrupar(String agrupamento, String inicio, String fim) {
        executar(() -> {
            var analisado = PeriodoAnalise.validar(LocalDate.parse(inicio), LocalDate.parse(fim), hoje);
            var escolhido = Agrupamento.valueOf(agrupamento);
            escolhido.validarPara(analisado);
            grupos = escolhido.dividir(analisado).size();
        });
    }

    @Quando("eu filtrar por plano o relatório de {string}")
    public void filtrarPorPlano(String tipo) {
        var filtros = new FiltrosRelatorio(Optional.of(new PlanoIdentificador("1")), Optional.empty(), Optional.empty(),
                Optional.empty());
        executar(() -> filtros.validarPara(TipoRelatorio.valueOf(tipo)));
    }

    @Quando("eu comparar o valor {bigdecimal} com o anterior {bigdecimal} em um indicador em que {string}")
    public void comparar(BigDecimal atual, BigDecimal anterior, String sentido) {
        variacao = Variacao.calcular(atual, Optional.of(anterior), SentidoIndicador.valueOf(sentido));
    }

    @Quando("o realizado for {bigdecimal} para uma meta de {bigdecimal} em um indicador em que {string}")
    public void avaliarMeta(BigDecimal realizado, BigDecimal desejado, String sentido) {
        atingida = SentidoIndicador.valueOf(sentido).atinge(realizado, desejado);
    }

    @Quando("eu exportar o aluno com CPF {string} e telefone {string}")
    public void exportarAluno(String cpfInformado, String telefoneInformado) {
        cpf = MascaraDadosPessoais.cpf(Optional.of(cpfInformado));
        telefone = MascaraDadosPessoais.telefone(Optional.of(telefoneInformado));
    }

    @Quando("eu exportar o aluno sem CPF e sem telefone")
    public void exportarAlunoSemContato() {
        cpf = MascaraDadosPessoais.cpf(Optional.empty());
        telefone = MascaraDadosPessoais.telefone(Optional.empty());
    }

    @Então("o período do relatório tem {int} dias")
    public void periodoTem(int dias) {
        assertNull(falha);
        assertEquals(dias, periodo.dias());
    }

    @Então("o relatório tem {int} grupos")
    public void relatorioTemGrupos(int quantidade) {
        assertNull(falha);
        assertEquals(quantidade, grupos);
    }

    @Então("o relatório é rejeitado com a mensagem {string}")
    public void rejeitado(String mensagem) {
        assertNotNull(falha);
        assertEquals(mensagem, falha.getMessage());
    }

    @Então("o relatório não é rejeitado")
    public void naoRejeitado() {
        assertNull(falha);
    }

    @Então("o resultado da comparação é {string}")
    public void resultadoComparacao(String resultado) {
        assertEquals(Variacao.ResultadoComparacao.valueOf(resultado), variacao.resultado());
    }

    @Então("a variação percentual é {string}")
    public void variacaoPercentual(String percentual) {
        assertEquals(new BigDecimal(percentual), variacao.percentual().orElseThrow());
    }

    @Então("a meta da academia é considerada {string}")
    public void metaConsiderada(String situacao) {
        assertEquals(situacao.equals("atingida"), atingida);
    }

    @Então("o CPF exportado é {string}")
    public void cpfExportado(String esperado) {
        assertEquals(esperado, cpf);
    }

    @Então("o telefone exportado é {string}")
    public void telefoneExportado(String esperado) {
        assertEquals(esperado, telefone);
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException excecao) {
            falha = excecao;
        }
    }
}
